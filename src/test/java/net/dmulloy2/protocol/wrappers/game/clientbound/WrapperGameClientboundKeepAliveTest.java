package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundKeepAliveTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundKeepAlive w = new WrapperGameClientboundKeepAlive();
        w.setId(123456789L);

        assertEquals(123456789L, w.getId());
        assertEquals(PacketType.Play.Server.KEEP_ALIVE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.KEEP_ALIVE);
        raw.getModifier().writeDefaults();
        raw.getLongs().write(0, 987654321L);

        WrapperGameClientboundKeepAlive w = new WrapperGameClientboundKeepAlive(raw);
        assertEquals(987654321L, w.getId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundKeepAlive w = new WrapperGameClientboundKeepAlive();
        w.setId(1L);

        new WrapperGameClientboundKeepAlive(w.getHandle()).setId(42L);

        assertEquals(42L, w.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundKeepAlive(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
