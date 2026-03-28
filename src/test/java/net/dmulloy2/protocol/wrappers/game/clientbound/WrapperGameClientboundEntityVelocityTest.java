package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundEntityVelocityTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundEntityVelocity w = new WrapperGameClientboundEntityVelocity();
        w.setEntityId(7);
        w.setVelocity(new Vector(1.0, 0.5, -0.5));

        assertEquals(7,    w.getEntityId());
        assertEquals(1.0,  w.getVelocity().getX(), 1e-4);
        assertEquals(0.5,  w.getVelocity().getY(), 1e-4);
        assertEquals(-0.5, w.getVelocity().getZ(), 1e-4);
        assertEquals(PacketType.Play.Server.ENTITY_VELOCITY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_VELOCITY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 3);

        WrapperGameClientboundEntityVelocity w = new WrapperGameClientboundEntityVelocity(raw);
        assertEquals(3, w.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundEntityVelocity w = new WrapperGameClientboundEntityVelocity();
        w.setEntityId(10);
        w.setVelocity(new Vector(0, 0, 0));

        new WrapperGameClientboundEntityVelocity(w.getHandle()).setEntityId(20);

        assertEquals(20, w.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundEntityVelocity(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
