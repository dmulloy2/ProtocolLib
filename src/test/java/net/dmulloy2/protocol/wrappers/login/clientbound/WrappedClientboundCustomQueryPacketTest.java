package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCustomQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundCustomQueryPacket w = new WrappedClientboundCustomQueryPacket(3, null);

        assertEquals(PacketType.Login.Server.CUSTOM_PAYLOAD, w.getHandle().getType());

        assertEquals(3, w.getTransactionId());
        assertEquals(null, w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCustomQueryPacket w = new WrappedClientboundCustomQueryPacket();

        assertEquals(PacketType.Login.Server.CUSTOM_PAYLOAD, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCustomQueryPacket source = new WrappedClientboundCustomQueryPacket(3, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCustomQueryPacket wrapper = new WrappedClientboundCustomQueryPacket(container);

        assertEquals(3, wrapper.getTransactionId());
        assertEquals(null, wrapper.getPayload());

        wrapper.setTransactionId(9);
        wrapper.setPayload(null);

        assertEquals(9, wrapper.getTransactionId());
        assertEquals(null, wrapper.getPayload());

        assertEquals(9, source.getTransactionId());
        assertEquals(null, source.getPayload());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCustomQueryPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
