package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundAnimationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundAnimation w = new WrapperGameClientboundAnimation();
        w.setEntityId(50);
        w.setAnimationId(0);
        assertEquals(50, w.getEntityId());
        assertEquals(0, w.getAnimationId());
        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ANIMATION);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 77);
        raw.getIntegers().write(1, 3);

        WrapperGameClientboundAnimation w = new WrapperGameClientboundAnimation(raw);
        assertEquals(77, w.getEntityId());
        assertEquals(3, w.getAnimationId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundAnimation w = new WrapperGameClientboundAnimation();
        w.setAnimationId(0);

        new WrapperGameClientboundAnimation(w.getHandle()).setAnimationId(4);

        assertEquals(4, w.getAnimationId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundAnimation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
