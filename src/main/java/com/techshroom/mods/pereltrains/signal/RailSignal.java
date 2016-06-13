package com.techshroom.mods.pereltrains.signal;

import com.techshroom.mods.pereltrains.segment.Rail;

import net.minecraft.util.EnumFacing;

public interface RailSignal {

    EnumFacing getControlledDirection();

    Rail getAttachedRail();

    void onStateChange(BlockingState previousState);

}
