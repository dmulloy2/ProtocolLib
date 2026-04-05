package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCommandSuggestionsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundCommandSuggestionsPacket w = new WrappedClientboundCommandSuggestionsPacket(3, 7, 5);

        assertEquals(PacketType.Play.Server.TAB_COMPLETE, w.getHandle().getType());

        assertEquals(3, w.getId());
        assertEquals(7, w.getStart());
        assertEquals(5, w.getLength());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCommandSuggestionsPacket w = new WrappedClientboundCommandSuggestionsPacket();

        assertEquals(PacketType.Play.Server.TAB_COMPLETE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCommandSuggestionsPacket source = new WrappedClientboundCommandSuggestionsPacket(3, 7, 5);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCommandSuggestionsPacket wrapper = new WrappedClientboundCommandSuggestionsPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(7, wrapper.getStart());
        assertEquals(5, wrapper.getLength());

        wrapper.setId(9);
        wrapper.setStart(-5);
        wrapper.setLength(0);

        assertEquals(9, wrapper.getId());
        assertEquals(-5, wrapper.getStart());
        assertEquals(0, wrapper.getLength());

        assertEquals(9, source.getId());
        assertEquals(-5, source.getStart());
        assertEquals(0, source.getLength());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCommandSuggestionsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
