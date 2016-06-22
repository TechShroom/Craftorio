package com.techshroom.mods.pereltrains.segment;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jgrapht.Graphs;
import org.jgrapht.ListenableGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableUndirectedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.graph.builder.UndirectedGraphBuilder;

import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.techshroom.mods.pereltrains.PerelTrains;
import com.techshroom.mods.pereltrains.signal.RailSignal;
import com.techshroom.mods.pereltrains.util.JGraphXAdapter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class RailGraph extends WorldSavedData {

    private static final String NAME = "railGraph";

    public static Optional<RailGraph> get(World world) {
        if (world.isRemote) {
            return Optional.empty();
        }
        RailGraph data = (RailGraph) world.getPerWorldStorage()
                .getOrLoadData(RailGraph.class, NAME);
        world.loadItemData(RailGraph.class, NAME);
        if (data == null) {
            world.getPerWorldStorage().setData(NAME,
                    data = new RailGraph(NAME));
        }
        return Optional.of(data);
    }

    private final UndirectedGraph<BlockPos, DefaultEdge> rails =
            new ListenableUndirectedGraph<>(
                    new SimpleGraph<>(DefaultEdge.class));
    {
        @SuppressWarnings("unchecked")
        ListenableGraph<BlockPos, DefaultEdge> lG =
                (ListenableGraph<BlockPos, DefaultEdge>) this.rails;
        JGraphXAdapter<BlockPos, DefaultEdge> graph =
                new JGraphXAdapter<BlockPos, DefaultEdge>(lG) {

                    private static final int SIZE = 20;

                    @Override
                    protected mxICell doInsertVertex(BlockPos vertex) {
                        return (mxICell) insertVertex(this.defaultParent, null,
                                vertex, vertex.getX() * (SIZE + SIZE / 2),
                                vertex.getZ() * (SIZE + SIZE / 2), SIZE, SIZE);
                    }

                };

        JFrame frame = new JFrame("RailGraph!");
        frame.add(new mxGraphComponent(graph));
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    // Only used by WorldSavedData and our own #get method.
    @Deprecated
    public RailGraph(String name) {
        super(name);
    }

    public void addRail(Rail rail) {
        this.rails.addVertex(rail.getPos());
        for (Rail neighbor : rail.getNeighborRails().values()) {
            this.rails.addVertex(neighbor.getPos());
            if (!this.rails.containsEdge(rail.getPos(), neighbor.getPos())) {
                this.rails.addEdge(rail.getPos(), neighbor.getPos());
            }
        }
        recalculateSegmentsNearVertex(rail);
    }

    public void removeRail(Rail rail) {
        if (!this.rails.removeVertex(rail.getPos())) {
            PerelTrains.getLogger().info("Unable to remove " + rail.getPos());
        }
        for (Rail neighbor : rail.getNeighborRails().values()) {
            recalculateSegmentsNearVertex(neighbor);
        }
    }

    public void addRailSignal(RailSignal signal) {
        Optional<Rail> enterRail = signal.getRailForEnteringSegment();
        Optional<Rail> exitRail = signal.getRailForThisSignalsSegment();
        Segment previousSingleSegment = enterRail.flatMap(Rail::getSegment)
                .orElseGet(() -> exitRail.flatMap(Rail::getSegment)
                        .orElseThrow(() -> new IllegalStateException(
                                "No segments on rails near signal, wtf?")));
        if (enterRail.isPresent() && exitRail.isPresent()) {
            UndirectedGraph<BlockPos, DefaultEdge> copy =
                    new UndirectedGraphBuilder<>(
                            new SimpleGraph<>(this.rails.getEdgeFactory()))
                                    .addGraph(this.rails).build();
            BlockPos posA = enterRail.get().getPos();
            BlockPos posB = exitRail.get().getPos();
            copy.removeEdge(posA, posB);
            if (new DijkstraShortestPath<>(copy, posA, posB)
                    .getPath() != null) {
                // No segment break occurs, update endpoints
                previousSingleSegment.addRail(enterRail.get());
                previousSingleSegment.addRail(exitRail.get());
            } else {
                // Split segment.
                Segment newA = Segment.create();
                Segment newB = Segment.create();
                
            }
        }
        previousSingleSegment.addRailSignal(signal);
        signal.recalculateSegmentData();
    }

    private void recalculateSegmentsNearVertex(Rail rail) {
        ensureSegment(rail);
        Map<EnumFacing, Rail> railsAway = rail.getNeighborRails();
        for (EnumFacing potentialTravel : railsAway.keySet()) {
            Optional<RailSignal> signal =
                    rail.getRailSignalBlocking(potentialTravel);
            if (signal.isPresent()) {
                // There is a rail signal on this rail. Ensure both sides have
                // unique segments!
                Optional<Rail> railA = signal.get().getRailForEnteringSegment();
                Optional<Segment> segmentA = railA.flatMap(Rail::getSegment);
                Optional<Rail> railB =
                        signal.get().getRailForThisSignalsSegment();
                Optional<Segment> segmentB = railB.flatMap(Rail::getSegment);
                if (segmentA.isPresent() && segmentB.isPresent()) {
                    if (segmentA.equals(segmentB)) {
                        // Matching segments! Re-calculate!
                        mapSegment(railA.get(), potentialTravel.getOpposite());
                        mapSegment(railB.get(), potentialTravel);
                    }
                } else {
                    // Maybe we need to re-calculate some segments?
                    if (!segmentB.isPresent() && railB.isPresent()) {
                        // RailB exists without a segment
                        mapSegment(railB.get(), potentialTravel);
                    }
                    if (!segmentA.isPresent() && railA.isPresent()) {
                        // RailA exists without a segment
                        mapSegment(railA.get(), potentialTravel.getOpposite());
                    }
                }
            } else {
                // No signal :(
                Rail railAway = railsAway.get(potentialTravel);
                ensureSegment(railAway);
                exploreStraight(railAway.getSegment().get(), rail, railAway,
                        new HashSet<>());
            }
        }
        // Finally, check nearby rails and do merging on segments
        if (railsAway.isEmpty()) {
            // Lonely rail. We're done.
            return;
        } else if (railsAway.size() == 1) {
            // One way out.
            Rail first = railsAway.values().iterator().next();
            ensureSegment(first);
            Optional<Segment> firstSeg = first.getSegment();
            if (firstSeg.equals(rail.getSegment())) {
                // A-ok.
                return;
            } else {
                exploreStraight(firstSeg.get(), rail, first, new HashSet<>());
                return;
            }
        } else {
            // Multiple rails, choose one and explore in the other directions
            Segment firstSeg = null;
            for (Map.Entry<EnumFacing, Rail> entry : railsAway.entrySet()) {
                Rail value = entry.getValue();
                if (firstSeg == null) {
                    ensureSegment(value);
                    firstSeg = value.getSegment().get();
                }
                exploreStraight(firstSeg, rail, value, new HashSet<>());
            }
        }
    }

    private void ensureSegment(Rail rail) {
        if (!rail.getSegment().isPresent()) {
            rail.setSegment(Segment.create());
        }
    }

    /**
     * Creates a new segment and applies it to all the Rails it finds.
     * 
     * @param rail
     */
    private void mapSegment(Rail rail, EnumFacing initialDirOfTravel) {
        Optional<RailSignal> signal =
                rail.getRailSignalBlocking(initialDirOfTravel);
        checkState(signal.isPresent(), "missing signal for %s",
                initialDirOfTravel);
        Segment seg = Segment.create();
        seg.addRailSignal(signal.get());
        seg.addRail(rail);
        exploreStraight(seg, rail,
                rail.getRail(initialDirOfTravel).orElse(null), new HashSet<>());
    }

    private void exploreStraight(Segment seg, Rail source, Rail next,
            Set<BlockPos> dejaVuCheck) {
        if (source == null || next == null) {
            return;
        }
        DefaultEdge edge = this.rails.getEdge(source.getPos(), next.getPos());
        if (edge == null) {
            // Nothing to explore!
            return;
        }
        while (true) {
            next.setSegment(seg);
            dejaVuCheck.add(next.getPos());
            dejaVuCheck.add(source.getPos());
            final Rail currentNext = next;
            final Rail currentSource = source;
            Collection<BlockPos> choices =
                    this.rails.edgesOf(next.getPos()).stream()
                            .map(e -> Graphs.getOppositeVertex(this.rails, e,
                                    currentNext.getPos()))
                            .filter(r -> !dejaVuCheck.contains(r))
                            .filter(r -> !r.equals(currentSource.getPos()))
                            .collect(Collectors.toList());
            if (choices.isEmpty()) {
                // End of the line.
                return;
            } else if (choices.size() == 1) {
                BlockPos first = choices.iterator().next();
                Rail atPos = (Rail) source.getWorldAbstract().get(first);
                Map<EnumFacing, Rail> neighborRails = next.getNeighborRails();
                EnumFacing travelDir =
                        neighborRails.entrySet().stream().filter(e -> {
                            return e.getValue().getPos().equals(first);
                        }).map(Map.Entry::getKey).findAny()
                                .orElseThrow(() -> new IllegalStateException(
                                        "No neighbor rail matching " + first
                                                + " in " + neighborRails));
                // Check if it has a signal on it
                if (atPos.getRailSignalBlocking(travelDir).isPresent()) {
                    // Not part of this segment, end of the line.
                    return;
                } else if (atPos.getRailSignalBlocking(travelDir.getOpposite())
                        .isPresent()) {
                    // Part of the segment but end of the line.
                    atPos.setSegment(seg);
                    return;
                }
                // Straight line.
                // Update edge.
                edge = this.rails.getEdge(next.getPos(), first);
                // Update source.
                source = next;
                // Update next.
                next = atPos;
            } else {
                // Branch.
                choices.stream().map(
                        bp -> (Rail) currentSource.getWorldAbstract().get(bp))
                        .forEach(r -> {
                            exploreStraight(seg, currentNext, r, dejaVuCheck);
                        });
                return;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        // TODO
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        // TODO
        return compound;
    }

}
