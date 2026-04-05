package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundEntityPositionSyncPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundEntityPositionSyncPacket w = new WrappedClientboundEntityPositionSyncPacket(
                5, new WrappedPositionMoveRotation(1.0, 2.0, 3.0, 0.0f, 0.0f), true);
        assertEquals(PacketType.Play.Server.ENTITY_POSITION_SYNC, w.getHandle().getType());
        assertEquals(5, w.getId());
        assertEquals(new WrappedPositionMoveRotation(1.0, 2.0, 3.0, 0.0f, 0.0f), w.getValues());
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundEntityPositionSyncPacket w = new WrappedClientboundEntityPositionSyncPacket();
        assertEquals(PacketType.Play.Server.ENTITY_POSITION_SYNC, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundEntityPositionSyncPacket src = new WrappedClientboundEntityPositionSyncPacket(
                5, new WrappedPositionMoveRotation(1.0, 2.0, 3.0, 0.0f, 0.0f), true);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundEntityPositionSyncPacket wrapper = new WrappedClientboundEntityPositionSyncPacket(container);
        assertEquals(5, wrapper.getId());
        assertEquals(new WrappedPositionMoveRotation(1.0, 2.0, 3.0, 0.0f, 0.0f), wrapper.getValues());
        assertTrue(wrapper.isOnGround());
        wrapper.setId(9);
        wrapper.setValues(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f));
        wrapper.setOnGround(false);
        assertEquals(9, wrapper.getId());
        assertEquals(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f), wrapper.getValues());
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundEntityPositionSyncPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
