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
package com.techshroom.mods.craftorio.util;

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
