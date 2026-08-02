package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundEntityPositionSyncPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundEntityPositionSyncPacket w = new WrappedClientboundEntityPositionSyncPacket(
                5, position(1, 2, 3, 0, 0), true);
        assertEquals(PacketType.Play.Server.ENTITY_POSITION_SYNC, w.getHandle().getType());
        assertEquals(5, w.getId());
        assertEquals(position(1, 2, 3, 0, 0), w.getValues());
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
                5, position(1, 2, 3, 0, 0), true);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundEntityPositionSyncPacket wrapper = new WrappedClientboundEntityPositionSyncPacket(container);
        assertEquals(5, wrapper.getId());
        assertEquals(position(1, 2, 3, 0, 0), wrapper.getValues());
        assertTrue(wrapper.isOnGround());
        wrapper.setId(9);
        wrapper.setValues(position(10, 20, 30, 270, -45));
        wrapper.setOnGround(false);
        assertEquals(9, wrapper.getId());
        assertEquals(position(10, 20, 30, 270, -45), wrapper.getValues());
        assertFalse(wrapper.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundEntityPositionSyncPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }

    private static WrappedPositionMoveRotation position(
            double x, double y, double z, float yaw, float pitch) {
        return WrappedPositionMoveRotation.create(
                new Vector(x, y, z),
                new Vector(),
                yaw,
                pitch);
    }
}
