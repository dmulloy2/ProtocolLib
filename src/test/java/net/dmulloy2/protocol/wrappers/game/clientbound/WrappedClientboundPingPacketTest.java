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
    void testCreate() {
        WrappedClientboundPingPacket w = new WrappedClientboundPingPacket();
        w.setId(42);

        assertEquals(PacketType.Play.Server.PING, w.getHandle().getType());

        ClientboundPingPacket p = (ClientboundPingPacket) w.getHandle().getHandle();

        assertEquals(42, p.getId());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundPingPacket nmsPacket = new ClientboundPingPacket(99);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPingPacket wrapper = new WrappedClientboundPingPacket(container);

        assertEquals(99, wrapper.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPingPacket nmsPacket = new ClientboundPingPacket(99);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPingPacket wrapper = new WrappedClientboundPingPacket(container);

        wrapper.setId(2);

        assertEquals(2, wrapper.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPingPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
