package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundClientTickEndPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Client.CLIENT_TICK_END, new WrappedServerboundClientTickEndPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundClientTickEndPacket w = new WrappedServerboundClientTickEndPacket();
        assertEquals(PacketType.Play.Client.CLIENT_TICK_END, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.CLIENT_TICK_END);
        WrappedServerboundClientTickEndPacket wrapper = new WrappedServerboundClientTickEndPacket(container);
        assertEquals(PacketType.Play.Client.CLIENT_TICK_END, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientTickEndPacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
