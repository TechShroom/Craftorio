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

import java.util.Optional;

import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.block.BlockRailSignal;
import com.techshroom.mods.pereltrains.block.LightValue;
import com.techshroom.mods.pereltrains.segment.Rail;
import com.techshroom.mods.pereltrains.segment.Segment;
import com.techshroom.mods.pereltrains.signal.BlockingState;
import com.techshroom.mods.pereltrains.signal.RailSignal;
import com.techshroom.mods.pereltrains.util.GeneralUtility;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntityRailSignal extends TileEntity implements RailSignal {

    private Segment inSegment = null;

    @Override
    public void onLoad() {
        recalculateSegmentData();
    }

    @Override
    public void recalculateSegmentData() {
        Optional<Segment> seg =
                getRailForThisSignalsSegment().flatMap(Rail::getSegment);
        Segment tmp;
        if (seg.isPresent() && !(tmp = seg.get()).equals(this.inSegment)) {
            if (this.inSegment != null) {
                this.inSegment.removeRailSignal(this);
            }
            tmp.addRailSignal(this);
            this.inSegment = tmp;
        } else if (!seg.isPresent()) {
            if (this.inSegment != null) {
                this.inSegment.removeRailSignal(this);
                this.inSegment = null;
            }
        }
    }

    @Override
    public EnumFacing getControlledDirection() {
        return GeneralUtility
                .getSignalFacing(getWorld().getBlockState(getPos())
                        .getValue(BlockRailSignal.ATTACHED_RAIL_PROPERTY))
                .getOpposite();
    }

    @Override
    public void onStateChange(BlockingState previousState) {
        if (this.inSegment == null) {
            // TODO is this exception worthy?
            PerelTrains.getLogger()
                    .warn("Returning from onStateChange due to null inSegment");
            return;
        }
        switch (this.inSegment.getState()) {
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

    private void setState(LightValue light) {
        World w = getWorld();
        w.setBlockState(getPos(), w.getBlockState(getPos())
                .withProperty(BlockRailSignal.LIGHT_PROPERTY, light));
    }

    // not part of the segment
    @Override
    public Optional<Rail> getRailForEnteringSegment() {
        return getRail(getControlledDirection().getOpposite());
    }

    // part of the segment
    @Override
    public Optional<Rail> getRailForThisSignalsSegment() {
        return getRail(getControlledDirection());
    }

    private Optional<Rail> getRail(EnumFacing wayToRail) {
        BlockPos pos = getPos().offset(wayToRail);
        if (!getWorld().isBlockLoaded(pos)) {
            return Optional.empty();
        }
        TileEntity tileEntity = getWorld().getTileEntity(pos);
        return Optional.ofNullable(tileEntity).filter(Rail.class::isInstance)
                .map(Rail.class::cast);
    }

}
