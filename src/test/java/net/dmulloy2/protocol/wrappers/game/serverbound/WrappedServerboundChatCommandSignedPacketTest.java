package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatCommandSignedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChatCommandSignedPacket w = new WrappedServerboundChatCommandSignedPacket("hello", 42L, Instant.ofEpochSecond(3000));

        assertEquals(PacketType.Play.Client.CHAT_COMMAND_SIGNED, w.getHandle().getType());

        assertEquals("hello", w.getCommand());
        assertEquals(42L, w.getSalt());
        assertEquals(Instant.ofEpochSecond(3000), w.getTimeStamp());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatCommandSignedPacket w = new WrappedServerboundChatCommandSignedPacket();

        assertEquals(PacketType.Play.Client.CHAT_COMMAND_SIGNED, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChatCommandSignedPacket source = new WrappedServerboundChatCommandSignedPacket("hello", 42L, Instant.ofEpochSecond(3000));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatCommandSignedPacket wrapper = new WrappedServerboundChatCommandSignedPacket(container);

        assertEquals("hello", wrapper.getCommand());
        assertEquals(42L, wrapper.getSalt());
        assertEquals(Instant.ofEpochSecond(3000), wrapper.getTimeStamp());

        wrapper.setCommand("modified");
        wrapper.setSalt(-1L);
        wrapper.setTimeStamp(Instant.ofEpochSecond(9999));

        assertEquals("modified", wrapper.getCommand());
        assertEquals(-1L, wrapper.getSalt());
        assertEquals(Instant.ofEpochSecond(9999), wrapper.getTimeStamp());

        assertEquals("modified", source.getCommand());
        assertEquals(-1L, source.getSalt());
        assertEquals(Instant.ofEpochSecond(9999), source.getTimeStamp());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatCommandSignedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
