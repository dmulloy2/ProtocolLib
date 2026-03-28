package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundViewDistanceTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundViewDistance w = new WrapperGameClientboundViewDistance();
        w.setViewDistance(10);
        assertEquals(10, w.getViewDistance());
        assertEquals(PacketType.Play.Server.VIEW_DISTANCE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.VIEW_DISTANCE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 12);

        WrapperGameClientboundViewDistance w = new WrapperGameClientboundViewDistance(raw);
        assertEquals(12, w.getViewDistance());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundViewDistance w = new WrapperGameClientboundViewDistance();
        w.setViewDistance(8);

        new WrapperGameClientboundViewDistance(w.getHandle()).setViewDistance(16);

        assertEquals(16, w.getViewDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundViewDistance(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
