/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */

package com.comphenix.protocol.injector.packet;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.compat.netty.LegacyProtocolRegistry;
import com.comphenix.protocol.compat.netty.independent.NettyProtocolRegistry;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.injector.netty.ProtocolRegistry;
import com.comphenix.protocol.injector.packet.LegacyPacketRegistry.InsufficientPacketsException;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.TroveWrapper.CannotFindTroveNoEntryValue;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Static packet registry in Minecraft.
 * @author Kristian
 */
@SuppressWarnings("rawtypes")
public class PacketRegistry {
	public static final ReportType REPORT_CANNOT_CORRECT_TROVE_MAP = new ReportType("Unable to correct no entry value.");
	
	public static final ReportType REPORT_INSUFFICIENT_SERVER_PACKETS = new ReportType("Too few server packets detected: %s");
	public static final ReportType REPORT_INSUFFICIENT_CLIENT_PACKETS = new ReportType("Too few client packets detected: %s");

	// Two different packet registry
	private static volatile LegacyPacketRegistry LEGACY;
	private static volatile ProtocolRegistry NETTY;
	
	// Cached for legacy
	private static volatile Set<PacketType> NETTY_SERVER_PACKETS;
	private static volatile Set<PacketType> NETTY_CLIENT_PACKETS;
	
	// Cached for Netty
	private static volatile Set<Integer> LEGACY_SERVER_PACKETS;
	private static volatile Set<Integer> LEGACY_CLIENT_PACKETS;
	private static volatile Map<Integer, Class> LEGACY_PREVIOUS_PACKETS;
	
	// Whether or not the registry has
	private static boolean INITIALIZED;
	
	/**
	 * Initialize the packet registry.
	 */
	private static void initialize() {
		if (INITIALIZED) {
			// Make sure they were initialized
			if (NETTY == null && LEGACY == null)
				throw new IllegalStateException("No initialized registry.");
			return;
		}
	
		// Check for netty
		if (MinecraftReflection.isUsingNetty()) {
			if (NETTY == null) {
				if (MinecraftVersion.getCurrentVersion().isAtLeast(MinecraftVersion.BOUNTIFUL_UPDATE)) {
					NETTY = new NettyProtocolRegistry();
				} else {
					NETTY = new LegacyProtocolRegistry();
				}
			}
		} else {
			initializeLegacy();
		}
	}

	/**
	 * Determine if the given packet type is supported on the current server.
	 * @param type - the type to check.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public static boolean isSupported(PacketType type) {
		initialize();
		
		if (NETTY != null)
			return NETTY.getPacketTypeLookup().containsKey(type);
		
		// Look up the correct type
		return type.isClient() ?
			LEGACY.getClientPackets().contains(type.getLegacyId()) :
			LEGACY.getServerPackets().contains(type.getLegacyId());
	}
	
	/**
	 * Initialize the legacy packet registry.
	 */
	private static void initializeLegacy() {
		if (LEGACY == null) {
			try {
				LEGACY = new LegacyPacketRegistry();
				LEGACY.initialize();
			} catch (InsufficientPacketsException e) {
				if (e.isClient()) {
					ProtocolLibrary.getErrorReporter().reportWarning(
						PacketRegistry.class, Report.newBuilder(REPORT_INSUFFICIENT_CLIENT_PACKETS).messageParam(e.getPacketCount())
					);
				} else {
					ProtocolLibrary.getErrorReporter().reportWarning(
						PacketRegistry.class, Report.newBuilder(REPORT_INSUFFICIENT_SERVER_PACKETS).messageParam(e.getPacketCount())
					);
				}
			} catch (CannotFindTroveNoEntryValue e) {
				ProtocolLibrary.getErrorReporter().reportWarning(PacketRegistry.class,
					Report.newBuilder(REPORT_CANNOT_CORRECT_TROVE_MAP).error(e.getCause()));
			}
		}
	}
	
	/**
	 * Retrieve a map of every packet class to every ID.
	 * <p>
	 * Deprecated: Use {@link #getPacketToType()} instead.
	 * @return A map of packet classes and their corresponding ID.
	 */
	@Deprecated
	public static Map<Class, Integer> getPacketToID() {
		initialize();
		
		if (NETTY != null) {
			@SuppressWarnings("unchecked")
			Map<Class, Integer> result = (Map)Maps.transformValues(NETTY.getPacketClassLookup(), new Function<PacketType, Integer>() {
				@Override
				public Integer apply(PacketType type) {
					return type.getLegacyId();
				};
			});
			return result;
		}
		return LEGACY.getPacketToID();
	}
	
	/**
	 * Retrieve a map of every packet class to the respective packet type.
	 * @return A map of packet classes and their corresponding packet type.
	 */
	public static Map<Class, PacketType> getPacketToType() {
		initialize();
	
		if (NETTY != null) {
			@SuppressWarnings("unchecked")
			Map<Class, PacketType> result = (Map)NETTY.getPacketClassLookup();
			return result;
		}
		return Maps.transformValues(LEGACY.getPacketToID(), new Function<Integer, PacketType>() {
			@Override
			public PacketType apply(Integer packetId) {
				return PacketType.findLegacy(packetId);
			};
		});
	}
	
	/**
	 * Retrieve the injected proxy classes handlig each packet ID.
	 * <p>
	 * This is not supported in 1.7.2 and later.
	 * @return Injected classes.
	 */
	@Deprecated
	public static Map<Integer, Class> getOverwrittenPackets() {
		initialize();
		
		if (LEGACY != null)
			return LEGACY.getOverwrittenPackets();
		throw new IllegalStateException("Not supported on Netty.");
	}
	
	/**
	 * Retrieve the vanilla classes handling each packet ID.
	 * @return Vanilla classes.
	 */
	@Deprecated
	public static Map<Integer, Class> getPreviousPackets() {
		initialize();
		
		if (NETTY != null) {
			// Construct it first
			if (LEGACY_PREVIOUS_PACKETS == null) {
				Map<Integer, Class> map = Maps.newHashMap();
				
				for (Entry<PacketType, Class<?>> entry : NETTY.getPacketTypeLookup().entrySet()) {
					map.put(entry.getKey().getLegacyId(), entry.getValue());
				}
				LEGACY_PREVIOUS_PACKETS = Collections.unmodifiableMap(map);
			}
			return LEGACY_PREVIOUS_PACKETS;
		}
		return LEGACY.getPreviousPackets();
	}
	
	/**
	 * Retrieve every known and supported server packet.
	 * <p>
	 * Deprecated: Use {@link #getServerPacketTypes()} instead.
	 * @return An immutable set of every known server packet.
	 * @throws FieldAccessException If we're unable to retrieve the server packet data from Minecraft.
	 */
	@Deprecated
	public static Set<Integer> getServerPackets() throws FieldAccessException {
		initialize();
		
		if (NETTY != null) {
			if (LEGACY_SERVER_PACKETS == null) {
				LEGACY_SERVER_PACKETS = toLegacy(NETTY.getServerPackets());
			}
			return LEGACY_SERVER_PACKETS;
		}
		return LEGACY.getServerPackets();
	}
	
	/**
	 * Retrieve every known and supported server packet type.
	 * @return Every server packet type.
	 */
	public static Set<PacketType> getServerPacketTypes() {
		initialize();
		
		if (NETTY != null) {
			NETTY.synchronize();
			return NETTY.getServerPackets();
		}
		
		// Handle legacy
		if (NETTY_SERVER_PACKETS == null) {
			NETTY_SERVER_PACKETS = toPacketTypes(LEGACY.getServerPackets(), Sender.SERVER);
		}
		return NETTY_SERVER_PACKETS;
	}
	
	/**
	 * Retrieve every known and supported client packet.
	 * <p>
	 * Deprecated: Use {@link #getClientPacketTypes()} instead.
	 * @return An immutable set of every known client packet.
	 * @throws FieldAccessException If we're unable to retrieve the client packet data from Minecraft.
	 */
	@Deprecated
	public static Set<Integer> getClientPackets() throws FieldAccessException {
		initialize();
		
		if (NETTY != null) {
			if (LEGACY_CLIENT_PACKETS == null) {
				LEGACY_CLIENT_PACKETS = toLegacy(NETTY.getClientPackets());
			}
			return LEGACY_CLIENT_PACKETS;
		}
		return LEGACY.getClientPackets();
	}
	
	/**
	 * Retrieve every known and supported server packet type.
	 * @return Every server packet type.
	 */
	public static Set<PacketType> getClientPacketTypes() {
		initialize();
		
		if (NETTY != null) {
			NETTY.synchronize();
			return NETTY.getClientPackets();
		}
		
		// Handle legacy
		if (NETTY_CLIENT_PACKETS == null) {
			NETTY_CLIENT_PACKETS = toPacketTypes(LEGACY.getClientPackets(), Sender.CLIENT);
		}
		return NETTY_CLIENT_PACKETS;
	}
	
	/**
	 * Convert a set of packet types to a set of integers based on the legacy packet ID.
	 * @param types - packet type.
	 * @return Set of integers.
	 */
	public static Set<Integer> toLegacy(Set<PacketType> types) {
		Set<Integer> result = Sets.newHashSet();
		
		for (PacketType type : types)
			result.add(type.getLegacyId());
		return Collections.unmodifiableSet(result);
	}
	
	/**
	 * Convert a set of legacy packet IDs to packet types.
	 * @param ids - legacy packet IDs.
	 * @return Set of packet types.
	 */
	public static Set<PacketType> toPacketTypes(Set<Integer> ids) {
		return toPacketTypes(ids, null);
	}
	
	/**
	 * Convert a set of legacy packet IDs to packet types.
	 * @param ids - legacy packet IDs.
	 * @param preference - the sender preference, if any.
	 * @return Set of packet types.
	 */
	public static Set<PacketType> toPacketTypes(Set<Integer> ids, Sender preference) {
		Set<PacketType> result = Sets.newHashSet();
		
		for (int id : ids)
			result.add(PacketType.fromLegacy(id, preference));
		return Collections.unmodifiableSet(result);
	}
	
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * <p>
	 * Deprecated: Use {@link #getPacketClassFromType(PacketType)} instead.
	 * @param packetID - the packet ID.
	 * @return The associated class.
	 */
	@Deprecated
	public static Class getPacketClassFromID(int packetID) {
		initialize();
		
		if (NETTY != null)
			return NETTY.getPacketTypeLookup().get(PacketType.findLegacy(packetID));
		return LEGACY.getPacketClassFromID(packetID);
	}
	
	/**
	 * Retrieves the correct packet class from a given type.
	 * @param type - the packet type.
	 * @return The associated class.
	 */
	public static Class getPacketClassFromType(PacketType type) {
		return getPacketClassFromType(type, false);
	}
	
	/**
	 * Retrieves the correct packet class from a given type.
	 * <p>
	 * Note that forceVanillla will be ignored on MC 1.7.2 and later.
	 * @param type - the packet type.
	 * @param forceVanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 */
	public static Class getPacketClassFromType(PacketType type, boolean forceVanilla) {
		initialize();
		
		if (NETTY != null)
			return NETTY.getPacketTypeLookup().get(type);
		return LEGACY.getPacketClassFromID(type.getLegacyId(), forceVanilla);
	}
	
	/**
	 * Retrieves the correct packet class from a given packet ID.
	 * <p>
	 * This method has been deprecated.
	 * @param packetID - the packet ID.
 	 * @param forceVanilla - whether or not to look for vanilla classes, not injected classes.
	 * @return The associated class.
	 */
	@Deprecated
	public static Class getPacketClassFromID(int packetID, boolean forceVanilla) {
		initialize();
		
		if (LEGACY != null)
			return LEGACY.getPacketClassFromID(packetID, forceVanilla);
		return getPacketClassFromID(packetID);
	}
	
	/**
	 * Retrieve the packet ID of a given packet.
	 * <p>
	 * Deprecated: Use {@link #getPacketType(Class)}.
	 * @param packet - the type of packet to check.
	 * @return The legacy ID of the given packet.
	 * @throws IllegalArgumentException If this is not a valid packet.
	 */
	@Deprecated
	public static int getPacketID(Class<?> packet) {
		initialize();
		
		if (NETTY != null)
			return NETTY.getPacketClassLookup().get(packet).getLegacyId();
		return LEGACY.getPacketID(packet);
	}
	
	/**
	 * Retrieve the packet type of a given packet.
	 * @param packet - the class of the packet.
	 * @return The packet type, or NULL if not found.
	 */
	public static PacketType getPacketType(Class<?> packet) {
		return getPacketType(packet, null);
	}
	
	/**
	 * Retrieve the packet type of a given packet.
	 * @param packet - the class of the packet.
	 * @param sender - the sender of the packet, or NULL.
	 * @return The packet type, or NULL if not found.
	 */
	public static PacketType getPacketType(Class<?> packet, Sender sender) {
		initialize();
		
		if (NETTY != null) {
			return NETTY.getPacketClassLookup().get(packet);
		} else {
			final int id = LEGACY.getPacketID(packet);
			
			return PacketType.hasLegacy(id) ?
					PacketType.fromLegacy(id, sender) : null;
		}
	}
}
