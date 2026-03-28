package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSpawnEntityTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSpawnEntity w = new WrapperGameClientboundSpawnEntity();
        UUID uuid = UUID.randomUUID();

        w.setEntityId(5);
        w.setEntityUUID(uuid);
        w.setEntityType(EntityType.ZOMBIE);
        w.setX(10.5); w.setY(64.0); w.setZ(-3.5);
        w.setVelocity(new Vector(0.1, 0.2, 0.3));
        w.setPitchByte(WrapperGameClientboundSpawnEntity.angleToByte(15.0f));
        w.setYawByte(WrapperGameClientboundSpawnEntity.angleToByte(90.0f));
        w.setHeadYawByte(WrapperGameClientboundSpawnEntity.angleToByte(90.0f));
        w.setData(42);

        assertEquals(5, w.getEntityId());
        assertEquals(uuid, w.getEntityUUID());
        assertEquals(EntityType.ZOMBIE, w.getEntityType());
        assertEquals(10.5, w.getX(), 1e-6);
        assertEquals(64.0, w.getY(), 1e-6);
        assertEquals(-3.5, w.getZ(), 1e-6);
        assertEquals(0.1, w.getVelocity().getX(), 1e-3);
        assertEquals(0.2, w.getVelocity().getY(), 1e-3);
        assertEquals(0.3, w.getVelocity().getZ(), 1e-3);
        assertEquals(42, w.getData());
        assertEquals(PacketType.Play.Server.SPAWN_ENTITY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);
        raw.getDoubles().write(0, 5.0);
        raw.getDoubles().write(1, 70.0);
        raw.getDoubles().write(2, 5.0);

        WrapperGameClientboundSpawnEntity w = new WrapperGameClientboundSpawnEntity(raw);
        assertEquals(99,   w.getEntityId());
        assertEquals(5.0,  w.getX(), 1e-6);
        assertEquals(70.0, w.getY(), 1e-6);
        assertEquals(5.0,  w.getZ(), 1e-6);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSpawnEntity w = new WrapperGameClientboundSpawnEntity();
        w.setEntityId(1);
        w.setX(0); w.setY(0); w.setZ(0);

        WrapperGameClientboundSpawnEntity w2 = new WrapperGameClientboundSpawnEntity(w.getHandle());
        w2.setEntityId(2);
        w2.setX(100.0);

        assertEquals(2,     w.getEntityId());
        assertEquals(100.0, w.getX(), 1e-6);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSpawnEntity(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }

    @Test
    void testAngleConversion() {
        assertEquals(0.0f,   WrapperGameClientboundSpawnEntity.byteToAngle((byte) 0),    0.1f);
        assertEquals(180.0f, WrapperGameClientboundSpawnEntity.byteToAngle((byte) -128), 0.1f);
        assertEquals((byte) 64, WrapperGameClientboundSpawnEntity.angleToByte(90.0f));
    }
}
