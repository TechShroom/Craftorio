package com.techshroom.mods.craftorio.block.inserters;

import static com.google.common.base.Preconditions.checkState;

import java.util.Optional;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

@AutoValue
public abstract class InserterTransferState {

    private static final InserterTransferState INITIAL = of(Mode.WAITING_TO_PICK_UP, null);
    private static final InserterTransferState BEGIN_RETURNING =
            of(Mode.RETURNING_TO_INITIAL, InserterTransferData.begin(null));

    public static InserterTransferState initial() {
        return INITIAL;
    }

    public static InserterTransferState deserializeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            return initial();
        }
        if (!nbt.hasKey("mode", NBT.TAG_INT)) {
            return initial();
        }
        return of(Mode.values()[nbt.getInteger("mode")], deserializeTrasferData(nbt));
    }

    @Nullable
    private static InserterTransferData deserializeTrasferData(NBTTagCompound nbt) {
        if (!nbt.hasKey("transferdata", NBT.TAG_COMPOUND)) {
            return null;
        }
        NBTTagCompound tdTag = nbt.getCompoundTag("transferdata");
        ItemStack stack =
                tdTag.hasKey("itemstack") ? ItemStack.loadItemStackFromNBT(tdTag.getCompoundTag("itemstack")) : null;
        int ticks = tdTag.getInteger("ticks");

        return InserterTransferData.of(stack, ticks);
    }

    static InserterTransferState of(Mode mode, @Nullable InserterTransferData data) {
        return new AutoValue_InserterTransferState(mode, Optional.ofNullable(data));
    }

    public enum Mode {
        WAITING_TO_PICK_UP, TRANSFERRING_ITEM, WAITING_TO_DELIVER, RETURNING_TO_INITIAL;

        // Helper methods for detecting modes

        public boolean in(Mode mode) {
            return this == mode;
        }

        public boolean in(Mode mode1, Mode mode2) {
            return in(mode1) || in(mode2);
        }

        public boolean in(Mode mode1, Mode mode2, Mode mode3) {
            return in(mode1, mode2) || in(mode3);
        }

    }

    InserterTransferState() {
    }

    public NBTTagCompound serializeNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("mode", getMode().ordinal());
        getTransferData().ifPresent(td -> {
            NBTTagCompound tdTag = new NBTTagCompound();
            if (td.getItemStack() != null) {
                tdTag.setTag("itemstack", td.getItemStack().serializeNBT());
            }
            tdTag.setInteger("ticks", td.getTicks());
            tag.setTag("transferdata", tdTag);
        });
        return null;
    }

    public abstract Mode getMode();

    public abstract Optional<InserterTransferData> getTransferData();

    public final InserterTransferState beginTransferring(ItemStack data) {
        checkState(getMode().in(Mode.WAITING_TO_PICK_UP), "cannot transfer from %s", getMode());
        return of(Mode.TRANSFERRING_ITEM, InserterTransferData.begin(data));
    }

    public final InserterTransferState tickTransfer() {
        // Ticking is OK in return, it tracks the arm progress
        checkState(getMode().in(Mode.TRANSFERRING_ITEM, Mode.RETURNING_TO_INITIAL), "cannot tick in %s", getMode());
        checkState(getTransferData().isPresent(), "cannot tick without transfer data");
        return of(getMode(), getTransferData().get().tick());
    }

    public final InserterTransferState waitForDrop() {
        checkState(getMode().in(Mode.TRANSFERRING_ITEM), "cannot wait from %s", getMode());
        return of(Mode.WAITING_TO_DELIVER, InserterTransferData.begin(getTransferData().get().getItemStack()));
    }

    public final InserterTransferState beginReturning() {
        checkState(getMode().in(Mode.TRANSFERRING_ITEM, Mode.WAITING_TO_DELIVER), "cannot return from %s", getMode());
        return BEGIN_RETURNING;
    }

    public final InserterTransferState beginWaiting() {
        checkState(getMode().in(Mode.RETURNING_TO_INITIAL), "cannot wait from %s", getMode());
        return INITIAL;
    }

}
