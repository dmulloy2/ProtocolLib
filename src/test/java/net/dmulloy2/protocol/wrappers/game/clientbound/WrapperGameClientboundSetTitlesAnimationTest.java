package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetTitlesAnimationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetTitlesAnimation w = new WrapperGameClientboundSetTitlesAnimation();
        w.setFadeIn(10);
        w.setStay(70);
        w.setFadeOut(20);
        assertEquals(10, w.getFadeIn());
        assertEquals(70, w.getStay());
        assertEquals(20, w.getFadeOut());
        assertEquals(PacketType.Play.Server.SET_TITLES_ANIMATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_TITLES_ANIMATION);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 5);
        raw.getIntegers().write(1, 40);
        raw.getIntegers().write(2, 10);

        WrapperGameClientboundSetTitlesAnimation w = new WrapperGameClientboundSetTitlesAnimation(raw);
        assertEquals(5, w.getFadeIn());
        assertEquals(40, w.getStay());
        assertEquals(10, w.getFadeOut());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetTitlesAnimation w = new WrapperGameClientboundSetTitlesAnimation();
        w.setStay(20);

        new WrapperGameClientboundSetTitlesAnimation(w.getHandle()).setStay(100);

        assertEquals(100, w.getStay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetTitlesAnimation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
