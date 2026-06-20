package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedLastSeenMessagesUpdate;
import com.comphenix.protocol.wrappers.WrappedMessageSignature;
import java.time.Instant;
import java.util.BitSet;
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
        WrappedLastSeenMessagesUpdate lastSeenMessages = new WrappedLastSeenMessagesUpdate(1, new BitSet(), (byte) 2);
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket(
                "hello", Instant.ofEpochSecond(2000), -1L, null, lastSeenMessages);

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());

        assertEquals("hello", w.getMessage());
        assertEquals(Instant.ofEpochSecond(2000), w.getTimeStamp());
        assertEquals(-1L, w.getSalt());
        assertEquals(null, w.getSignature());
        assertEquals(lastSeenMessages, w.getLastSeenMessages());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatPacket w = new WrappedServerboundChatPacket();

        assertEquals(PacketType.Play.Client.CHAT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedLastSeenMessagesUpdate lastSeenMessages = new WrappedLastSeenMessagesUpdate(1, new BitSet(), (byte) 2);
        WrappedServerboundChatPacket source = new WrappedServerboundChatPacket(
                "hello", Instant.ofEpochSecond(2000), -1L, null, lastSeenMessages);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatPacket wrapper = new WrappedServerboundChatPacket(container);

        assertEquals("hello", wrapper.getMessage());
        assertEquals(Instant.ofEpochSecond(2000), wrapper.getTimeStamp());
        assertEquals(-1L, wrapper.getSalt());
        assertEquals(null, wrapper.getSignature());
        assertEquals(lastSeenMessages, wrapper.getLastSeenMessages());

        wrapper.setMessage("modified");
        wrapper.setTimeStamp(Instant.ofEpochSecond(9999));
        wrapper.setSalt(0L);
        wrapper.setSignature(null);
        WrappedLastSeenMessagesUpdate updatedLastSeenMessages =
                new WrappedLastSeenMessagesUpdate(3, new BitSet(), (byte) 4);
        wrapper.setLastSeenMessages(updatedLastSeenMessages);

        assertEquals("modified", wrapper.getMessage());
        assertEquals(Instant.ofEpochSecond(9999), wrapper.getTimeStamp());
        assertEquals(0L, wrapper.getSalt());
        assertEquals(null, wrapper.getSignature());
        assertEquals(updatedLastSeenMessages, wrapper.getLastSeenMessages());

        assertEquals("modified", source.getMessage());
        assertEquals(Instant.ofEpochSecond(9999), source.getTimeStamp());
        assertEquals(0L, source.getSalt());
        assertEquals(null, source.getSignature());
        assertEquals(updatedLastSeenMessages, source.getLastSeenMessages());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
