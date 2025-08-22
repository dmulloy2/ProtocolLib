package com.comphenix.protocol.injector;

import com.comphenix.protocol.ProtocolManager;

/**
 * Yields access to the internal hook configuration.
 *
 * @author Kristian
 */
public interface InternalManager extends ProtocolManager {

    /**
     * Called when ProtocolLib is closing.
     */
    void close();

    /**
     * Determine if debug mode is enabled.
     *
     * @return TRUE if it is, FALSE otherwise.
     */
    boolean isDebug();

    /**
     * Set whether or not debug mode is enabled.
     *
     * @param debug - TRUE if it is, FALSE otherwise.
     */
    void setDebug(boolean debug);
}
