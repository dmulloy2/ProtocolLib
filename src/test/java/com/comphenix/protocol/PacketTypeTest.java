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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

import net.minecraft.network.protocol.login.PacketLoginInStart;

/**
 * @author dmulloy2
 */
public class PacketTypeTest {

	private static final Pattern PACKET_PATTERN = Pattern.compile("(?<sender>Serverbound|Clientbound)(?<name>\\w+)Packet");

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
        PacketType.onDynamicCreate = (x, y) -> {
        };
    }

     public static void main(String[] args) throws Exception {
//    public void generateNewPackets() throws Exception {
        BukkitInitialization.initializeAll();

        PacketType.onDynamicCreate = (type, className) -> {
        	String packetTypeClassName = className;

        	Matcher matcher = PACKET_PATTERN.matcher(className);
        	if (matcher.find()) {
        		if (!matcher.group("sender").equals(type.getSender().getMojangName())) {
        			throw new RuntimeException(String.format("wrong packet flow, exepected: %s, got: %s", type.getSender().getMojangName(), matcher.group("sender")));
        		}
        		packetTypeClassName = matcher.group("name");
        	}

        	System.out.printf("%s, %s = new PacketType(PROTOCOL, SENDER, %s, \"%s\") %s\n", type.getProtocol(), type.getSender(), formatHex(type.getCurrentId()), packetTypeClassName, className);
        };

        PacketType.onIdMismatch = (type, newId) -> {
        	System.out.printf("%s, %s, %s %s MISMTACH %s\n", type.getProtocol(), type.getSender(), type.name(), formatHex(type.getCurrentId()), formatHex(newId));
        };
        
        // initialize packet registry
        PacketRegistry.getClientPacketTypes();

        for (PacketType type : PacketType.values()) {
            if (type.isDeprecated()) {
                continue;
            }

            if (type.getPacketClass() == null) {
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
        for (PacketType type : PacketType.values()) {
            PacketType roundTrip = PacketType.findCurrent(type.getProtocol(), type.getSender(), type.names[0]);
            assertEquals(type, roundTrip);
        }
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
    public void ensureRegistryInitializes() throws Exception {
        try {
            PacketType.onDynamicCreate = (type, className) -> {
                throw new RuntimeException("Dynamically generated packet " + className);
            };

            // try to initialize packet registry
            PacketRegistry.getClientPacketTypes();
		} finally {
	        PacketType.onDynamicCreate = (x, y) -> { };
		}
    }

    @Test
    @Disabled // TODO -- lots of constructor parameters :(
    public void testCreateMapChunk() {
        new PacketContainer(PacketType.Play.Server.MAP_CHUNK);
    }

    @Test
    @Disabled // TODO -- ScoreboardObjective parameter in constructor is causing this to fail
    public void testCreateScoreboardObjective() {
        new PacketContainer(PacketType.Play.Server.SCOREBOARD_OBJECTIVE);
    }

    @Test
    @Disabled // TODO -- Entity parameter in constructor is causing this to fail
    public void testCreateEntitySound() {
        new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
    }

	@Test
	public void testPacketCreation() {
		List<PacketType> failed = new ArrayList<>();
		for (PacketType type : PacketType.values()) {
			if (!type.isSupported()) {
				continue;
			}

            if (type == PacketType.Play.Server.ENTITY_SOUND
                || type == PacketType.Play.Server.SCOREBOARD_OBJECTIVE
                || type == PacketType.Play.Server.MAP_CHUNK) {
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
