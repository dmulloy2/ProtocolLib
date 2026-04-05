package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundCustomQueryAnswerPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundCustomQueryAnswerPacket w = new WrappedServerboundCustomQueryAnswerPacket(3, null);

        assertEquals(PacketType.Login.Client.CUSTOM_PAYLOAD, w.getHandle().getType());

        assertEquals(3, w.getTransactionId());
        assertEquals(null, w.getPayload());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCustomQueryAnswerPacket w = new WrappedServerboundCustomQueryAnswerPacket();

        assertEquals(PacketType.Login.Client.CUSTOM_PAYLOAD, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundCustomQueryAnswerPacket source = new WrappedServerboundCustomQueryAnswerPacket(3, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundCustomQueryAnswerPacket wrapper = new WrappedServerboundCustomQueryAnswerPacket(container);

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
                () -> new WrappedServerboundCustomQueryAnswerPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
