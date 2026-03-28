package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTickingStatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket();
        w.setTickRate(20.0f);
        w.setFrozen(true);
        assertEquals(20.0f, w.getTickRate(), 1e-6f);
        assertTrue(w.isFrozen());
        assertEquals(PacketType.Play.Server.TICKING_STATE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.TICKING_STATE);
        raw.getModifier().writeDefaults();
        raw.getFloat().write(0, 4.0f);
        raw.getBooleans().write(0, false);

        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket(raw);
        assertEquals(4.0f, w.getTickRate(), 1e-6f);
        assertFalse(w.isFrozen());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket();
        w.setFrozen(false);

        new WrappedClientboundTickingStatePacket(w.getHandle()).setFrozen(true);

        assertTrue(w.isFrozen());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
