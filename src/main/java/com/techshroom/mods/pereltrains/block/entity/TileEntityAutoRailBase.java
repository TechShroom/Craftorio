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

import java.util.HashSet;
import java.util.Set;

import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.PerelBlocks;
import com.techshroom.mods.pereltrains.segment.Segment;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityAutoRailBase extends TileEntity {

    private final Set<TileEntityAutoRailBase> connections = new HashSet<>();
    private Segment segment;

    @Override
    public void onLoad() {
        updateLinks();
    }

    public void updateLinks() {
        if (getWorld().isRemote) {
            return;
        }
        this.connections.clear();
        if (this.segment != null) {
            this.segment.removeRail(this);
        }
        Segment remap = this.segment;
        World world = getWorld();
        if (world.isBlockLoaded(getPos())) {
            for (BlockPos pos : PerelBlocks.NORMAL_RAIL.new ConnectionHelper(
                    world, getPos(), world.getBlockState(getPos()))
                            .getConnectedRails()) {
                if (!world.isBlockLoaded(pos)) {
                    // we'll get it later!
                    continue;
                }
                TileEntityAutoRailBase rail =
                        (TileEntityAutoRailBase) world.getTileEntity(pos);
                if (rail == null) {
                    // skip it, wtf?
                    continue;
                }
                this.connections.add(rail);
                if (remap == null) {
                    remap = rail.getSegment();
                }
                if (rail.getSegment() != remap) {
                    rail.segment = remap;
                } else if (rail.getSegment() == null) {
                    // schedule re-write for later?
                    // idk
                    PerelTrains.getLogger().warn(
                            "Rail with null segment as neighbor: " + rail);
                }
            }
        }
        if (remap == null) {
            remap = Segment.create();
        }
        this.segment = remap;
        this.segment.addRail(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.segment != null) {
            compound.setInteger("segmentId", this.segment.getId());
        }
        return super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("segmentId")) {
            this.segment = Segment.get(compound.getInteger("segmentId"));
            this.segment.addRail(this);
        }
    }

    public Segment getSegment() {
        return this.segment;
    }

    @Override
    public boolean hasFastRenderer() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[segment=" + this.segment + ",pos="
                + getPos() + "]";
    }

}
