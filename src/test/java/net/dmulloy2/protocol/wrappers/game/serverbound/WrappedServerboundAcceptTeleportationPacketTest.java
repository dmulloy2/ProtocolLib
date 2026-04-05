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
    void testAllArgsCreate() {
        WrappedServerboundAcceptTeleportationPacket w = new WrappedServerboundAcceptTeleportationPacket(3);

        assertEquals(PacketType.Play.Client.TELEPORT_ACCEPT, w.getHandle().getType());

        ServerboundAcceptTeleportationPacket p = (ServerboundAcceptTeleportationPacket) w.getHandle().getHandle();

        assertEquals(3, p.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundAcceptTeleportationPacket w = new WrappedServerboundAcceptTeleportationPacket();

        assertEquals(PacketType.Play.Client.TELEPORT_ACCEPT, w.getHandle().getType());

        ServerboundAcceptTeleportationPacket p = (ServerboundAcceptTeleportationPacket) w.getHandle().getHandle();

        assertEquals(0, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundAcceptTeleportationPacket nmsPacket = new ServerboundAcceptTeleportationPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundAcceptTeleportationPacket wrapper = new WrappedServerboundAcceptTeleportationPacket(container);

        assertEquals(3, wrapper.getId());

        wrapper.setId(9);

        assertEquals(9, nmsPacket.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundAcceptTeleportationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
