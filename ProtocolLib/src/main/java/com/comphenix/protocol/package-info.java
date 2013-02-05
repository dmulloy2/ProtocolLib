/**
 * Contains classes for retrieving the main {@link ProtocolMananger} object.
 * <p>
 * This allows plugins to reliably and easily read and modify the packet stream of any CraftBukkit-derivative 
 * (or specifically compatible) Minecraft-server. 
 * <p>
 * This manager can be retrieved throught a static method in {@link ProtocolLibrary}:
 * <pre>
 * {@code
 * ProtocolManager manager = ProtocolLibrary.getProtocolManager();
 * }
 * </pre>
 */
package com.comphenix.protocol;