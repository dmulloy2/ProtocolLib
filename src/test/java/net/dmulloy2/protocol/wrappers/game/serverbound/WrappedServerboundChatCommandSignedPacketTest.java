package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedLastSeenMessagesUpdate;
import java.time.Instant;
import java.util.BitSet;
import java.util.List;
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
        WrappedServerboundChatCommandSignedPacket.ArgumentSignatures argumentSignatures =
                new WrappedServerboundChatCommandSignedPacket.ArgumentSignatures(List.of());
        WrappedLastSeenMessagesUpdate lastSeenMessages = new WrappedLastSeenMessagesUpdate(1, new BitSet(), (byte) 2);
        WrappedServerboundChatCommandSignedPacket w = new WrappedServerboundChatCommandSignedPacket(
                "hello", 42L, Instant.ofEpochSecond(3000), argumentSignatures, lastSeenMessages);

        assertEquals(PacketType.Play.Client.CHAT_COMMAND_SIGNED, w.getHandle().getType());

        assertEquals("hello", w.getCommand());
        assertEquals(42L, w.getSalt());
        assertEquals(Instant.ofEpochSecond(3000), w.getTimeStamp());
        assertEquals(argumentSignatures, w.getArgumentSignatures());
        assertEquals(lastSeenMessages, w.getLastSeenMessages());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChatCommandSignedPacket w = new WrappedServerboundChatCommandSignedPacket();

        assertEquals(PacketType.Play.Client.CHAT_COMMAND_SIGNED, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChatCommandSignedPacket.ArgumentSignatures argumentSignatures =
                new WrappedServerboundChatCommandSignedPacket.ArgumentSignatures(List.of());
        WrappedLastSeenMessagesUpdate lastSeenMessages = new WrappedLastSeenMessagesUpdate(1, new BitSet(), (byte) 2);
        WrappedServerboundChatCommandSignedPacket source = new WrappedServerboundChatCommandSignedPacket(
                "hello", 42L, Instant.ofEpochSecond(3000), argumentSignatures, lastSeenMessages);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundChatCommandSignedPacket wrapper = new WrappedServerboundChatCommandSignedPacket(container);

        assertEquals("hello", wrapper.getCommand());
        assertEquals(42L, wrapper.getSalt());
        assertEquals(Instant.ofEpochSecond(3000), wrapper.getTimeStamp());
        assertEquals(argumentSignatures, wrapper.getArgumentSignatures());
        assertEquals(lastSeenMessages, wrapper.getLastSeenMessages());

        wrapper.setCommand("modified");
        wrapper.setSalt(-1L);
        wrapper.setTimeStamp(Instant.ofEpochSecond(9999));
        WrappedServerboundChatCommandSignedPacket.ArgumentSignatures updatedArgumentSignatures =
                new WrappedServerboundChatCommandSignedPacket.ArgumentSignatures(List.of());
        WrappedLastSeenMessagesUpdate updatedLastSeenMessages =
                new WrappedLastSeenMessagesUpdate(3, new BitSet(), (byte) 4);
        wrapper.setArgumentSignatures(updatedArgumentSignatures);
        wrapper.setLastSeenMessages(updatedLastSeenMessages);

        assertEquals("modified", wrapper.getCommand());
        assertEquals(-1L, wrapper.getSalt());
        assertEquals(Instant.ofEpochSecond(9999), wrapper.getTimeStamp());
        assertEquals(updatedArgumentSignatures, wrapper.getArgumentSignatures());
        assertEquals(updatedLastSeenMessages, wrapper.getLastSeenMessages());

        assertEquals("modified", source.getCommand());
        assertEquals(-1L, source.getSalt());
        assertEquals(Instant.ofEpochSecond(9999), source.getTimeStamp());
        assertEquals(updatedArgumentSignatures, source.getArgumentSignatures());
        assertEquals(updatedLastSeenMessages, source.getLastSeenMessages());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChatCommandSignedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
