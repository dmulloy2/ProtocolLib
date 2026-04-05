package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPingPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPingPacket w = new WrappedClientboundPingPacket(3);

        assertEquals(PacketType.Play.Server.PING, w.getHandle().getType());

        ClientboundPingPacket p = (ClientboundPingPacket) w.getHandle().getHandle();

        assertEquals(3, p.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPingPacket w = new WrappedClientboundPingPacket();

        assertEquals(PacketType.Play.Server.PING, w.getHandle().getType());

        ClientboundPingPacket p = (ClientboundPingPacket) w.getHandle().getHandle();

        assertEquals(0, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPingPacket nmsPacket = new ClientboundPingPacket(3);
        PacketContainer container = new PacketContainer(WrappedClientboundPingPacket.TYPE, nmsPacket);
        WrappedClientboundPingPacket wrapper = new WrappedClientboundPingPacket(container);

        assertEquals(3, wrapper.getId());

        wrapper.setId(9);

        assertEquals(9, nmsPacket.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPingPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
