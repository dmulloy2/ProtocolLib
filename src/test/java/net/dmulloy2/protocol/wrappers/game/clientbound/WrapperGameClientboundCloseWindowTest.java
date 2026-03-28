package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundCloseWindowTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundCloseWindow w = new WrapperGameClientboundCloseWindow();
        w.setWindowId(5);

        assertEquals(5, w.getWindowId());
        assertEquals(PacketType.Play.Server.CLOSE_WINDOW, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.CLOSE_WINDOW);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 3);

        WrapperGameClientboundCloseWindow w = new WrapperGameClientboundCloseWindow(raw);
        assertEquals(3, w.getWindowId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundCloseWindow w = new WrapperGameClientboundCloseWindow();
        w.setWindowId(1);

        new WrapperGameClientboundCloseWindow(w.getHandle()).setWindowId(9);

        assertEquals(9, w.getWindowId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundCloseWindow(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
