/*
 * This file is part of PerelTrains, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshoom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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

    private static final TwoInt[] HORIZONTAL_OFFSETS = { new TwoInt(-1, 0),
            new TwoInt(1, 0), new TwoInt(0, -1), new TwoInt(0, 1) };

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
