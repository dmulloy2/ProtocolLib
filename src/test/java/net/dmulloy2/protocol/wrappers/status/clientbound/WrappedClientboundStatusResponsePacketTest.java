package net.dmulloy2.protocol.wrappers.status.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundStatusResponsePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundStatusResponsePacket w = new WrappedClientboundStatusResponsePacket((WrappedServerPing) null);

        assertEquals(PacketType.Status.Server.SERVER_INFO, w.getHandle().getType());

        assertEquals(null, w.getStatus());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundStatusResponsePacket w = new WrappedClientboundStatusResponsePacket();

        assertEquals(PacketType.Status.Server.SERVER_INFO, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundStatusResponsePacket source = new WrappedClientboundStatusResponsePacket((WrappedServerPing) null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundStatusResponsePacket wrapper = new WrappedClientboundStatusResponsePacket(container);

        assertEquals(null, wrapper.getStatus());

        wrapper.setStatus(null);

        assertEquals(null, wrapper.getStatus());

        assertEquals(null, source.getStatus());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundStatusResponsePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
