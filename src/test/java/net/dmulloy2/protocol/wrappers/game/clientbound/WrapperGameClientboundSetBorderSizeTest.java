package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetBorderSizeTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetBorderSize w = new WrapperGameClientboundSetBorderSize();
        w.setDiameter(1024.0);
        assertEquals(1024.0, w.getDiameter(), 1e-9);
        assertEquals(PacketType.Play.Server.SET_BORDER_SIZE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_BORDER_SIZE);
        raw.getModifier().writeDefaults();
        raw.getDoubles().write(0, 60000000.0);

        WrapperGameClientboundSetBorderSize w = new WrapperGameClientboundSetBorderSize(raw);
        assertEquals(60000000.0, w.getDiameter(), 1e-9);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetBorderSize w = new WrapperGameClientboundSetBorderSize();
        w.setDiameter(100.0);

        new WrapperGameClientboundSetBorderSize(w.getHandle()).setDiameter(200.0);

        assertEquals(200.0, w.getDiameter(), 1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetBorderSize(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
