package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket("hello", Instant.ofEpochSecond(2000), -1L, null);

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());

        assertEquals("hello", w.getMessage());
        assertEquals(Instant.ofEpochSecond(2000), w.getTimeStamp());
        assertEquals(-1L, w.getSalt());
        assertEquals(null, w.getSignature());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket();

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChatPacket source = new WrappedServerboundChatPacket("hello", Instant.ofEpochSecond(2000), -1L, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatPacket wrapper = new WrappedServerboundChatPacket(container);

        assertEquals("hello", wrapper.getMessage());
        assertEquals(Instant.ofEpochSecond(2000), wrapper.getTimeStamp());
        assertEquals(-1L, wrapper.getSalt());
        assertEquals(null, wrapper.getSignature());

        wrapper.setMessage("modified");
        wrapper.setTimeStamp(Instant.ofEpochSecond(9999));
        wrapper.setSalt(0L);
        wrapper.setSignature(null);

        assertEquals("modified", wrapper.getMessage());
        assertEquals(Instant.ofEpochSecond(9999), wrapper.getTimeStamp());
        assertEquals(0L, wrapper.getSalt());
        assertEquals(null, wrapper.getSignature());

        assertEquals("modified", source.getMessage());
        assertEquals(Instant.ofEpochSecond(9999), source.getTimeStamp());
        assertEquals(0L, source.getSalt());
        assertEquals(null, source.getSignature());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
