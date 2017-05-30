/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 * Copyright (C) 2016 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 */
package com.comphenix.protocol;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.packet.PacketRegistry;

import net.minecraft.server.v1_12_R1.EnumProtocol;
import net.minecraft.server.v1_12_R1.EnumProtocolDirection;
import net.minecraft.server.v1_12_R1.PacketLoginInStart;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

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
	public void testDeprecation() {
		assertTrue("Packet isn't properly deprecated", PacketType.Status.Server.OUT_SERVER_INFO.isDeprecated());
		assertTrue("Deprecated packet isn't properly included",
				PacketRegistry.getServerPacketTypes().contains(PacketType.Status.Server.OUT_SERVER_INFO));
		assertFalse("Packet isn't properly deprecated", PacketType.Play.Server.CHAT.isDeprecated());
		assertEquals("Deprecated packets aren't equal", PacketType.Status.Server.OUT_SERVER_INFO,
				PacketType.Status.Server.SERVER_INFO);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ensureTypesAreCorrect() throws Exception {
		boolean fail = false;

		EnumProtocol[] protocols = EnumProtocol.values();
		for (EnumProtocol protocol : protocols) {
			Field field = EnumProtocol.class.getDeclaredField("h");
			field.setAccessible(true);

			Map<EnumProtocolDirection, Map<Integer, Class<?>>> map = (Map<EnumProtocolDirection, Map<Integer, Class<?>>>) field.get(protocol);
			for (Entry<EnumProtocolDirection, Map<Integer, Class<?>>> entry : map.entrySet()) {
				Map<Integer, Class<?>> treeMap = new TreeMap<>(entry.getValue());
				for (Entry<Integer, Class<?>> entry1 : treeMap.entrySet()) {
					try {
						PacketType type = PacketType.fromClass(entry1.getValue());
						if (type.getCurrentId() != entry1.getKey())
							throw new IllegalStateException("Packet ID for " + type + " is incorrect. Expected " + entry1.getKey() + ", but got " + type.getCurrentId());
					} catch (Throwable ex) {
						ex.printStackTrace();
						fail = true;
					}
				}
			}
		}

		assertTrue("Packet type(s) were incorrect!", !fail);
	}
}
