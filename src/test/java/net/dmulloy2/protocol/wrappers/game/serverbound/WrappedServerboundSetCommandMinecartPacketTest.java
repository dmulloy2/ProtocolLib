package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetCommandMinecartPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetCommandMinecartPacket w = new WrappedServerboundSetCommandMinecartPacket(3, "world", true);

        assertEquals(PacketType.Play.Client.SET_COMMAND_MINECART, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals("world", w.getCommand());
        assertTrue(w.isTrackOutput());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetCommandMinecartPacket w = new WrappedServerboundSetCommandMinecartPacket();

        assertEquals(PacketType.Play.Client.SET_COMMAND_MINECART, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetCommandMinecartPacket source = new WrappedServerboundSetCommandMinecartPacket(3, "world", true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCommandMinecartPacket wrapper = new WrappedServerboundSetCommandMinecartPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals("world", wrapper.getCommand());
        assertTrue(wrapper.isTrackOutput());

        wrapper.setEntityId(9);
        wrapper.setCommand("hello");
        wrapper.setTrackOutput(false);

        assertEquals(9, wrapper.getEntityId());
        assertEquals("hello", wrapper.getCommand());
        assertFalse(wrapper.isTrackOutput());

        assertEquals(9, source.getEntityId());
        assertEquals("hello", source.getCommand());
        assertFalse(source.isTrackOutput());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCommandMinecartPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
