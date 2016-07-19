/*
 * This file is part of Craftorio, licensed under the MIT License (MIT).
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
package com.techshroom.mods.craftorio.segment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.techshroom.mods.craftorio.block.entity.TileEntityAutoRailBase;
import com.techshroom.mods.craftorio.block.entity.TileEntityRailSignal;
import com.techshroom.mods.craftorio.signal.BlockingState;

/**
 * A segment is a section of a railway that is guarded by one or more signals. A
 * railway with no signals has only one segment.
 */
public final class Segment {

    private static final BitSet usedIds = new BitSet();
    private static final LoadingCache<Integer, Segment> SEGMENT_CACHE =
            CacheBuilder.newBuilder().weakValues()
                    .<Integer, Segment> removalListener(event -> {
                        usedIds.clear(event.getKey());
                    }).build(CacheLoader.from(Segment::new));

    public static Segment get(int id) {
        return SEGMENT_CACHE.getUnchecked(id);
    }

    public static Segment create() {
        return get(usedIds.nextClearBit(0));
    }

    private final Set<TileEntityRailSignal> guardingSignals = new HashSet<>();
    private final Set<TileEntityAutoRailBase> rails = new HashSet<>();
    private final int id;
    private BlockingState state = BlockingState.OPEN;
    private int reservation;

    private Segment(int id) {
        usedIds.set(id);
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void addRailSignal(TileEntityRailSignal signal) {
        checkNotNull(signal);
        checkState(!this.guardingSignals.contains(signal),
                "duplicate signal registration");
        this.guardingSignals.add(signal);
        signal.onStateChange(this.state, this.state);
    }

    public boolean hasSignal(TileEntityRailSignal signal) {
        return this.guardingSignals.contains(signal);
    }

    public void addRail(TileEntityAutoRailBase rail) {
        this.rails.add(rail);
    }

    public void removeRail(TileEntityAutoRailBase rail) {
        this.rails.remove(rail);
    }

    public void removeRailSignal(TileEntityRailSignal signal) {
        this.guardingSignals.remove(signal);
    }

    public Set<TileEntityRailSignal> getGuardingSignals() {
        return this.guardingSignals;
    }

    public Set<TileEntityAutoRailBase> getRails() {
        return this.rails;
    }

    public boolean attemptReserve(int train) {
        if (this.reservation != -1 && this.reservation != train) {
            return false;
        }
        if (this.state != BlockingState.OPEN) {
            return false;
        }
        this.reservation = train;
        setState(BlockingState.EXPECTING);
        return true;
    }

    public void onEnter(int train) {
        if (train != this.reservation) {
            // should we do anything about this???
        }
        setState(BlockingState.CLOSED);
    }

    public BlockingState getState() {
        return this.state;
    }

    private void setState(BlockingState state) {
        BlockingState prev = this.state;
        this.state = checkNotNull(state);
        for (TileEntityRailSignal signal : this.guardingSignals) {
            signal.onStateChange(prev, this.state);
        }
    }

    @Override
    public String toString() {
        return "Segment[id=" + this.id + "]";
    }

}
