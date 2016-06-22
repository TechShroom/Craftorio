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

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.techshroom.mods.pereltrains.WorldAbstraction;
import com.techshroom.mods.pereltrains.WorldAbstractionImpl;
import com.techshroom.mods.pereltrains.segment.Rail;
import com.techshroom.mods.pereltrains.segment.RailGraph;
import com.techshroom.mods.pereltrains.segment.Segment;
import com.techshroom.mods.pereltrains.signal.RailSignal;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class TileEntityAutoRailBase extends TileEntity implements Rail {

    private Segment segment;

    @Override
    public void onLoad() {
        loadSelfIntoGraph();
    }

    @Override
    public void loadSelfIntoGraph() {
        RailGraph.get(getWorld()).ifPresent(g -> g.addRail(this));
    }

    @Override
    public Map<EnumFacing, Rail> getNeighborRails() {
        return generateNeighborRails();
    }

    private Map<EnumFacing, Rail> generateNeighborRails() {
        ImmutableMap.Builder<EnumFacing, Rail> map = ImmutableMap.builder();
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            getRail(facing).ifPresent(r -> map.put(facing, r));
        }
        return map.build();
    }

    @Override
    public Optional<RailSignal> getRailSignalBlocking(EnumFacing travelDir) {
        EnumFacing wayToSignal =
                GeneralUtility.getSignalFacing(travelDir).getOpposite();
        BlockPos pos = getPos().offset(wayToSignal);
        if (!getWorld().isBlockLoaded(pos)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getWorld().getTileEntity(pos))
                .filter(RailSignal.class::isInstance)
                .map(RailSignal.class::cast);
    }

    @Override
    public Optional<Segment> getSegment() {
        return Optional.ofNullable(this.segment);
    }

    @Override
    public void setSegment(Segment segment) {
        this.segment = segment;
    }

    @Override
    public Optional<Rail> getRail(EnumFacing dir) {
        BlockPos pos = getPos().offset(dir);
        if (!getWorld().isBlockLoaded(pos)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getWorld().getTileEntity(pos))
                .filter(Rail.class::isInstance).map(Rail.class::cast);
    }

    @Override
    public WorldAbstraction getWorldAbstract() {
        return WorldAbstractionImpl.get(getWorld());
    }

    @Override
    public String toString() {
        return getClass().getName() + "[segment=" + this.segment + ",pos="
                + getPos() + "]";
    }

}
