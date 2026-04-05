package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetCommandBlockPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetCommandBlockPacket w = new WrappedServerboundSetCommandBlockPacket("hello", false, true, true, new BlockPosition(4, 5, 6));

        assertEquals(PacketType.Play.Client.SET_COMMAND_BLOCK, w.getHandle().getType());

        assertEquals("hello", w.getCommand());
        assertFalse(w.isTrackOutput());
        assertTrue(w.isConditional());
        assertTrue(w.isAutomatic());
        assertEquals(new BlockPosition(4, 5, 6), w.getPos());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetCommandBlockPacket w = new WrappedServerboundSetCommandBlockPacket();

        assertEquals(PacketType.Play.Client.SET_COMMAND_BLOCK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetCommandBlockPacket source = new WrappedServerboundSetCommandBlockPacket("hello", false, true, true, new BlockPosition(4, 5, 6));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCommandBlockPacket wrapper = new WrappedServerboundSetCommandBlockPacket(container);

        assertEquals("hello", wrapper.getCommand());
        assertFalse(wrapper.isTrackOutput());
        assertTrue(wrapper.isConditional());
        assertTrue(wrapper.isAutomatic());
        assertEquals(new BlockPosition(4, 5, 6), wrapper.getPos());

        wrapper.setCommand("modified");
        wrapper.setTrackOutput(true);
        wrapper.setConditional(false);
        wrapper.setAutomatic(false);
        wrapper.setPos(new BlockPosition(10, 20, 30));

        assertEquals("modified", wrapper.getCommand());
        assertTrue(wrapper.isTrackOutput());
        assertFalse(wrapper.isConditional());
        assertFalse(wrapper.isAutomatic());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());

        assertEquals("modified", source.getCommand());
        assertTrue(source.isTrackOutput());
        assertFalse(source.isConditional());
        assertFalse(source.isAutomatic());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCommandBlockPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
