package com.techshroom.mods.pereltrains.segment;

import java.util.Map;
import java.util.Optional;

import com.techshroom.mods.pereltrains.signal.RailSignal;

import net.minecraft.util.EnumFacing;

public interface Rail {
    
    Segment getSegment();

    Map<EnumFacing, Rail> getNeighborRails();

    Optional<RailSignal> getSignal(EnumFacing dir);

}
