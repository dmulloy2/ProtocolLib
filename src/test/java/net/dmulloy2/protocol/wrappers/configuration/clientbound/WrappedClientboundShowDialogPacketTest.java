package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundShowDialogPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Configuration.Server.SHOW_DIALOG, new WrappedClientboundShowDialogPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundShowDialogPacket w = new WrappedClientboundShowDialogPacket();
        assertEquals(PacketType.Configuration.Server.SHOW_DIALOG, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.SHOW_DIALOG);
        WrappedClientboundShowDialogPacket wrapper = new WrappedClientboundShowDialogPacket(container);
        assertEquals(PacketType.Configuration.Server.SHOW_DIALOG, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundShowDialogPacket(new PacketContainer(PacketType.Configuration.Server.KEEP_ALIVE)));
    }
}
