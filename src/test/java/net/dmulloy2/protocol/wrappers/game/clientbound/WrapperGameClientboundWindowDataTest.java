package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundWindowDataTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundWindowData w = new WrapperGameClientboundWindowData();
        w.setWindowId(1);
        w.setProperty(4);
        w.setValue(100);
        assertEquals(1, w.getWindowId());
        assertEquals(4, w.getProperty());
        assertEquals(100, w.getValue());
        assertEquals(PacketType.Play.Server.WINDOW_DATA, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.WINDOW_DATA);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 2);
        raw.getIntegers().write(1, 0);
        raw.getIntegers().write(2, 50);

        WrapperGameClientboundWindowData w = new WrapperGameClientboundWindowData(raw);
        assertEquals(2, w.getWindowId());
        assertEquals(0, w.getProperty());
        assertEquals(50, w.getValue());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundWindowData w = new WrapperGameClientboundWindowData();
        w.setValue(0);

        new WrapperGameClientboundWindowData(w.getHandle()).setValue(200);

        assertEquals(200, w.getValue());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundWindowData(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
