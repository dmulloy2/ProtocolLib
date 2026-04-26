package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundGameRuleValuesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundGameRuleValuesPacket w = new WrappedClientboundGameRuleValuesPacket();
        assertEquals(PacketType.Play.Server.GAME_RULE_VALUES, w.getHandle().getType());
    }

    @Test
    void testGetValuesEmptyByDefault() {
        WrappedClientboundGameRuleValuesPacket w = new WrappedClientboundGameRuleValuesPacket();
        Map<MinecraftKey, String> values = w.getValues();
        assertNotNull(values);
    }

    @Test
    void testSetAndGetValues() {
        WrappedClientboundGameRuleValuesPacket w = new WrappedClientboundGameRuleValuesPacket();
        // ResourceLocation/Identifier requires lowercase paths
        MinecraftKey doFireTick = new MinecraftKey("minecraft", "do_fire_tick");

        w.setValues(Map.of(doFireTick, "true"));

        Map<MinecraftKey, String> values = w.getValues();
        assertEquals(1, values.size());
        String val = values.values().iterator().next();
        assertEquals("true", val);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundGameRuleValuesPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
