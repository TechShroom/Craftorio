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
package com.techshroom.mods.pereltrains.block;

import com.techshroom.mods.pereltrains.block.entity.TileEntityRailSignal;
import com.techshroom.mods.pereltrains.util.BlockLocator;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockRailSignal extends ExtendedBlock
        implements ITileEntityProvider {

    private static final BlockLocator RAIL_LOCATOR =
            BlockLocator.forBlock(Blocks.RAIL).checkAllStates(true);

    protected BlockRailSignal() {
        super(Material.IRON, "rail_signal");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityRailSignal();
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (!RAIL_LOCATOR.touchingHorizontal(worldIn, pos)) {
            return false;
        }
        return super.canPlaceBlockAt(worldIn, pos);
    }

}
