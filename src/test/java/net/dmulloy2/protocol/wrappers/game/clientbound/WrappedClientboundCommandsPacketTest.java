package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCommandsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.COMMANDS, new WrappedClientboundCommandsPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCommandsPacket w = new WrappedClientboundCommandsPacket();

        assertEquals(PacketType.Play.Server.COMMANDS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.COMMANDS);
        WrappedClientboundCommandsPacket wrapper = new WrappedClientboundCommandsPacket(container);

        assertEquals(PacketType.Play.Server.COMMANDS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCommandsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
