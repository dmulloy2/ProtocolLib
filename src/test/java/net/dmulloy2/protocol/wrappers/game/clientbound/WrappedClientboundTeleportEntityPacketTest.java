package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import java.util.HashSet;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTeleportEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTeleportEntityPacket w =
                new WrappedClientboundTeleportEntityPacket(3, position(4, 5, 6, 90, 45), new HashSet<>(), true);

        assertEquals(PacketType.Play.Server.ENTITY_TELEPORT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(position(4, 5, 6, 90, 45), w.getChange());
        assertEquals(new HashSet<>(), w.getRelatives());
        assertTrue(w.isOnGround());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTeleportEntityPacket w = new WrappedClientboundTeleportEntityPacket();

        assertEquals(PacketType.Play.Server.ENTITY_TELEPORT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTeleportEntityPacket source =
                new WrappedClientboundTeleportEntityPacket(3, position(4, 5, 6, 90, 45), new HashSet<>(), true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTeleportEntityPacket wrapper = new WrappedClientboundTeleportEntityPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(position(4, 5, 6, 90, 45), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());
        assertTrue(wrapper.isOnGround());

        wrapper.setEntityId(9);
        wrapper.setChange(position(10, 20, 30, 270, -45));
        wrapper.setRelatives(new HashSet<>());
        wrapper.setOnGround(false);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(position(10, 20, 30, 270, -45), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());
        assertFalse(wrapper.isOnGround());

        assertEquals(9, source.getEntityId());
        assertEquals(position(10, 20, 30, 270, -45), source.getChange());
        assertEquals(new HashSet<>(), source.getRelatives());
        assertFalse(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTeleportEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
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
