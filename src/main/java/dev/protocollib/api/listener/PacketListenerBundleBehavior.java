package dev.protocollib.api.listener;

/**
 * Controls packet processing behavior relative to packet bundles (groups of
 * packets processed atomically).
 * <p>
 * Determines whether individual packets should be processed or ignored based on
 * their membership in a packet bundle. Packet bundles typically represent
 * groups of packets that are be processed together as an atomic unit.
 * </p>
 */
public enum PacketListenerBundleBehavior {

    /**
     * Processes all packets unconditionally, regardless of whether they are part of
     * a packet bundle.
     */
    ALWAYS,

    /**
     * Processes only packets that exist outside of packet bundles, ignoring those
     * inside bundles.
     */
    SKIP_INSIDE_BUNDLE,

    /**
     * Processes only packets that are part of packet bundles, ignoring standalone
     * packets.
     */
    SKIP_OUTSIDE_BUNDLE;
}
