package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundGameRuleValuesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.GAME_RULE_VALUES, new WrappedClientboundGameRuleValuesPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundGameRuleValuesPacket w = new WrappedClientboundGameRuleValuesPacket();
        assertEquals(PacketType.Play.Server.GAME_RULE_VALUES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.GAME_RULE_VALUES);
        WrappedClientboundGameRuleValuesPacket wrapper = new WrappedClientboundGameRuleValuesPacket(container);
        assertEquals(PacketType.Play.Server.GAME_RULE_VALUES, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameRuleValuesPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
