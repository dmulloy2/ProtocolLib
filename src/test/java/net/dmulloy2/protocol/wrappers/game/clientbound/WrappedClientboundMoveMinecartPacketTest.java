package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveMinecartPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No all-args constructor: lerpSteps field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.MOVE_MINECART, new WrappedClientboundMoveMinecartPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveMinecartPacket w = new WrappedClientboundMoveMinecartPacket();
        assertEquals(PacketType.Play.Server.MOVE_MINECART, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.MOVE_MINECART);
        WrappedClientboundMoveMinecartPacket wrapper = new WrappedClientboundMoveMinecartPacket(container);
        wrapper.setEntityId(99);
        assertEquals(99, wrapper.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveMinecartPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
