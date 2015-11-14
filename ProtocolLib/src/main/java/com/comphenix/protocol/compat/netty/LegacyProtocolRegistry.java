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
package com.comphenix.protocol.compat.netty;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.netty.ProtocolRegistry;
import com.comphenix.protocol.injector.packet.MapContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * @author dmulloy2
 */

public class LegacyProtocolRegistry extends ProtocolRegistry {
	
	public LegacyProtocolRegistry() {
		super();
	}

	@Override
	protected void initialize() {
		final Object[] protocols = enumProtocol.getEnumConstants();
		List<Map<Integer, Class<?>>> serverMaps = Lists.newArrayList();
		List<Map<Integer, Class<?>>> clientMaps = Lists.newArrayList();
		StructureModifier<Object> modifier = null;
		
		// Result
		Register result = new Register();
		
		for (Object protocol : protocols) {
			if (modifier == null)
				modifier = new StructureModifier<Object>(protocol.getClass().getSuperclass(), false);
			StructureModifier<Map<Integer, Class<?>>> maps = modifier.withTarget(protocol).withType(Map.class);
			
			serverMaps.add(maps.read(0));
			clientMaps.add(maps.read(1));
		}
		// Maps we have to occationally check have changed
		for (Map<Integer, Class<?>> map : Iterables.concat(serverMaps, clientMaps)) {
			result.containers.add(new MapContainer(map));
		}
 		
		// Heuristic - there are more server packets than client packets
		if (sum(clientMaps) > sum(serverMaps)) {
			// Swap if this is violated
			List<Map<Integer, Class<?>>> temp = serverMaps;
			serverMaps = clientMaps;
			clientMaps = temp;
		}
		
		for (int i = 0; i < protocols.length; i++) {
			Enum<?> enumProtocol = (Enum<?>) protocols[i];
			Protocol equivalent = Protocol.fromVanilla(enumProtocol);
			
			// Associate known types
			associatePackets(result, serverMaps.get(i), equivalent, Sender.SERVER);
			associatePackets(result, clientMaps.get(i), equivalent, Sender.CLIENT);
		}

		// Exchange (thread safe, as we have only one writer)
		this.register = result;
	}

	@Override
	protected void associatePackets(Register register, Map<Integer, Class<?>> lookup, Protocol protocol, Sender sender) {
		for (Entry<Integer, Class<?>> entry : lookup.entrySet()) {
			PacketType type = PacketType.fromID(protocol, sender, entry.getKey(), entry.getValue());

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
