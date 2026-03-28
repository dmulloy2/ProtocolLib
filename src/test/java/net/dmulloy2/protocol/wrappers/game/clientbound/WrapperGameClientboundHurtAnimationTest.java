package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundHurtAnimationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundHurtAnimation w = new WrapperGameClientboundHurtAnimation();
        w.setEntityId(42);
        w.setYaw(135.0f);

        assertEquals(42,     w.getEntityId());
        assertEquals(135.0f, w.getYaw(), 1e-4f);
        assertEquals(PacketType.Play.Server.HURT_ANIMATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.HURT_ANIMATION);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);
        raw.getFloat().write(0, 45.0f);

        WrapperGameClientboundHurtAnimation w = new WrapperGameClientboundHurtAnimation(raw);
        assertEquals(7,     w.getEntityId());
        assertEquals(45.0f, w.getYaw(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundHurtAnimation w = new WrapperGameClientboundHurtAnimation();
        w.setEntityId(1);
        w.setYaw(0.0f);

        new WrapperGameClientboundHurtAnimation(w.getHandle()).setYaw(270.0f);

        assertEquals(270.0f, w.getYaw(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundHurtAnimation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
