package com.techshroom.mods.craftorio.block.inserters;

import java.awt.Color;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.techshroom.mods.craftorio.block.inserters.InserterTransferState.Mode;
import com.techshroom.mods.craftorio.util.GeneralUtility;
import com.techshroom.mods.craftorio.util.InventoryUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.items.IItemHandler;

public class TileEntityInserter extends TileEntity implements ITickable {

    @CapabilityInject(IItemHandler.class)
    private static final Capability<IItemHandler> HANDLER_CAP = null;

    public static final int MAX_TICKS_PER_TRANSFER = 20;

    private InserterTransferState transferState = InserterTransferState.initial();
    private int transferMax;
    private int color;

    private transient boolean connectionsCalculated;
    // Connected blocks
    private transient TileEntity pullInventory;
    private transient TileEntity pushInventory;
    private transient IItemHandler pullHandler;
    private transient IItemHandler pushHandler;

    public void recalculateNeighbors() {
        this.connectionsCalculated = false;
        if (getWorld() == null || !getWorld().isBlockLoaded(getPos())) {
            return;
        }
        if (!Stream.of(EnumFacing.values()).map(getPos()::offset).allMatch(getWorld()::isBlockLoaded)) {
            // Some blocks aren't loaded near us. Calculate later.
            return;
        }
        @SuppressWarnings("deprecation")
        IBlockState blockstate = getBlockType().getStateFromMeta(getBlockMetadata());
        EnumFacing facing = BlockInserter.getInserterFacing(blockstate);
        this.pullInventory = itemHandler(facing.getOpposite()).orElse(null);
        if (this.pullInventory != null) {
            this.pullHandler = this.pullInventory.getCapability(HANDLER_CAP, facing);
        }
        this.pushInventory = itemHandler(facing).orElse(null);
        if (this.pushInventory != null) {
            this.pushHandler = this.pushInventory.getCapability(HANDLER_CAP, facing.getOpposite());
        }
        this.connectionsCalculated = true;
    }

    public void setTransferMax(int transferMax) {
        this.transferMax = transferMax;
    }

    public int getTransferMax() {
        return this.transferMax;
    }

    public void setColor(Color color) {
        setColor(color.getRGB());
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    @Override
    public void update() {
        if (!this.connectionsCalculated) {
            recalculateNeighbors();
        }
        Mode mode = this.transferState.getMode();
        InserterTransferData td;
        switch (mode) {
            case WAITING_TO_PICK_UP:
                if (this.pullInventory != null) {
                    ItemStack stack = InventoryUtil.pull(this.pullHandler, this.transferMax);
                    stateChange(state -> state.beginTransferring(stack));
                }
                break;
            case RETURNING_TO_INITIAL:
            case TRANSFERRING_ITEM:
                stateChange(InserterTransferState::tickTransfer);
                td = this.transferState.getTransferData().get();
                if (td.getTicks() == MAX_TICKS_PER_TRANSFER) {
                    if (mode == Mode.RETURNING_TO_INITIAL) {
                        stateChange(InserterTransferState::beginWaiting);
                    } else {
                        assert mode == Mode.TRANSFERRING_ITEM;
                        if (this.pushHandler == null) {
                            /// hmmm....we could act like factorio and drop it
                            // but factorio only drops ONCE
                            // how about we hold on to it here!
                            stateChange(InserterTransferState::waitForDrop);
                        } else {
                            InventoryUtil.putStack(this.pushHandler, td.getItemStack());
                            stateChange(InserterTransferState::beginReturning);
                        }
                    }
                }
                break;
            case WAITING_TO_DELIVER:
                td = this.transferState.getTransferData().get();
                InventoryUtil.putStack(this.pushHandler, td.getItemStack());
                stateChange(InserterTransferState::beginReturning);
            default:
                throw new AssertionError("missing mode " + mode);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = super.serializeNBT();
        tag.setInteger("transferMax", getTransferMax());
        tag.setInteger("color", getColor());
        tag.setTag("transferState", this.transferState.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        setTransferMax(nbt.getInteger("transferMax"));
        setColor(nbt.getInteger("color"));
        this.transferState = InserterTransferState.deserializeNBT(nbt.getCompoundTag("transferState"));
    }

    private void stateChange(UnaryOperator<InserterTransferState> op) {
        this.transferState = op.apply(this.transferState);
    }

    private Optional<TileEntity> itemHandler(EnumFacing facing) {
        BlockPos offsetPos = getPos().offset(facing);
        return GeneralUtility.itemHandler(getWorld(), offsetPos, facing).map(ok -> getWorld().getTileEntity(offsetPos));
    }

}
