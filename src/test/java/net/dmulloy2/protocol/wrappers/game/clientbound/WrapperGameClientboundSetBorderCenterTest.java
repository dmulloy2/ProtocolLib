package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetBorderCenterTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetBorderCenter w = new WrapperGameClientboundSetBorderCenter();
        w.setX(100.5);
        w.setZ(-200.5);
        assertEquals(100.5, w.getX(), 1e-9);
        assertEquals(-200.5, w.getZ(), 1e-9);
        assertEquals(PacketType.Play.Server.SET_BORDER_CENTER, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_BORDER_CENTER);
        raw.getModifier().writeDefaults();
        raw.getDoubles().write(0, 0.0);
        raw.getDoubles().write(1, 0.0);

        WrapperGameClientboundSetBorderCenter w = new WrapperGameClientboundSetBorderCenter(raw);
        assertEquals(0.0, w.getX(), 1e-9);
        assertEquals(0.0, w.getZ(), 1e-9);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetBorderCenter w = new WrapperGameClientboundSetBorderCenter();
        w.setX(0.0);

        new WrapperGameClientboundSetBorderCenter(w.getHandle()).setX(500.0);

        assertEquals(500.0, w.getX(), 1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetBorderCenter(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
