package com.techshroom.mods.pereltrains.signal;

/**
 * Represents the state of a segment. A segment can be open, expecting, or
 * closed.
 */
public enum BlockingState {

    /**
     * The segment is guarding is ready for a train to enter it.
     */
    OPEN,
    /**
     * The segment is guarding is expecting a train to enter it.
     */
    EXPECTING,
    /**
     * The segment is guarding has a train in it.
     */
    CLOSED;

}
