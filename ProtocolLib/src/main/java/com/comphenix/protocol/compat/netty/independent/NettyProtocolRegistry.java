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
package com.comphenix.protocol.compat.netty.independent;

import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.netty.ProtocolRegistry;
import com.comphenix.protocol.injector.packet.MapContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Maps;

/**
 * @author dmulloy2
 */

public class NettyProtocolRegistry extends ProtocolRegistry {
	
	@Override
	protected synchronized void initialize() {
		Object[] protocols = enumProtocol.getEnumConstants();

		// ID to Packet class maps
		Map<Object, Map<Integer, Class<?>>> serverMaps = Maps.newLinkedHashMap();
		Map<Object, Map<Integer, Class<?>>> clientMaps = Maps.newLinkedHashMap();

		Register result = new Register();
		StructureModifier<Object> modifier = null;

		// Iterate through the protocols
		for (Object protocol : protocols) {
			if (modifier == null)
				modifier = new StructureModifier<Object>(protocol.getClass().getSuperclass(), false);
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

		// Maps we have to occationally check have changed
		for (Map<Integer, Class<?>> map : serverMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (Map<Integer, Class<?>> map : clientMaps.values()) {
			result.containers.add(new MapContainer(map));
		}

		for (int i = 0; i < protocols.length; i++) {
			Object protocol = protocols[i];
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

	@Override
	protected void associatePackets(Register register, Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender) {
		for (Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			PacketType type = PacketType.fromCurrent(protocol, sender, entry.getKey(), entry.getValue());

			try {
				register.typeToClass.put(type, entry.getValue());

				if (sender == Sender.SERVER)
					register.serverPackets.add(type);
				if (sender == Sender.CLIENT)
					register.clientPackets.add(type);
			} catch (IllegalArgumentException ex) {
				// Sometimes this happens with fake packets, just ignore it
			}
		}
	}
}
