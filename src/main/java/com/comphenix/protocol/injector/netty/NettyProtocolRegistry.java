/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
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
package com.comphenix.protocol.injector.netty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.injector.packet.MapContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

public class NettyProtocolRegistry extends ProtocolRegistry {

	public NettyProtocolRegistry() {
		super();
	}

	@Override
	protected synchronized void initialize() {
		if (MinecraftVersion.getCurrentVersion().isAtLeast(MinecraftVersion.BEE_UPDATE)) {
			initializeNew();
			return;
		}

		Object[] protocols = enumProtocol.getEnumConstants();

		// ID to Packet class maps
		Map<Object, Map<Integer, Class<?>>> serverMaps = Maps.newLinkedHashMap();
		Map<Object, Map<Integer, Class<?>>> clientMaps = Maps.newLinkedHashMap();

		Register result = new Register();
		StructureModifier<Object> modifier = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (modifier == null) {
				modifier = new StructureModifier<Object>(protocol.getClass().getSuperclass(), false);
			}

			StructureModifier<Map<Object, Map<Integer, Class<?>>>> maps = modifier.withTarget(protocol).withType(Map.class);
			for (Entry<Object, Map<Integer, Class<?>>> entry : maps.read(0).entrySet()) {
				String direction = entry.getKey().toString();
				if (direction.contains("CLIENTBOUND")) { // Sent by Server
					serverMaps.put(protocol, entry.getValue());
				} else if (direction.contains("SERVERBOUND")) { // Sent by Client
					clientMaps.put(protocol, entry.getValue());
				}
			}
		}

		// Maps we have to occasionally check have changed
		for (Object map : serverMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Object map : clientMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Object protocol : protocols) {
			Enum<?> enumProtocol = (Enum<?>) protocol;
			Protocol equivalent = Protocol.fromVanilla(enumProtocol);
			
			// Associate known types
			if (serverMaps.containsKey(protocol))
				associatePackets(result, serverMaps.get(protocol), equivalent, Sender.SERVER);
			if (clientMaps.containsKey(protocol))
				associatePackets(result, clientMaps.get(protocol), equivalent, Sender.CLIENT);
		}

		// Exchange (thread safe, as we have only one writer)
		this.register = result;
	}

	@SuppressWarnings("unchecked")
	private synchronized void initializeNew() {
		Object[] protocols = enumProtocol.getEnumConstants();

		// ID to Packet class maps
		Map<Object, Map<Class<?>, Integer>> serverMaps = Maps.newLinkedHashMap();
		Map<Object, Map<Class<?>, Integer>> clientMaps = Maps.newLinkedHashMap();

		Register result = new Register();
		Field mainMapField = null;
		Field packetMapField = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (mainMapField == null) {
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(protocol.getClass(), true);
				mainMapField = fuzzy.getField(FuzzyFieldContract.newBuilder()
						.banModifier(Modifier.STATIC)
						.requireModifier(Modifier.FINAL)
						.typeDerivedOf(Map.class)
						.build());
				mainMapField.setAccessible(true);
			}

			Map<Object, Object> directionMap;

			try {
				directionMap = (Map<Object, Object>) mainMapField.get(protocol);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Failed to access packet map", ex);
			}

			for (Entry<Object, Object> entry : directionMap.entrySet()) {
				Object holder = entry.getValue();
				if (packetMapField == null) {
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(holder.getClass(), true);
					packetMapField = fuzzy.getField(FuzzyFieldContract.newBuilder()
							.banModifier(Modifier.STATIC)
							.requireModifier(Modifier.FINAL)
							.typeDerivedOf(Map.class)
							.build());
					packetMapField.setAccessible(true);
				}

				Map<Class<?>, Integer> packetMap;

				try {
					packetMap = (Map<Class<?>, Integer>) packetMapField.get(holder);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException("Failed to access packet map", ex);
				}

				String direction = entry.getKey().toString();
				if (direction.contains("CLIENTBOUND")) { // Sent by Server
					serverMaps.put(protocol, packetMap);
				} else if (direction.contains("SERVERBOUND")) { // Sent by Client
					clientMaps.put(protocol, packetMap);
				}
			}
		}

		// Maps we have to occasionally check have changed
		// TODO: Find equivalent in Object2IntMap

		/* for (Object map : serverMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Object map : clientMaps.values()) {
			result.containers.add(new MapContainer(map));
		} */

		for (Object protocol : protocols) {
			Enum<?> enumProtocol = (Enum<?>) protocol;
			Protocol equivalent = Protocol.fromVanilla(enumProtocol);

			// Associate known types
			if (serverMaps.containsKey(protocol)) {
				associatePackets(result, reverse(serverMaps.get(protocol)), equivalent, Sender.SERVER);
			}
			if (clientMaps.containsKey(protocol)) {
				associatePackets(result, reverse(clientMaps.get(protocol)), equivalent, Sender.CLIENT);
			}
		}

		// Exchange (thread safe, as we have only one writer)
		this.register = result;
	}

	/**
	 * Reverses a key->value map to value->key
	 * Non-deterministic behavior when multiple keys are mapped to the same value
	 */
	private <K, V> Map<V, K> reverse(Map<K, V> map) {
		Map<V, K> newMap = new HashMap<>(map.size());
		for (Entry<K, V> entry : map.entrySet()) {
			newMap.put(entry.getValue(), entry.getKey());
		}
		return newMap;
	}

	@Override
	protected void associatePackets(Register register, Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender) {
		for (Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			PacketType type = PacketType.fromCurrent(protocol, sender, entry.getKey(), entry.getValue());
			// System.out.println(Arrays.toString(type.getClassNames()) + " -> " + entry.getValue());

			try {
				register.typeToClass.put(type, entry.getValue());

				if (sender == Sender.SERVER)
					register.serverPackets.add(type);
				if (sender == Sender.CLIENT)
					register.clientPackets.add(type);
			} catch (Exception ex) {
				ProtocolLogger.debug("Encountered an exception associating packet " + type, ex);
			}
		}
	}
}
