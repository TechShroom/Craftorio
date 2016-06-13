package com.techshroom.mods.pereltrains.util;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.UndirectedGraph;

public final class GraphUtility {

    public static <V, E> void splitUnconnected(UndirectedGraph<V, E> source,
            UndirectedGraph<V, E> second, E edgeToRemove) {
        V v1 = source.getEdgeSource(edgeToRemove);
        V v2 = source.getEdgeTarget(edgeToRemove);
        source.removeEdge(v1, v2);
        Deque<V> verticiesToCheck = new LinkedList<>();
        verticiesToCheck.addFirst(v2);
        while (!verticiesToCheck.isEmpty()) {
            V check = verticiesToCheck.pollLast();
            Set<E> edges = source.edgesOf(check);
            if (!edges.isEmpty()) {
                for (E edge : edges) {
                    V opposite = Graphs.getOppositeVertex(source, edge, check);
                    verticiesToCheck.addFirst(opposite);
                    Graphs.addEdgeWithVertices(second, source, edge);
                }
                source.removeAllEdges(edges);
            }
            source.removeVertex(check);
        }
    }

    private GraphUtility() {
    }

}
