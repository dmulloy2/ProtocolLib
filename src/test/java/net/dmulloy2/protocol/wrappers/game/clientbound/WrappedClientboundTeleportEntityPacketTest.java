package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.RelativeArgument;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTeleportEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    // -------------------------------------------------------------------------
    // 1. Packet creation
    // -------------------------------------------------------------------------

    @Test
    void testCreate() {
        WrappedClientboundTeleportEntityPacket wrapper = new WrappedClientboundTeleportEntityPacket();

        wrapper.setEntityId(42);

        WrappedPositionMoveRotation change = new WrappedPositionMoveRotation(1.5, 64.0, -3.5, 90.0f, -15.0f);
        wrapper.setChange(change);

        Set<RelativeArgument> relatives = EnumSet.of(RelativeArgument.X, RelativeArgument.Y, RelativeArgument.Z);
        wrapper.setRelatives(relatives);

        wrapper.setOnGround(true);

        // Verify round-trip on the same object
        assertEquals(42, wrapper.getEntityId());

        WrappedPositionMoveRotation readBack = wrapper.getChange();
        assertEquals(1.5,   readBack.getX(),    1e-6);
        assertEquals(64.0,  readBack.getY(),    1e-6);
        assertEquals(-3.5,  readBack.getZ(),    1e-6);
        assertEquals(90.0f,  readBack.getYaw(),   1e-4f);
        assertEquals(-15.0f, readBack.getPitch(), 1e-4f);

        assertTrue(wrapper.getRelatives().contains(RelativeArgument.X));
        assertTrue(wrapper.getRelatives().contains(RelativeArgument.Y));
        assertTrue(wrapper.getRelatives().contains(RelativeArgument.Z));
        assertFalse(wrapper.getRelatives().contains(RelativeArgument.Y_ROT));

        assertTrue(wrapper.isOnGround());
        assertNotNull(wrapper.getHandle());
        assertEquals(PacketType.Play.Server.ENTITY_TELEPORT, wrapper.getHandle().getType());
    }

    // -------------------------------------------------------------------------
    // 2. Reading from an existing PacketContainer
    // -------------------------------------------------------------------------

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
        raw.getModifier().writeDefaults();

        raw.getIntegers().write(0, 7);
        raw.getBooleans().write(0, false);
        raw.getPositionMoveRotations().write(0, new WrappedPositionMoveRotation(10.0, 80.0, 5.0, 45.0f, 0.0f));
        raw.getSets(EnumWrappers.getRelativeArgumentConverter()).write(0, EnumSet.of(RelativeArgument.DELTA_X));

        WrappedClientboundTeleportEntityPacket wrapper = new WrappedClientboundTeleportEntityPacket(raw);

        assertEquals(7, wrapper.getEntityId());
        assertFalse(wrapper.isOnGround());

        WrappedPositionMoveRotation readBack = wrapper.getChange();
        assertEquals(10.0, readBack.getX(),    1e-6);
        assertEquals(80.0, readBack.getY(),    1e-6);
        assertEquals(5.0,  readBack.getZ(),    1e-6);
        assertEquals(45.0f, readBack.getYaw(),   1e-4f);
        assertEquals(0.0f,  readBack.getPitch(), 1e-4f);

        assertTrue(wrapper.getRelatives().contains(RelativeArgument.DELTA_X));
        assertFalse(wrapper.getRelatives().contains(RelativeArgument.X));
    }

    // -------------------------------------------------------------------------
    // 3. Modifying an existing packet
    // -------------------------------------------------------------------------

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTeleportEntityPacket wrapper = new WrappedClientboundTeleportEntityPacket();
        wrapper.setEntityId(99);
        wrapper.setChange(new WrappedPositionMoveRotation(0.0, 0.0, 0.0, 0.0f, 0.0f));
        wrapper.setRelatives(EnumSet.noneOf(RelativeArgument.class));
        wrapper.setOnGround(false);

        // Second wrapper around the same handle
        WrappedClientboundTeleportEntityPacket modified =
                new WrappedClientboundTeleportEntityPacket(wrapper.getHandle());

        modified.setEntityId(100);
        modified.setChange(new WrappedPositionMoveRotation(-5.0, 100.0, 20.0, 180.0f, 30.0f));
        modified.setRelatives(EnumSet.of(RelativeArgument.Y_ROT, RelativeArgument.X_ROT));
        modified.setOnGround(true);

        // Both wrappers share the same handle – changes must be visible on the original
        assertEquals(100, wrapper.getEntityId());
        assertTrue(wrapper.isOnGround());

        WrappedPositionMoveRotation pos = wrapper.getChange();
        assertEquals(-5.0,  pos.getX(),    1e-6);
        assertEquals(100.0, pos.getY(),    1e-6);
        assertEquals(20.0,  pos.getZ(),    1e-6);
        assertEquals(180.0f, pos.getYaw(),   1e-4f);
        assertEquals(30.0f,  pos.getPitch(), 1e-4f);

        Set<RelativeArgument> rel = wrapper.getRelatives();
        assertTrue(rel.contains(RelativeArgument.Y_ROT));
        assertTrue(rel.contains(RelativeArgument.X_ROT));
        assertFalse(rel.contains(RelativeArgument.X));
    }

    // -------------------------------------------------------------------------
    // 4. Constructor rejects wrong packet type
    // -------------------------------------------------------------------------

    @Test
    void testWrongPacketTypeThrows() {
        PacketContainer wrong = new PacketContainer(PacketType.Play.Server.CHAT);
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTeleportEntityPacket(wrong));
    }
}
