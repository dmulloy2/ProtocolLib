package dev.protocollib.api.listener;

/**
 * Representing the priority levels for packet listeners.
 */
public enum PacketListenerPriority {

    /** Lowest priority, executed first. */
    LOWEST, 

    /** Low priority, executed after lowest but before normal. */
    LOW, 

    /** Normal priority, executed after low but before high. */
    NORMAL, 

    /** High priority, executed after normal but before highest. */
    HIGH, 

    /** Highest priority, executed last. */
    HIGHEST;
}
