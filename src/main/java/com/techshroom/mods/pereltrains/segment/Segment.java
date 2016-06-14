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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.signal.BlockingState;
import com.techshroom.mods.pereltrains.signal.RailSignal;
import com.techshroom.mods.pereltrains.util.GraphUtility;

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
    private final UndirectedGraph<Rail, DefaultEdge> railGraph =
            new SimpleGraph<>(DefaultEdge.class);
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

    public void addRail(Rail rail) {
        Map<EnumFacing, Rail> neighbors = rail.getNeighborRails();
        checkArgument(
                neighbors.values().stream()
                        .anyMatch(this.railGraph::containsVertex),
                "rail %s is not connected to this segment", rail);
        for (Rail r : neighbors.values()) {
            this.railGraph.addVertex(r);
            this.railGraph.addEdge(r, rail);
        }
    }

    public void removeRailSignal(RailSignal signal) {
        // TODO merge segments here.
        checkNotNull(signal);
        if (!this.guardingSignals.contains(signal)) {
            PerelTrains.getLogger()
                    .warn("Attempting to remove signal that doesn't exist: "
                            + signal);
            return;
        }
        this.guardingSignals.remove(signal);
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

    /**
     * Breaks this segment into two parts, depending on where the new rail
     * signal is. It returns a segment that is the other side of the signal. It
     * will NOT add the signal to the Segment(s), that is the caller's job.
     * <p>
     * N.B.: The other Segment is sometimes THIS Segment. Consider a simple
     * circular track. If one signal is placed, that doesn't actually break the
     * segment. It only puts a signal on it that will forever block.
     * </p>
     * 
     * @return
     */
    public Segment breakSegment(RailSignal placement) {
        Rail vertex = placement.getAttachedRail();
        checkArgument(this.railGraph.containsVertex(vertex),
                "rail %s is not in this segment", vertex);
        int degree = this.railGraph.degreeOf(vertex);
        if (degree == 0) {
            // No edges -> 1 rail, 1 segment. Return this segment.
            return this;
        }
        if (degree == 1) {
            // 1 edge -> end of rail, 1 segment. Return this segment.
            return this;
        }
        // multiple edges -> segment split! probably!
        UndirectedGraph<Rail, DefaultEdge> splitTester =
                SimpleGraph.<Rail, DefaultEdge> builder(DefaultEdge.class)
                        .addGraph(this.railGraph).build();
        DefaultEdge firstEdge = splitTester.edgesOf(vertex).iterator().next();
        splitTester.removeEdge(firstEdge);
        GraphPath<Rail, DefaultEdge> path =
                new DijkstraShortestPath<>(splitTester,
                        splitTester.getEdgeSource(firstEdge),
                        splitTester.getEdgeTarget(firstEdge)).getPath();
        if (path == null) {
            // Actual split, split the graph and make segments
            Segment created = create();
            GraphUtility.splitUnconnected(this.railGraph, created.railGraph,
                    firstEdge);
            return created;
        } else {
            // just a loop....
            // but do remove the edge, it doesn't exist anymore
            this.railGraph.removeEdge(splitTester.getEdgeSource(firstEdge),
                    splitTester.getEdgeTarget(firstEdge));
            return this;
        }
    }

}
