package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundSetGameRulePacket.WrappedEntry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetGameRulePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetGameRulePacket w = new WrappedServerboundSetGameRulePacket();
        assertEquals(PacketType.Play.Client.SET_GAME_RULE, w.getHandle().getType());
    }

    @Test
    void testGetEntriesEmptyByDefault() {
        WrappedServerboundSetGameRulePacket w = new WrappedServerboundSetGameRulePacket();
        List<WrappedEntry> entries = w.getEntries();
        assertNotNull(entries);
    }

    @Test
    void testSetAndGetEntries() {
        WrappedServerboundSetGameRulePacket w = new WrappedServerboundSetGameRulePacket();
        // ResourceLocation/Identifier requires lowercase paths
        WrappedEntry entry = new WrappedEntry(new MinecraftKey("minecraft", "do_fire_tick"), "false");
        w.setEntries(List.of(entry));

        List<WrappedEntry> entries = w.getEntries();
        assertEquals(1, entries.size());
        assertEquals("do_fire_tick", entries.get(0).gameRuleKey.getKey());
        assertEquals("false", entries.get(0).value);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetGameRulePacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
