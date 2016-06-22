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

import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import com.techshroom.mods.pereltrains.WorldAbstraction;
import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface Rail {

    BlockPos getPos();
    
    WorldAbstraction getWorldAbstract();

    void loadSelfIntoGraph();

    Optional<RailSignal> getRailSignalBlocking(EnumFacing travelDir);

    Optional<Segment> getSegment();

    void setSegment(@Nullable Segment segment);

    Map<EnumFacing, Rail> getNeighborRails();

    Optional<Rail> getRail(EnumFacing dir);

}
