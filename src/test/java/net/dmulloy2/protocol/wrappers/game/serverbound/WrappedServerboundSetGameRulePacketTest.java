package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetGameRulePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Client.SET_GAME_RULE, new WrappedServerboundSetGameRulePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetGameRulePacket w = new WrappedServerboundSetGameRulePacket();
        assertEquals(PacketType.Play.Client.SET_GAME_RULE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.SET_GAME_RULE);
        WrappedServerboundSetGameRulePacket wrapper = new WrappedServerboundSetGameRulePacket(container);
        assertEquals(PacketType.Play.Client.SET_GAME_RULE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetGameRulePacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
