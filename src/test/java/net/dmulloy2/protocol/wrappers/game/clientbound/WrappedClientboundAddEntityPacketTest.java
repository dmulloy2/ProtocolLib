package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.UUID;
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
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket(3, UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), null, 3.14, 100.0, -2.5, new Vector(1.0, 2.0, 3.0), (byte) 1, (byte) 7, (byte) 3, 7);

        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), w.getEntityUUID());
        assertEquals(null, w.getEntityType());
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
    void testNoArgsCreate() {
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket();

        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundAddEntityPacket source = new WrappedClientboundAddEntityPacket(3, UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), null, 3.14, 100.0, -2.5, new Vector(1.0, 2.0, 3.0), (byte) 1, (byte) 7, (byte) 3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAddEntityPacket wrapper = new WrappedClientboundAddEntityPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(UUID.fromString("abcdef01-2345-6789-abcd-ef0123456789"), wrapper.getEntityUUID());
        assertEquals(null, wrapper.getEntityType());
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
        wrapper.setEntityType(null);
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
        assertEquals(null, wrapper.getEntityType());
        assertEquals(100.0, wrapper.getX(), 1e-9);
        assertEquals(2.71, wrapper.getY(), 1e-9);
        assertEquals(-5.0, wrapper.getZ(), 1e-9);
        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getVelocity());
        assertEquals((byte) -1, wrapper.getPitchByte());
        assertEquals((byte) 0, wrapper.getYawByte());
        assertEquals((byte) 15, wrapper.getHeadYawByte());
        assertEquals(0, wrapper.getData());

        assertEquals(9, source.getEntityId());
        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getEntityUUID());
        assertEquals(null, source.getEntityType());
        assertEquals(100.0, source.getX(), 1e-9);
        assertEquals(2.71, source.getY(), 1e-9);
        assertEquals(-5.0, source.getZ(), 1e-9);
        assertEquals(new Vector(10.0, 20.0, 30.0), source.getVelocity());
        assertEquals((byte) -1, source.getPitchByte());
        assertEquals((byte) 0, source.getYawByte());
        assertEquals((byte) 15, source.getHeadYawByte());
        assertEquals(0, source.getData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAddEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
