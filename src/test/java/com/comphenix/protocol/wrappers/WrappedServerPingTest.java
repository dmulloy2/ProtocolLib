package com.comphenix.protocol.wrappers;

import java.io.IOException;
import java.util.Optional;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftProtocolVersion;
import com.comphenix.protocol.wrappers.WrappedServerPing.CompressedImage;
import com.google.common.io.Resources;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedServerPingTest {

    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    @Disabled // TODO MotD is null
    public void fullTest() throws IOException  {
        PacketContainer packet = new PacketContainer(PacketType.Status.Server.SERVER_INFO);
        Optional<WrappedServerPing> optionalPing = packet.getServerPings().optionRead(0);
        assertTrue(optionalPing.isPresent());

        WrappedServerPing serverPing = optionalPing.get();
        assertNotNull(serverPing.getMotD());
        assertNotNull(serverPing.getFavicon());
        assertNotNull(serverPing.getPlayers());
        assertNotNull(serverPing.getVersionName());

        CompressedImage tux = CompressedImage.fromPng(Resources.getResource("tux.png").openStream());
        byte[] original = tux.getDataCopy();

        serverPing.setMotD("Hello, this is a test.");
        serverPing.setPlayersOnline(5);
        serverPing.setPlayersMaximum(10);
        serverPing.setVersionName("Minecraft 123");
        serverPing.setVersionProtocol(4);
        serverPing.setFavicon(tux);
        serverPing.setEnforceSecureChat(true);

        packet.getServerPings().write(0, serverPing);

        WrappedServerPing roundTrip = packet.getServerPings().read(0);

        String asJson = serverPing.toJson();
        WrappedServerPing deserialized = WrappedServerPing.fromJson(asJson);
        deserialized.setMotD(serverPing.getMotD());

        // Deserializing to JSON and parsing the JSON again can lead to a different object as the Mojang Datafixer optimizes server icons or reorders components in the server description
        assertEquals(serverPing.getVersionName(), deserialized.getVersionName(), "Failed to serialize as JSON and deserialize afterwards (version name mismatch)");
        assertEquals(serverPing.getVersionProtocol(), deserialized.getVersionProtocol(), "Failed to serialize as JSON and deserialize afterwards (version protocol mismatch)");
        assertEquals(serverPing.getPlayersOnline(), deserialized.getPlayersOnline(), "Failed to serialize as JSON and deserialize afterwards (players online mismatch)");
        assertEquals(serverPing.getPlayersMaximum(), deserialized.getPlayersMaximum(), "Failed to serialize as JSON and deserialize afterwards (players maximum mismatch)");
        assertEquals(serverPing.getPlayers(), deserialized.getPlayers(), "Failed to serialize as JSON and deserialize afterwards (player sample mismatch)");
        assertEquals(PlainTextComponentSerializer.plainText().serialize(AdventureComponentConverter.fromWrapper(serverPing.getMotD())), PlainTextComponentSerializer.plainText().serialize(AdventureComponentConverter.fromWrapper(deserialized.getMotD()))); // Check if plain text is equivalent

        assertEquals(5, roundTrip.getPlayersOnline());
        assertEquals(10, roundTrip.getPlayersMaximum());
        assertEquals("Minecraft 123", roundTrip.getVersionName());
        assertEquals(4, roundTrip.getVersionProtocol());
        assertTrue(roundTrip.isEnforceSecureChat());

        assertArrayEquals(original, roundTrip.getFavicon().getData());

        CompressedImage copy = CompressedImage.fromBase64Png(Base64Coder.encodeLines(tux.getData()));
        assertArrayEquals(copy.getData(), roundTrip.getFavicon().getData());
    }

    @Test
    public void testDefaultData() {
        PacketContainer packet = new PacketContainer(PacketType.Status.Server.SERVER_INFO);
        packet.getServerPings().write(0, new WrappedServerPing());

        WrappedServerPing serverPing = packet.getServerPings().read(0);
        assertEquals(serverPing.getMotD(), WrappedChatComponent.fromLegacyText("A Minecraft Server"));
        assertEquals(serverPing.getVersionProtocol(), MinecraftProtocolVersion.getCurrentVersion());
    }

    @Test
    public void testSetPartialData() {
        PacketContainer packet = new PacketContainer(PacketType.Status.Server.SERVER_INFO);

        WrappedServerPing serverPing = new WrappedServerPing();
        serverPing.setPlayersOnline(69);
        serverPing.setPlayersMaximum(420);

        packet.getServerPings().write(0, serverPing);

        WrappedServerPing roundTrip = packet.getServerPings().read(0);
        assertEquals(roundTrip.getMotD(), WrappedChatComponent.fromLegacyText("A Minecraft Server"));
        assertEquals(roundTrip.getVersionProtocol(), MinecraftProtocolVersion.getCurrentVersion());
        assertEquals(roundTrip.getPlayersOnline(), 69);
        assertEquals(roundTrip.getPlayersMaximum(), 420);
    }
}
