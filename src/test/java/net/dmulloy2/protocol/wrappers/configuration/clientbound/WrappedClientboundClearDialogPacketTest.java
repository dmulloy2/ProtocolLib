package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundClearDialogPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Configuration.Server.CLEAR_DIALOG, new WrappedClientboundClearDialogPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundClearDialogPacket w = new WrappedClientboundClearDialogPacket();
        assertEquals(PacketType.Configuration.Server.CLEAR_DIALOG, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.CLEAR_DIALOG);
        WrappedClientboundClearDialogPacket wrapper = new WrappedClientboundClearDialogPacket(container);
        assertEquals(PacketType.Configuration.Server.CLEAR_DIALOG, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundClearDialogPacket(new PacketContainer(PacketType.Configuration.Server.KEEP_ALIVE)));
    }
}
