package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import java.util.HashSet;
import java.util.Set;
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
        WrappedClientboundTeleportEntityPacket w = new WrappedClientboundTeleportEntityPacket(3, new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), new HashSet<>(), true);

        assertEquals(PacketType.Play.Server.ENTITY_TELEPORT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), w.getChange());
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
        WrappedClientboundTeleportEntityPacket source = new WrappedClientboundTeleportEntityPacket(3, new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), new HashSet<>(), true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTeleportEntityPacket wrapper = new WrappedClientboundTeleportEntityPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(new WrappedPositionMoveRotation(4.0, 5.0, 6.0, 90.0f, 45.0f), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());
        assertTrue(wrapper.isOnGround());

        wrapper.setEntityId(9);
        wrapper.setChange(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f));
        wrapper.setRelatives(new HashSet<>());
        wrapper.setOnGround(false);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f), wrapper.getChange());
        assertEquals(new HashSet<>(), wrapper.getRelatives());
        assertFalse(wrapper.isOnGround());

        assertEquals(9, source.getEntityId());
        assertEquals(new WrappedPositionMoveRotation(10.0, 20.0, 30.0, 270.0f, -45.0f), source.getChange());
        assertEquals(new HashSet<>(), source.getRelatives());
        assertFalse(source.isOnGround());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTeleportEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
