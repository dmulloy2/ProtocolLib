package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundAcceptTeleportationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundAcceptTeleportationPacket w = new WrappedServerboundAcceptTeleportationPacket();
        w.setId(42);

        assertEquals(PacketType.Play.Client.TELEPORT_ACCEPT, w.getHandle().getType());

        ServerboundAcceptTeleportationPacket p = (ServerboundAcceptTeleportationPacket) w.getHandle().getHandle();

        assertEquals(42, p.getId());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundAcceptTeleportationPacket nmsPacket = new ServerboundAcceptTeleportationPacket(7);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAcceptTeleportationPacket wrapper = new WrappedServerboundAcceptTeleportationPacket(container);

        assertEquals(7, wrapper.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundAcceptTeleportationPacket nmsPacket = new ServerboundAcceptTeleportationPacket(7);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAcceptTeleportationPacket wrapper = new WrappedServerboundAcceptTeleportationPacket(container);

        wrapper.setId(99);

        assertEquals(99, wrapper.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundAcceptTeleportationPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
