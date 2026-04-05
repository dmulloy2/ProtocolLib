package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerInputPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Client.STEER_VEHICLE, new WrappedServerboundPlayerInputPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerInputPacket w = new WrappedServerboundPlayerInputPacket();

        assertEquals(PacketType.Play.Client.STEER_VEHICLE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.STEER_VEHICLE);
        WrappedServerboundPlayerInputPacket wrapper = new WrappedServerboundPlayerInputPacket(container);

        assertEquals(PacketType.Play.Client.STEER_VEHICLE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerInputPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
