package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetBeaconPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetBeaconPacket w = new WrappedServerboundSetBeaconPacket(Optional.empty(), Optional.empty());

        assertEquals(PacketType.Play.Client.BEACON, w.getHandle().getType());

        assertEquals(Optional.empty(), w.getPrimary());
        assertEquals(Optional.empty(), w.getSecondary());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetBeaconPacket w = new WrappedServerboundSetBeaconPacket();

        assertEquals(PacketType.Play.Client.BEACON, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetBeaconPacket source = new WrappedServerboundSetBeaconPacket(Optional.empty(), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetBeaconPacket wrapper = new WrappedServerboundSetBeaconPacket(container);

        assertEquals(Optional.empty(), wrapper.getPrimary());
        assertEquals(Optional.empty(), wrapper.getSecondary());

        wrapper.setPrimary(Optional.empty());
        wrapper.setSecondary(Optional.empty());

        assertEquals(Optional.empty(), wrapper.getPrimary());
        assertEquals(Optional.empty(), wrapper.getSecondary());

        assertEquals(Optional.empty(), source.getPrimary());
        assertEquals(Optional.empty(), source.getSecondary());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetBeaconPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
