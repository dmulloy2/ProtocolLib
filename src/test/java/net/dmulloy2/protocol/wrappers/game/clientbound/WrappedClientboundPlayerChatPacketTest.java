package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerChatPacket w = new WrappedClientboundPlayerChatPacket(3, 7, UUID.fromString("00000000-0000-0000-0000-000000000001"), WrappedChatComponent.fromText("Hello, world!"), null);

        assertEquals(PacketType.Play.Server.CHAT, w.getHandle().getType());

        assertEquals(3, w.getGlobalIndex());
        assertEquals(7, w.getIndex());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), w.getSender());
        assertEquals(WrappedChatComponent.fromText("Hello, world!"), w.getUnsignedContent());
        assertEquals(null, w.getSignature());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerChatPacket w = new WrappedClientboundPlayerChatPacket();

        assertEquals(PacketType.Play.Server.CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerChatPacket source = new WrappedClientboundPlayerChatPacket(3, 7, UUID.fromString("00000000-0000-0000-0000-000000000001"), WrappedChatComponent.fromText("Hello, world!"), null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerChatPacket wrapper = new WrappedClientboundPlayerChatPacket(container);

        assertEquals(3, wrapper.getGlobalIndex());
        assertEquals(7, wrapper.getIndex());
        assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), wrapper.getSender());
        assertEquals(WrappedChatComponent.fromText("Hello, world!"), wrapper.getUnsignedContent());
        assertEquals(null, wrapper.getSignature());

        wrapper.setGlobalIndex(9);
        wrapper.setIndex(-5);
        wrapper.setSender(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        wrapper.setUnsignedContent(WrappedChatComponent.fromText("Modified"));
        wrapper.setSignature(null);

        assertEquals(9, wrapper.getGlobalIndex());
        assertEquals(-5, wrapper.getIndex());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getSender());
        assertEquals(WrappedChatComponent.fromText("Modified"), wrapper.getUnsignedContent());
        assertEquals(null, wrapper.getSignature());

        assertEquals(9, source.getGlobalIndex());
        assertEquals(-5, source.getIndex());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getSender());
        assertEquals(WrappedChatComponent.fromText("Modified"), source.getUnsignedContent());
        assertEquals(null, source.getSignature());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerChatPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
