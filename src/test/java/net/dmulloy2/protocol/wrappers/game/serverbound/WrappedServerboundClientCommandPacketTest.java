package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundClientCommandPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        // NMS constructor takes ServerboundClientCommandPacket$Action; use wrapper-based approach
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket();
        w.setAction(EnumWrappers.ClientCommand.PERFORM_RESPAWN);

        assertEquals(PacketType.Play.Client.CLIENT_COMMAND, w.getHandle().getType());
        assertEquals(EnumWrappers.ClientCommand.PERFORM_RESPAWN, w.getAction());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedServerboundClientCommandPacket src = new WrappedServerboundClientCommandPacket();
        src.setAction(EnumWrappers.ClientCommand.REQUEST_STATS);

        WrappedServerboundClientCommandPacket wrapper = new WrappedServerboundClientCommandPacket(src.getHandle());

        assertEquals(EnumWrappers.ClientCommand.REQUEST_STATS, wrapper.getAction());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket();
        w.setAction(EnumWrappers.ClientCommand.PERFORM_RESPAWN);

        w.setAction(EnumWrappers.ClientCommand.REQUEST_STATS);

        assertEquals(EnumWrappers.ClientCommand.REQUEST_STATS, w.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
