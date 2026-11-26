package com.comphenix.protocol.injector.packet;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.utility.MinecraftVersion;

import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationPacketTypes;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.cookie.CookiePacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.handshake.HandshakePacketTypes;
import net.minecraft.network.protocol.handshake.HandshakeProtocols;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.login.LoginProtocols;
import net.minecraft.network.protocol.ping.PingPacketTypes;
import net.minecraft.network.protocol.status.StatusPacketTypes;
import net.minecraft.network.protocol.status.StatusProtocols;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PacketRegistryTests {

	@BeforeAll
	public static void beforeAll() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testRegistryInit() {
		PacketRegistry.reset();
		// completing without exception
		PacketRegistry.initialize();
	}

	@Test
	public void testAllPacketsRegistered() {
		List<PacketType> missing = new ArrayList<>();
		for (PacketType type : PacketType.values()) {
			if (type.isDeprecated()
					|| type.getCurrentVersion().compareTo(MinecraftVersion.getCurrentVersion()) > 0) {
				continue;
			}
			if (!PacketRegistry.tryGetPacketClass(type).isPresent()) {
				missing.add(type);
			}
		}
		assertTrue(missing.isEmpty(), "Missing packets: " + missing);
	}

    @Test
    public void testEveryServerProtocolPacketHasDeclaredTypeAndId() throws ReflectiveOperationException {
        // Update the packet type and template lists below when Minecraft adds a protocol phase.
        Map<Object, Class<?>> packetClasses = new HashMap<>();
        for (Class<?> types : List.of(CommonPacketTypes.class, ConfigurationPacketTypes.class,
                CookiePacketTypes.class, GamePacketTypes.class, HandshakePacketTypes.class,
                LoginPacketTypes.class, PingPacketTypes.class, StatusPacketTypes.class)) {
            for (Field field : types.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())
                        && field.getType() == net.minecraft.network.protocol.PacketType.class) {
                    Class<?> packetClass = (Class<?>) ((ParameterizedType) field.getGenericType())
                            .getActualTypeArguments()[0];
                    packetClasses.put(field.get(null), packetClass);
                }
            }
        }

        List<String> errors = new ArrayList<>();
        for (ProtocolInfo.DetailsProvider template : List.of(HandshakeProtocols.SERVERBOUND_TEMPLATE,
                StatusProtocols.SERVERBOUND_TEMPLATE, StatusProtocols.CLIENTBOUND_TEMPLATE,
                LoginProtocols.SERVERBOUND_TEMPLATE, LoginProtocols.CLIENTBOUND_TEMPLATE,
                ConfigurationProtocols.SERVERBOUND_TEMPLATE, ConfigurationProtocols.CLIENTBOUND_TEMPLATE,
                GameProtocols.SERVERBOUND_TEMPLATE, GameProtocols.CLIENTBOUND_TEMPLATE)) {
            ProtocolInfo.Details details = template.details();
            PacketType.Protocol protocol = PacketType.Protocol.fromVanilla(details.id());
            PacketType.Sender sender = details.flow() == PacketFlow.CLIENTBOUND
                    ? PacketType.Sender.SERVER : PacketType.Sender.CLIENT;
            details.listPackets((nmsType, id) -> {
                Class<?> packetClass = packetClasses.get(nmsType);
                PacketType type = packetClass == null ? null : PacketRegistry.getPacketType(protocol, packetClass);
                String description = protocol + " " + nmsType + " (" + id + ")";
                if (type == null || type.isDynamic() || type.isDeprecated()) {
                    errors.add("Missing declared type for " + description);
                } else if (type.getCurrentId() != id || type.getSender() != sender) {
                    errors.add("Incorrect ID or sender for " + description + ": " + type);
                }
            });
        }
        assertTrue(errors.isEmpty(), String.join("\n", errors));
    }
}
