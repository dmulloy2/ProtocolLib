package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket();
        Instant now = Instant.ofEpochSecond(1_700_000_000L);
        w.setMessage("hello world");
        w.setTimeStamp(now);
        w.setSalt(12345L);

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());

        assertEquals("hello world", w.getMessage());
        assertEquals(now, w.getTimeStamp());
        assertEquals(12345L, w.getSalt());
    }

    @Test
    void testReadFromExistingPacket() {
        // NMS constructor is complex; use wrapper-based approach
        WrappedServerboundChatPacket src = new WrappedServerboundChatPacket();
        Instant ts = Instant.ofEpochSecond(1_600_000_000L);
        src.setMessage("test message");
        src.setTimeStamp(ts);
        src.setSalt(9876L);

        WrappedServerboundChatPacket wrapper = new WrappedServerboundChatPacket(src.getHandle());

        assertEquals("test message", wrapper.getMessage());
        assertEquals(ts, wrapper.getTimeStamp());
        assertEquals(9876L, wrapper.getSalt());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket();
        w.setMessage("original");
        w.setTimeStamp(Instant.ofEpochSecond(1_000_000L));
        w.setSalt(1L);

        w.setMessage("modified");

        assertEquals("modified", w.getMessage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatPacket(
                        new PacketContainer(PacketType.Play.Client.ATTACK)));
    }
}
