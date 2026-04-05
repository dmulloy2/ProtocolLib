package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundGameEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundGameEventPacket w = new WrappedClientboundGameEventPacket(3, 0.5f);

        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());

        assertEquals(3, w.getEvent());
        assertEquals(0.5f, w.getValue(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundGameEventPacket w = new WrappedClientboundGameEventPacket();

        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundGameEventPacket source = new WrappedClientboundGameEventPacket(3, 0.5f);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundGameEventPacket wrapper = new WrappedClientboundGameEventPacket(container);

        assertEquals(3, wrapper.getEvent());
        assertEquals(0.5f, wrapper.getValue(), 1e-4f);

        wrapper.setEvent(9);
        wrapper.setValue(-3.0f);

        assertEquals(9, wrapper.getEvent());
        assertEquals(-3.0f, wrapper.getValue(), 1e-4f);

        assertEquals(9, source.getEvent());
        assertEquals(-3.0f, source.getValue(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
