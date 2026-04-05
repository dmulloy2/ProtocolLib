package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundTeleportToEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Client.SPECTATE, new WrappedServerboundTeleportToEntityPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundTeleportToEntityPacket w = new WrappedServerboundTeleportToEntityPacket();

        assertEquals(PacketType.Play.Client.SPECTATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.SPECTATE);
        WrappedServerboundTeleportToEntityPacket wrapper = new WrappedServerboundTeleportToEntityPacket(container);

        assertEquals(PacketType.Play.Client.SPECTATE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundTeleportToEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
