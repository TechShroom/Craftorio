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

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.techshroom.mods.pereltrains.segment.Rail;
import com.techshroom.mods.pereltrains.segment.Segment;
import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class TileEntityAutoRailBase extends TileEntity implements Rail {

    private int segmentId = -1;
    private transient Segment segment;

    @Override
    public void onLoad() {
        checkSegment();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.segmentId = compound.getInteger("segmentId");
    }

    public void checkSegment() {
        if (this.segmentId == -1) {
            if (this.segment == null) {
                setSegment(Segment.create());
            }
            this.segmentId = this.segment.getId();
        }
        // Change if no segment or if the ID is different
        if (this.segment != null && this.segment.getId() == this.segmentId) {
            return;
        }
        setSegment(Segment.get(this.segmentId));
    }

    private void setSegment(Segment segment) {
        this.segment = segment;
        //this.segment.addRail(this);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        checkState(this.segmentId != -1, "checkSegment never called!");
        compound.setInteger("segmentId", this.segmentId);
        return super.writeToNBT(compound);
    }

    @Override
    public Map<EnumFacing, Rail> getNeighborRails() {
        return generateNeighborRails();
    }

    private Map<EnumFacing, Rail> generateNeighborRails() {
        ImmutableMap.Builder<EnumFacing, Rail> map = ImmutableMap.builder();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            TileEntity te = getWorld().getTileEntity(getPos().offset(facing));
            if (te instanceof Rail) {
                map.put(facing, (Rail) te);
            }
        }
        return map.build();
    }

    @Override
    public Optional<RailSignal> getSignal(EnumFacing dir) {
        return Optional
                .ofNullable(getWorld().getTileEntity(getPos().offset(dir)))
                .filter(RailSignal.class::isInstance)
                .map(RailSignal.class::cast);
    }

    @Override
    public Segment getSegment() {
        checkSegment();
        return this.segment;
    }

}
