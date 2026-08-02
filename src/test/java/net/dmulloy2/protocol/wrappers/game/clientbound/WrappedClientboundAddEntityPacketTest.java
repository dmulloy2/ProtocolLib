package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.phys.Vec3;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAddEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket(3, UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), EntityType.PIG, 3.14, 100.0, -2.5, new Vector(1.0, 2.0, 3.0), (byte) 1, (byte) 7, (byte) 3, 7);

        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), w.getEntityUUID());
        assertEquals(EntityType.PIG, w.getEntityType());
        assertEquals(3.14, w.getX(), 1e-9);
        assertEquals(100.0, w.getY(), 1e-9);
        assertEquals(-2.5, w.getZ(), 1e-9);
        assertEquals(new Vector(1.0, 2.0, 3.0), w.getVelocity());
        assertEquals((byte) 1, w.getPitchByte());
        assertEquals((byte) 7, w.getYawByte());
        assertEquals((byte) 3, w.getHeadYawByte());
        assertEquals(7, w.getData());
    }

    @Test
    void testEntityTypeRoundTrip() {
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket();

        w.setEntityType(EntityType.PIG);
        assertEquals(EntityType.PIG, w.getEntityType());

        w.setEntityType(EntityType.ARMOR_STAND);
        assertEquals(EntityType.ARMOR_STAND, w.getEntityType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket();

        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        // Build the underlying NMS packet directly via its canonical constructor so we
        // exercise the real network-wire signature, then wrap it.
        ClientboundAddEntityPacket nmsPacket = new ClientboundAddEntityPacket(
                3, UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"),
                3.14, 100.0, -2.5,
                /* xRot */ WrappedClientboundAddEntityPacket.byteToAngle((byte) 1),
                /* yRot */ WrappedClientboundAddEntityPacket.byteToAngle((byte) 7),
                net.minecraft.world.entity.EntityTypes.PIG, 7, new Vec3(1.0, 2.0, 3.0),
                /* yHeadRot */ WrappedClientboundAddEntityPacket.byteToAngle((byte) 3));
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAddEntityPacket wrapper = new WrappedClientboundAddEntityPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), wrapper.getEntityUUID());
        assertEquals(EntityType.PIG, wrapper.getEntityType());
        assertEquals(3.14, wrapper.getX(), 1e-9);
        assertEquals(100.0, wrapper.getY(), 1e-9);
        assertEquals(-2.5, wrapper.getZ(), 1e-9);
        assertEquals(new Vector(1.0, 2.0, 3.0), wrapper.getVelocity());
        assertEquals((byte) 1, wrapper.getPitchByte());
        assertEquals((byte) 7, wrapper.getYawByte());
        assertEquals((byte) 3, wrapper.getHeadYawByte());
        assertEquals(7, wrapper.getData());

        wrapper.setEntityId(9);
        wrapper.setEntityUUID(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        wrapper.setEntityType(EntityType.ARMOR_STAND);
        wrapper.setX(100.0);
        wrapper.setY(2.71);
        wrapper.setZ(-5.0);
        wrapper.setVelocity(new Vector(10.0, 20.0, 30.0));
        wrapper.setPitchByte((byte) -1);
        wrapper.setYawByte((byte) 0);
        wrapper.setHeadYawByte((byte) 15);
        wrapper.setData(0);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getEntityUUID());
        assertEquals(EntityType.ARMOR_STAND, wrapper.getEntityType());
        assertEquals(100.0, wrapper.getX(), 1e-9);
        assertEquals(2.71, wrapper.getY(), 1e-9);
        assertEquals(-5.0, wrapper.getZ(), 1e-9);
        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getVelocity());
        assertEquals((byte) -1, wrapper.getPitchByte());
        assertEquals((byte) 0, wrapper.getYawByte());
        assertEquals((byte) 15, wrapper.getHeadYawByte());
        assertEquals(0, wrapper.getData());
    }

    @Test
    void testAngleConversionsMatchNmsSignedPacking() {
        assertEquals(-180.0f, WrappedClientboundAddEntityPacket.byteToAngle((byte) -128));
        assertEquals(-1.40625f, WrappedClientboundAddEntityPacket.byteToAngle((byte) -1));
        assertEquals((byte) -1, WrappedClientboundAddEntityPacket.angleToByte(-1.0f));
        assertEquals((byte) -128, WrappedClientboundAddEntityPacket.angleToByte(-180.0f));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAddEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
