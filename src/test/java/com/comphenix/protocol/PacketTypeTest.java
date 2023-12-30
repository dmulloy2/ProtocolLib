/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2016 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftReflectionTestUtil;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.network.protocol.login.PacketLoginInStart;
import org.apache.commons.lang.WordUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author dmulloy2
 */
public class PacketTypeTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();

        // I'm well aware this is jank, but it does in fact work correctly and give the desired result
        /* PacketType.onDynamicCreate = className -> {
            throw new RuntimeException("Dynamically generated packet " + className);
        }; */
    }

    @AfterAll
    public static void afterClass() {
        PacketType.onDynamicCreate = __ -> {
        };
    }

    @SuppressWarnings("unchecked")
    // @Test
    // public static void main(String[] args) throws Exception {
    public void generateNewPackets() throws Exception {
        MinecraftReflectionTestUtil.init();

        Set<Class<?>> allTypes = new HashSet<>();
        List<Class<?>> newTypes = new ArrayList<>();

        EnumProtocol[] protocols = EnumProtocol.values();
        for (EnumProtocol protocol : protocols) {
            System.out.println(WordUtils.capitalize(protocol.name().toLowerCase()));

            Field field = EnumProtocol.class.getDeclaredField("k");
            field.setAccessible(true);

            Map<EnumProtocolDirection, Object> map = (Map<EnumProtocolDirection, Object>) field.get(protocol);
            for (Entry<EnumProtocolDirection, Object> entry : map.entrySet()) {
                Field mapField = entry.getValue().getClass().getDeclaredField("b");
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

        String className;
        List<String> classNames = new ArrayList<>();

        if (fullName.endsWith("Packet")) {
            for (String name : fullName.split("\\$")) {
                List<String> split = splitOnCaps(name);
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 1; i < split.size() - 1; i++) {
                    nameBuilder.append(split.get(i));
                }
                classNames.add(nameBuilder.toString());
            }
        } else {
            for (String name : fullName.split("\\$")) {
                List<String> split = splitOnCaps(name);
                StringBuilder nameBuilder = new StringBuilder();
                for (int i = 3; i < split.size(); i++) {
                    nameBuilder.append(split.get(i));
                }
                classNames.add(nameBuilder.toString());
            }
        }

        className = classNames.get(classNames.size() - 1);

        PacketType existing = null;
        try {
            existing = PacketType.fromClass(clazz);
            if (existing.isDynamic()) {
                existing = null;
            }
        } catch (Exception ignored) {
            // doesn't exist
        }

        String fieldName;
        if (existing == null) {
            // Format it like SET_PROTOCOL
            StringBuilder fieldNameBuilder = new StringBuilder();
            char[] chars = className.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (i != 0 && Character.isUpperCase(c)) {
                    fieldNameBuilder.append("_");
                }
                fieldNameBuilder.append(Character.toUpperCase(c));
            }

            fieldName = fieldNameBuilder.toString().replace("N_B_T", "NBT");
        } else {
            fieldName = existing.name();
        }

        builder.append(fieldName);
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
                alias = alias.substring(alias.lastIndexOf('.') + 1);
                if (!names.contains(alias)) {
                    names.add(alias);
                }
            }
        } catch (Exception ignored) {
        }

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

    @Test
    public void testFindCurrent() {
        assertEquals(PacketType.Play.Client.STEER_VEHICLE,
                PacketType.findCurrent(Protocol.PLAY, Sender.CLIENT, "SteerVehicle"));
    }

    @Test
    public void testLoginStart() {
        // This packet is critical for handleLoin
        assertEquals(PacketLoginInStart.class, PacketType.Login.Client.START.getPacketClass());
    }

    @Test
    public void testDeprecation() {
        assertTrue(PacketType.Status.Server.OUT_SERVER_INFO.isDeprecated(), "Packet isn't properly deprecated");
        assertTrue(PacketRegistry.getServerPacketTypes().contains(PacketType.Status.Server.OUT_SERVER_INFO),
                "Deprecated packet isn't properly included");
        assertFalse(PacketType.Play.Server.CHAT.isDeprecated(), "Packet isn't properly deprecated");
        assertEquals(PacketType.Status.Server.OUT_SERVER_INFO, PacketType.Status.Server.SERVER_INFO,
                "Deprecated packets aren't equal");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ensureTypesAreCorrect() throws Exception {
        PacketType.onDynamicCreate = className -> {
            throw new RuntimeException("Dynamically generated packet " + className);
        };

        try {
            boolean fail = false;

            EnumProtocol[] protocols = EnumProtocol.values();
            for (EnumProtocol protocol : protocols) {
                Field field = EnumProtocol.class.getDeclaredField("h");
                field.setAccessible(true);

                Map<EnumProtocolDirection, Object> map = (Map<EnumProtocolDirection, Object>) field.get(protocol);
                for (Entry<EnumProtocolDirection, Object> entry : map.entrySet()) {
                    Field holderField = entry.getValue().getClass().getDeclaredField("c");
                    holderField.setAccessible(true);

                    Object holder = holderField.get(entry.getValue());
                    Field mapField = holder.getClass().getDeclaredField("b");
                    mapField.setAccessible(true);

                    Map<Class<?>, Integer> reverseMap = (Map<Class<?>, Integer>) mapField.get(holder);

                    Map<Integer, Class<?>> treeMap = new TreeMap<>();
                    for (Entry<Class<?>, Integer> entry1 : reverseMap.entrySet()) {
                        treeMap.put(entry1.getValue(), entry1.getKey());
                    }

                    for (Entry<Integer, Class<?>> entry1 : treeMap.entrySet()) {
                        try {
                            PacketType type = PacketType.fromClass(entry1.getValue());
                            if (type.getCurrentId() != entry1.getKey()) {
                                throw new IllegalStateException(
                                        "Packet ID for " + type + " is incorrect. Expected " + entry1.getKey() + ", but got "
                                                + type.getCurrentId());
                            }
                        } catch (Throwable ex) {
                            if (ex.getMessage().contains("BundleDelimiterPacket")) {
                                continue;
                            }

                            ex.printStackTrace();
                            fail = true;
                        }
                    }
                }
            }

            assertFalse(fail, "Packet type(s) were incorrect!");
        } finally {
            PacketType.onDynamicCreate = __ -> { };
        }
    }

	@Test
	public void testPacketCreation() {
		List<PacketType> failed = new ArrayList<>();
		for (PacketType type : PacketType.values()) {
			if (!type.isSupported()) {
				continue;
			}

			try {
				new PacketContainer(type);
			} catch (Exception ex) {
				failed.add(type);
			}
		}
		assertTrue(failed.isEmpty(), "Failed to create: " + failed);
	}

    @Test
    public void testPacketBundleWriting() {
        PacketContainer bundlePacket = new PacketContainer(PacketType.Play.Server.BUNDLE);
        assertEquals(MinecraftReflection.getPackedBundlePacketClass().orElseThrow(() -> new IllegalStateException("Packet Bundle class is not present")), bundlePacket.getHandle().getClass());
        List<PacketContainer> bundle = new ArrayList<>();

        PacketContainer chatMessage = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        chatMessage.getChatComponents().write(0, WrappedChatComponent.fromText("Test"));
        chatMessage.getBooleans().write(0, false);
        bundle.add(chatMessage);
        bundlePacket.getPacketBundles().write(0, bundle);
    }
}
