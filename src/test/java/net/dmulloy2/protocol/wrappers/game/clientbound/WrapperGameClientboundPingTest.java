package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPingTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPing w = new WrapperGameClientboundPing();
        w.setId(42);
        assertEquals(42, w.getId());
        assertEquals(PacketType.Play.Server.PING, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PING);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);

        WrapperGameClientboundPing w = new WrapperGameClientboundPing(raw);
        assertEquals(99, w.getId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPing w = new WrapperGameClientboundPing();
        w.setId(1);

        new WrapperGameClientboundPing(w.getHandle()).setId(2);

        assertEquals(2, w.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPing(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
