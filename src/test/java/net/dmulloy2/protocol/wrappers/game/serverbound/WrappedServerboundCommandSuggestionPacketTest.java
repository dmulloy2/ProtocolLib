package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundCommandSuggestionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundCommandSuggestionPacket w = new WrappedServerboundCommandSuggestionPacket(3, "world");

        assertEquals(PacketType.Play.Client.TAB_COMPLETE, w.getHandle().getType());

        ServerboundCommandSuggestionPacket p = (ServerboundCommandSuggestionPacket) w.getHandle().getHandle();

        assertEquals(3, p.getId());
        assertEquals("world", p.getCommand());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCommandSuggestionPacket w = new WrappedServerboundCommandSuggestionPacket();

        assertEquals(PacketType.Play.Client.TAB_COMPLETE, w.getHandle().getType());

        ServerboundCommandSuggestionPacket p = (ServerboundCommandSuggestionPacket) w.getHandle().getHandle();

        assertEquals(0, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundCommandSuggestionPacket nmsPacket = new ServerboundCommandSuggestionPacket(3, "world");
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundCommandSuggestionPacket wrapper = new WrappedServerboundCommandSuggestionPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals("world", wrapper.getCommand());

        wrapper.setId(9);
        wrapper.setCommand("hello");

        assertEquals(9, nmsPacket.getId());
        assertEquals("hello", nmsPacket.getCommand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundCommandSuggestionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
