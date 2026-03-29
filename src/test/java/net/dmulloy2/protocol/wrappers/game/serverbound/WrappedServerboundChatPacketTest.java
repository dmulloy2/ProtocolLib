package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
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
    void testAllArgsCreate() {
        Instant ts = Instant.ofEpochSecond(1_700_000_000L);
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket("hello world", ts, 42L);

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());

        assertEquals("hello world", w.getMessage());
        assertEquals(ts, w.getTimeStamp());
        assertEquals(42L, w.getSalt());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket();

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());

        assertEquals("", w.getMessage());
        // TODO: should this be null, or the epoch?
        // assertEquals(Instant.EPOCH, w.getTimeStamp());
        assertEquals(0L, w.getSalt());
    }

    @Test
    void testModifyExistingPacket() {
        Instant ts1 = Instant.ofEpochSecond(1_000_000L);
        Instant ts2 = Instant.ofEpochSecond(2_000_000L);

        WrappedServerboundChatPacket src = new WrappedServerboundChatPacket("original", ts1, 1L);
        ServerboundChatPacket nmsPacket = (ServerboundChatPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatPacket wrapper = new WrappedServerboundChatPacket(container);

        assertEquals("original", wrapper.getMessage());
        assertEquals(ts1, wrapper.getTimeStamp());
        assertEquals(1L, wrapper.getSalt());

        wrapper.setMessage("modified");
        wrapper.setTimeStamp(ts2);
        wrapper.setSalt(99L);

        assertEquals("modified", wrapper.getMessage());
        assertEquals(ts2, wrapper.getTimeStamp());
        assertEquals(99L, wrapper.getSalt());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT_COMMAND)));
    }
}
