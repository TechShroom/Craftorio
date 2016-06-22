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
package com.techshroom.mods.pereltrains.segment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.signal.BlockingState;
import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.util.EnumFacing;

/**
 * A segment is a section of a railway that is guarded by one or more signals. A
 * railway with no signals has only one segment.
 */
public final class Segment {

    private static final BitSet usedIds = new BitSet();
    private static final LoadingCache<Integer, Segment> SEGMENT_CACHE =
            CacheBuilder.newBuilder().maximumSize(50)
                    .<Integer, Segment> removalListener(event -> {
                        usedIds.clear(event.getKey());
                    }).build(CacheLoader.from(Segment::new));

    public static Segment get(int id) {
        return SEGMENT_CACHE.getUnchecked(id);
    }

    public static Segment create() {
        return get(usedIds.nextClearBit(0));
    }

    private final Set<RailSignal> guardingSignals = new HashSet<>();
    private final Set<Rail> endpoints = new HashSet<>();
    private final int id;
    private BlockingState state = BlockingState.OPEN;
    private Train reservation;

    private Segment(int id) {
        usedIds.set(id);
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void addRailSignal(RailSignal signal) {
        checkNotNull(signal);
        checkState(!this.guardingSignals.contains(signal),
                "duplicate signal registration");
        this.guardingSignals.add(signal);
        signal.onStateChange(this.state);
    }

    public boolean hasSignal(RailSignal signal) {
        return this.guardingSignals.contains(signal);
    }

    public void addRail(Rail rail) {
        rail.setSegment(this);
        for (EnumFacing potentialTravel : EnumFacing.HORIZONTALS) {
            if (hasSignal(
                    rail.getRailSignalBlocking(potentialTravel).orElse(null))) {
                this.endpoints.add(rail);
            }
        }
    }

    public void removeRail(Rail rail) {
        rail.setSegment(null);
        if (!this.endpoints.contains(rail)) {
            PerelTrains.getLogger().warn(
                    "Attempting to remove rail that doesn't exist: " + rail);
            return;
        }
        this.endpoints.remove(rail);
    }

    public void removeRailSignal(RailSignal signal) {
        checkNotNull(signal);
        if (!this.guardingSignals.contains(signal)) {
            PerelTrains.getLogger()
                    .warn("Attempting to remove signal that doesn't exist: "
                            + signal);
            return;
        }
        this.guardingSignals.remove(signal);
    }

    public Set<RailSignal> getGuardingSignals() {
        return this.guardingSignals;
    }

    public Set<Rail> getEndpoints() {
        return this.endpoints;
    }

    public boolean attemptReserve(Train train) {
        if (train == null) {
            return false;
        }
        if (this.reservation != null && this.reservation != train) {
            return false;
        }
        this.reservation = train;
        setState(BlockingState.EXPECTING);
        return true;
    }

    public BlockingState getState() {
        return this.state;
    }

    private void setState(BlockingState state) {
        BlockingState prev = this.state;
        this.state = checkNotNull(state);
        for (RailSignal signal : this.guardingSignals) {
            signal.onStateChange(prev);
        }
    }

    @Override
    public String toString() {
        return "Segment[id=" + this.id + "]";
    }

}
