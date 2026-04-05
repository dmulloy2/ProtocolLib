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
    void testAllArgsCreate() {
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket(EnumWrappers.ClientCommand.REQUEST_STATS);

        assertEquals(PacketType.Play.Client.CLIENT_COMMAND, w.getHandle().getType());

        assertEquals(EnumWrappers.ClientCommand.REQUEST_STATS, w.getAction());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundClientCommandPacket w = new WrappedServerboundClientCommandPacket();

        assertEquals(PacketType.Play.Client.CLIENT_COMMAND, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundClientCommandPacket source = new WrappedServerboundClientCommandPacket(EnumWrappers.ClientCommand.REQUEST_STATS);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundClientCommandPacket wrapper = new WrappedServerboundClientCommandPacket(container);

        assertEquals(EnumWrappers.ClientCommand.REQUEST_STATS, wrapper.getAction());

        wrapper.setAction(EnumWrappers.ClientCommand.PERFORM_RESPAWN);

        assertEquals(EnumWrappers.ClientCommand.PERFORM_RESPAWN, wrapper.getAction());

        assertEquals(EnumWrappers.ClientCommand.PERFORM_RESPAWN, source.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientCommandPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
