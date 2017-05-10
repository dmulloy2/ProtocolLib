/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2016 dmulloy2
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
package com.comphenix.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.server.v1_11_R1.PacketLoginInStart;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.netty.NettyProtocolRegistry;
import com.comphenix.protocol.injector.netty.ProtocolRegistry;

/**
 * @author dmulloy2
 */
public class PacketTypeTest {

	@BeforeClass
	public static void initializeReflection() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testFindCurrent() {
		assertEquals(PacketType.Play.Client.STEER_VEHICLE, PacketType.findCurrent(Protocol.PLAY, Sender.CLIENT, "SteerVehicle"));
	}

	@Test
	public void testLoginStart() {
		// This packet is critical for handleLoin
		assertEquals(PacketLoginInStart.class, PacketType.Login.Client.START.getPacketClass());
	}

	@Test
	public void ensureAllExist() {
		boolean missing = false;
		ProtocolRegistry registry = new NettyProtocolRegistry();
		Map<PacketType, Class<?>> lookup = registry.getPacketTypeLookup();
		for (Entry<PacketType, Class<?>> entry : lookup.entrySet()) {
			PacketType type = entry.getKey();
			Class<?> clazz = entry.getValue();

			if (type.isDynamic()) {
				System.err.println("Packet " + clazz + " does not have a corresponding PacketType!");
				missing = true;
				
			}
			//assertFalse("Packet " + clazz + " does not have a corresponding PacketType!", type.isDynamic());
		}

		assertFalse("There are packets that aren\'t accounted for!", missing);
	}
}
