package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetDisplayObjectivePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetDisplayObjectivePacket w = new WrappedClientboundSetDisplayObjectivePacket(EnumWrappers.DisplaySlot.SIDEBAR, "world");

        assertEquals(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE, w.getHandle().getType());

        assertEquals(EnumWrappers.DisplaySlot.SIDEBAR, w.getSlot());
        assertEquals("world", w.getObjectiveName());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetDisplayObjectivePacket w = new WrappedClientboundSetDisplayObjectivePacket();

        assertEquals(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetDisplayObjectivePacket source = new WrappedClientboundSetDisplayObjectivePacket(EnumWrappers.DisplaySlot.SIDEBAR, "world");
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetDisplayObjectivePacket wrapper = new WrappedClientboundSetDisplayObjectivePacket(container);

        assertEquals(EnumWrappers.DisplaySlot.SIDEBAR, wrapper.getSlot());
        assertEquals("world", wrapper.getObjectiveName());

        wrapper.setSlot(EnumWrappers.DisplaySlot.LIST);
        wrapper.setObjectiveName("hello");

        assertEquals(EnumWrappers.DisplaySlot.LIST, wrapper.getSlot());
        assertEquals("hello", wrapper.getObjectiveName());

        assertEquals(EnumWrappers.DisplaySlot.LIST, source.getSlot());
        assertEquals("hello", source.getObjectiveName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetDisplayObjectivePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
