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
package com.techshroom.mods.pereltrains.block.entity;

import com.techshroom.mods.pereltrains.block.BlockRailSignal;
import com.techshroom.mods.pereltrains.block.LightValue;
import com.techshroom.mods.pereltrains.signal.BlockingState;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;

public class TileEntityRailSignal extends TileEntity {

    @Override
    public void onLoad() {
        recalculateLighting();
    }

    public void recalculateLighting() {
        World w = getWorld();
        EnumFacing attachedDir = w.getBlockState(getPos())
                .getValue(BlockRailSignal.ATTACHED_RAIL_PROPERTY);
        TileEntityAutoRailBase railAttached =
                (TileEntityAutoRailBase) w.getChunkFromBlockCoords(getPos())
                        .getTileEntity(getPos().offset(attachedDir),
                                EnumCreateEntityType.CHECK);
        if (railAttached != null) {
            railAttached.onSignalAttached(this);
        }
    }

    private void setState(LightValue light) {
        World w = getWorld();
        w.setBlockState(getPos(), w.getBlockState(getPos())
                .withProperty(BlockRailSignal.LIGHT_PROPERTY, light));
    }

    public void onStateChange(BlockingState previousState,
            BlockingState newState) {
        switch (newState) {
            case OPEN:
                setState(LightValue.GREEN);
                break;
            case EXPECTING:
                setState(LightValue.YELLOW);
                break;
            case CLOSED:
                setState(LightValue.RED);
                break;
            default:
                setState(LightValue.NONE);
                break;
        }
    }

}
