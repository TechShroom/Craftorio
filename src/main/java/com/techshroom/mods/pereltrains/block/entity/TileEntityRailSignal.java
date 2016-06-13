package com.techshroom.mods.pereltrains.block.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.techshroom.mods.pereltrains.block.BlockRailSignal;
import com.techshroom.mods.pereltrains.block.LightValue;
import com.techshroom.mods.pereltrains.segment.Rail;
import com.techshroom.mods.pereltrains.signal.BlockingState;
import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRailSignal extends TileEntity implements RailSignal {

    @Override
    public EnumFacing getControlledDirection() {
        return getWorld().getBlockState(getPos())
                .getValue(BlockRailSignal.ATTACHED_RAIL_PROPERTY);
    }

    @Override
    public Rail getAttachedRail() {
        EnumFacing attachDir = getWorld().getBlockState(getPos())
                .getValue(BlockRailSignal.ATTACHED_RAIL_PROPERTY);
        BlockPos railPos = getPos().offset(attachDir);
        // TODO catch exceptions and include a friendly "you broke it" message?
        return checkNotNull((Rail) getWorld().getTileEntity(railPos),
                "no Rail at %s", railPos);
    }

    @Override
    public void onStateChange(BlockingState previousState) {
        switch (getAttachedRail().getSegment().getState()) {
            case CLOSED:
                setState(LightValue.RED);
                break;
            case OPEN:
                setState(LightValue.GREEN);
                break;
            case EXPECTING:
                setState(LightValue.YELLOW);
                break;
            default:
                setState(LightValue.NONE);
                break;
        }
    }

    private void setState(LightValue light) {
        World w = getWorld();
        w.setBlockState(getPos(), w.getBlockState(getPos())
                .withProperty(BlockRailSignal.LIGHT_PROPERTY, light));
    }

}
