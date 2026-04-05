package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundDebugSubscriptionRequestPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST, new WrappedServerboundDebugSubscriptionRequestPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundDebugSubscriptionRequestPacket w = new WrappedServerboundDebugSubscriptionRequestPacket();
        assertEquals(PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST);
        WrappedServerboundDebugSubscriptionRequestPacket wrapper = new WrappedServerboundDebugSubscriptionRequestPacket(container);
        assertEquals(PacketType.Play.Client.DEBUG_SUBSCRIPTION_REQUEST, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundDebugSubscriptionRequestPacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
