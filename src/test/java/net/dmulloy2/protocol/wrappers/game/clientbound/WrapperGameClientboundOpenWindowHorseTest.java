package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundOpenWindowHorseTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundOpenWindowHorse w = new WrapperGameClientboundOpenWindowHorse();
        w.setWindowId(3);
        w.setContainerSize(6);
        w.setEntityId(55);
        assertEquals(3, w.getWindowId());
        assertEquals(6, w.getContainerSize());
        assertEquals(55, w.getEntityId());
        assertEquals(PacketType.Play.Server.OPEN_WINDOW_HORSE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.OPEN_WINDOW_HORSE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 1);
        raw.getIntegers().write(1, 3);
        raw.getIntegers().write(2, 100);

        WrapperGameClientboundOpenWindowHorse w = new WrapperGameClientboundOpenWindowHorse(raw);
        assertEquals(1, w.getWindowId());
        assertEquals(3, w.getContainerSize());
        assertEquals(100, w.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundOpenWindowHorse w = new WrapperGameClientboundOpenWindowHorse();
        w.setEntityId(1);

        new WrapperGameClientboundOpenWindowHorse(w.getHandle()).setEntityId(77);

        assertEquals(77, w.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundOpenWindowHorse(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
