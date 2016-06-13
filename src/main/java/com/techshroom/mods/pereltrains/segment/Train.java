package com.techshroom.mods.pereltrains.segment;

import java.util.Set;

/**
 * Represents a rail train. The train knows what segment(s) it is occupying, and
 * can be told to start and stop.
 */
public interface Train {

    Set<Segment> getSegments();

    void start();

    void stop();

    boolean isAttemptingToMove();

}
