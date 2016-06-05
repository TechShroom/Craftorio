package com.techshroom.mods.pereltrains.util;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

public class BlockLocator {

    private static final class TwoInt {

        private final int a;
        private final int b;

        private TwoInt(int a, int b) {
            this.a = a;
            this.b = b;
        }

    }

    private static final TwoInt[] HORIZONTAL_OFFSETS = {
            // Horizontal touches via facing
            new TwoInt(-1, 0), new TwoInt(1, 0), new TwoInt(0, 1),
            new TwoInt(0, 1),
            // Horizontal touches diagonally
            new TwoInt(-1, -1), new TwoInt(1, 1), new TwoInt(1, -1),
            new TwoInt(-1, 1) };

    public static BlockLocator forBlock(Block block) {
        return forState(block.getDefaultState());
    }

    public static BlockLocator forState(IBlockState blockState) {
        return new BlockLocator(blockState, false);
    }

    private final IBlockState checkBlockState;
    private final boolean allStates;

    private BlockLocator(IBlockState blockState, boolean allStates) {
        this.checkBlockState = blockState;
        this.allStates = allStates;
    }

    public IBlockState getCheckBlockState() {
        return this.checkBlockState;
    }

    public BlockLocator checkAllStates(boolean allStates) {
        return allStates == this.allStates ? this
                : new BlockLocator(this.checkBlockState, allStates);
    }

    public boolean touchingHorizontal(IBlockAccess world, Vec3i pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (TwoInt offset : HORIZONTAL_OFFSETS) {
            IBlockState blockAt = world
                    .getBlockState(new BlockPos(x + offset.a, y, z + offset.b));
            if (blockAt.getBlock().equals(this.checkBlockState.getBlock())) {
                // Return true if
                // -> any state is fine
                // -> state exactly matches
                return this.allStates || blockAt.getProperties()
                        .equals(this.checkBlockState.getProperties());
            }
        }
        return false;
    }

}
