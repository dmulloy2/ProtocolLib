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
import java.util.*;
import java.util.Map.Entry;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.Constants;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

import net.minecraft.server.v1_16_R2.EnumProtocol;
import net.minecraft.server.v1_16_R2.EnumProtocolDirection;
import net.minecraft.server.v1_16_R2.PacketLoginInStart;

import org.apache.commons.lang.WordUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author dmulloy2
 */
public class PacketTypeTest {

	@BeforeClass
	public static void beforeClass() {
		// I'm well aware this is jank, but it does in fact work correctly and give the desired result
		PacketType.onDynamicCreate = className -> {
			throw new RuntimeException("Dynamically generated packet " + className);
		};
	}

	@AfterClass
	public static void afterClass() {
		PacketType.onDynamicCreate = __ -> {};
	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Constants.init();

		Set<Class<?>> allTypes = new HashSet<>();
		List<Class<?>> newTypes = new ArrayList<>();

		EnumProtocol[] protocols = EnumProtocol.values();
		for (EnumProtocol protocol : protocols) {
			System.out.println(WordUtils.capitalize(protocol.name().toLowerCase()));

			Field field = EnumProtocol.class.getDeclaredField("h");
			field.setAccessible(true);

			Map<EnumProtocolDirection, Object> map = (Map<EnumProtocolDirection, Object>) field.get(protocol);
			for (Entry<EnumProtocolDirection, Object> entry : map.entrySet()) {
				Field mapField = entry.getValue().getClass().getDeclaredField("a");
				mapField.setAccessible(true);

				Map<Class<?>, Integer> reverseMap = (Map<Class<?>, Integer>) mapField.get(entry.getValue());

				Map<Integer, Class<?>> treeMap = new TreeMap<>();
				for (Entry<Class<?>, Integer> entry1 : reverseMap.entrySet()) {
					treeMap.put(entry1.getValue(), entry1.getKey());
				}

				System.out.println("  " + entry.getKey());
				for (Entry<Integer, Class<?>> entry1 : treeMap.entrySet()) {
					System.out.println(generateNewType(entry1.getKey(), entry1.getValue()));
					allTypes.add(entry1.getValue());

					try {
						PacketType.fromClass(entry1.getValue());
					} catch (Exception ex) {
						newTypes.add(entry1.getValue());
					}
				}
			}
		}

		System.out.println("New types: " + newTypes);

		for (PacketType type : PacketType.values()) {
			if (type.isDeprecated()) {
				continue;
			}

			if (!allTypes.contains(type.getPacketClass())) {
				System.out.println(type + " was removed");
			}
		}
	}

	private static String formatHex(int dec) {
		if (dec < 0) {
			return "0xFF";
		}

		String hex = Integer.toHexString(dec).toUpperCase();
		return "0x" + (hex.length() < 2 ? "0" : "") + hex;
	}

	private static List<String> splitOnCaps(String string) {
		List<String> list = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		char[] chars = string.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i != 0 && Character.isUpperCase(c)) {
				list.add(builder.toString());
				builder = new StringBuilder();
			}

			builder.append(c);
		}

		list.add(builder.toString());
		return list;
	}

	private static String generateNewType(int packetId, Class<?> clazz) {
		StringBuilder builder = new StringBuilder();
		builder.append("\t\t\t");
		builder.append("public static final PacketType ");

		String fullName = clazz.getName();
		fullName = fullName.substring(fullName.lastIndexOf(".") + 1);

		List<String> classNames = new ArrayList<>(2);
		for (String name : fullName.split("\\$")) {
			List<String> split = splitOnCaps(name);
			StringBuilder nameBuilder = new StringBuilder();
			for (int i = 3; i < split.size(); i++) {
				nameBuilder.append(split.get(i));
			}
			classNames.add(nameBuilder.toString());
		}

		String className = classNames.get(classNames.size() - 1);

		// Format it like SET_PROTOCOL
		StringBuilder fieldName = new StringBuilder();
		char[] chars = className.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (i != 0 && Character.isUpperCase(c)) {
				fieldName.append("_");
			}
			fieldName.append(Character.toUpperCase(c));
		}

		builder.append(fieldName.toString().replace("N_B_T", "NBT"));
		builder.append(" = ");

		// Add spacing
		if (builder.length() > 65) {
			builder.append("\n");
		} else {
			while (builder.length() < 65) {
				builder.append(" ");
			}
		}
		builder.append("new ");
		builder.append("PacketType(PROTOCOL, SENDER, ");

		builder.append(formatHex(packetId));
		builder.append(", ");

		StringBuilder nameBuilder = new StringBuilder();
		for (int i = 0; i < classNames.size(); i++) {
			if (i != 0) {
				nameBuilder.append("$");
			}
			nameBuilder.append(classNames.get(i));
		}

		String name = nameBuilder.toString();
		String namesArg = listToString(getAllNames(clazz, name));

		builder.append(namesArg);
		builder.append(");");

		return builder.toString();
	}

	private static List<String> getAllNames(Class<?> packetClass, String newName) {
		List<String> names = new ArrayList<>();
		names.add(newName);

		try {
			PacketType type = PacketType.fromClass(packetClass);
			for (String alias : type.names) {
				if (!names.contains(alias)) {
					names.add(alias);
				}
			}
		} catch (Exception ignored) { }

		return names;
	}

	private static String listToString(List<String> list) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			if (i != 0) {
				builder.append(", ");
			}
			builder.append("\"").append(list.get(i)).append("\"");
		}

		return builder.toString();
	}

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

			Map<EnumProtocolDirection, Object> map = (Map<EnumProtocolDirection, Object>) field.get(protocol);
			for (Entry<EnumProtocolDirection, Object> entry : map.entrySet()) {
				Field mapField = entry.getValue().getClass().getDeclaredField("a");
				mapField.setAccessible(true);

				Map<Class<?>, Integer> reverseMap = (Map<Class<?>, Integer>) mapField.get(entry.getValue());

				Map<Integer, Class<?>> treeMap = new TreeMap<>();
				for (Entry<Class<?>, Integer> entry1 : reverseMap.entrySet()) {
					treeMap.put(entry1.getValue(), entry1.getKey());
				}

				for (Entry<Integer, Class<?>> entry1 : treeMap.entrySet()) {
					try {
						PacketType type = PacketType.fromClass(entry1.getValue());
						if (type.getCurrentId() != entry1.getKey())
							throw new IllegalStateException(
									"Packet ID for " + type + " is incorrect. Expected " + entry1.getKey() + ", but got " + type.getCurrentId());
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
