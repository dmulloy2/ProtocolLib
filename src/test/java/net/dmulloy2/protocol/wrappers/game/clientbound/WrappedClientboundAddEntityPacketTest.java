package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAddEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundAddEntityPacket w = new WrappedClientboundAddEntityPacket();
        UUID uuid = UUID.fromString("aaaabbbb-cccc-dddd-eeee-ffffaaaabbbb");
        w.setEntityId(42);
        w.setEntityUUID(uuid);
        w.setEntityType(EntityType.ZOMBIE);
        w.setX(1.5);
        w.setY(64.0);
        w.setZ(-3.5);
        w.setVelocity(new Vector(0.0, 0.0, 0.0));
        w.setPitchByte((byte) 32);
        w.setYawByte((byte) 64);
        w.setHeadYawByte((byte) 96);
        w.setData(7);

        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());

        ClientboundAddEntityPacket p = (ClientboundAddEntityPacket) w.getHandle().getHandle();

        assertEquals(42, p.getId());
        assertEquals(uuid, p.getUUID());
        assertEquals(1.5, p.getX(), 1e-6);
        assertEquals(64.0, p.getY(), 1e-6);
        assertEquals(-3.5, p.getZ(), 1e-6);
        assertEquals(7, p.getData());
    }

    @Test
    void testReadFromExistingPacket() {
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        ClientboundAddEntityPacket nmsPacket = new ClientboundAddEntityPacket(
                10, uuid, 2.0, 70.0, 4.0, 0.0f, 90.0f,
                net.minecraft.world.entity.EntityType.ZOMBIE, 0,
                new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.0), 0.0
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAddEntityPacket wrapper = new WrappedClientboundAddEntityPacket(container);

        assertEquals(10, wrapper.getEntityId());
        assertEquals(uuid, wrapper.getEntityUUID());
        assertEquals(2.0, wrapper.getX(), 1e-6);
        assertEquals(70.0, wrapper.getY(), 1e-6);
        assertEquals(4.0, wrapper.getZ(), 1e-6);
    }

    @Test
    void testModifyExistingPacket() {
        UUID uuid = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        ClientboundAddEntityPacket nmsPacket = new ClientboundAddEntityPacket(
                10, uuid, 2.0, 70.0, 4.0, 0.0f, 90.0f,
                net.minecraft.world.entity.EntityType.ZOMBIE, 0,
                new net.minecraft.world.phys.Vec3(0.0, 0.0, 0.0), 0.0
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAddEntityPacket wrapper = new WrappedClientboundAddEntityPacket(container);

        wrapper.setEntityId(99);

        assertEquals(99, wrapper.getEntityId());
        assertEquals(uuid, wrapper.getEntityUUID());
        assertEquals(2.0, wrapper.getX(), 1e-6);
        assertEquals(70.0, wrapper.getY(), 1e-6);
        assertEquals(4.0, wrapper.getZ(), 1e-6);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAddEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
