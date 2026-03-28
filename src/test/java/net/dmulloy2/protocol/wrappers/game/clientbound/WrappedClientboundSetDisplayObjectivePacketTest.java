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
    void testCreate() {
        WrappedClientboundSetDisplayObjectivePacket w = new WrappedClientboundSetDisplayObjectivePacket();
        w.setSlot(EnumWrappers.DisplaySlot.BELOW_NAME);
        w.setObjectiveName("myObjective");

        assertEquals(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE, w.getHandle().getType());
        assertEquals(EnumWrappers.DisplaySlot.BELOW_NAME, w.getSlot());
        assertEquals("myObjective", w.getObjectiveName());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        container.getModifier().writeDefaults();
        container.getDisplaySlots().write(0, EnumWrappers.DisplaySlot.SIDEBAR);
        container.getStrings().write(0, "testObj");

        WrappedClientboundSetDisplayObjectivePacket wrapper = new WrappedClientboundSetDisplayObjectivePacket(container);

        assertEquals(EnumWrappers.DisplaySlot.SIDEBAR, wrapper.getSlot());
        assertEquals("testObj", wrapper.getObjectiveName());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SCOREBOARD_DISPLAY_OBJECTIVE);
        container.getModifier().writeDefaults();
        container.getStrings().write(0, "old");

        WrappedClientboundSetDisplayObjectivePacket wrapper = new WrappedClientboundSetDisplayObjectivePacket(container);
        wrapper.setObjectiveName("new");

        assertEquals("new", wrapper.getObjectiveName());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetDisplayObjectivePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
