package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetObjectivePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetObjectivePacket w = new WrappedClientboundSetObjectivePacket("hello", 7, WrappedChatComponent.fromText("Goodbye!"), EnumWrappers.RenderType.INTEGER, Optional.empty());

        assertEquals(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, w.getHandle().getType());

        assertEquals("hello", w.getObjectiveName());
        assertEquals(7, w.getMethod());
        assertEquals(WrappedChatComponent.fromText("Goodbye!"), w.getDisplayName());
        assertEquals(EnumWrappers.RenderType.INTEGER, w.getRenderType());
        assertEquals(Optional.empty(), w.getNumberFormat());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetObjectivePacket w = new WrappedClientboundSetObjectivePacket();

        assertEquals(PacketType.Play.Server.SCOREBOARD_OBJECTIVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetObjectivePacket source = new WrappedClientboundSetObjectivePacket("hello", 7, WrappedChatComponent.fromText("Goodbye!"), EnumWrappers.RenderType.INTEGER, Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetObjectivePacket wrapper = new WrappedClientboundSetObjectivePacket(container);

        assertEquals("hello", wrapper.getObjectiveName());
        assertEquals(7, wrapper.getMethod());
        assertEquals(EnumWrappers.RenderType.INTEGER, wrapper.getRenderType());

        wrapper.setObjectiveName("modified");
        wrapper.setMethod(-5);
        wrapper.setDisplayName(WrappedChatComponent.fromText("Modified"));
        wrapper.setRenderType(EnumWrappers.RenderType.HEARTS);
        wrapper.setNumberFormat(Optional.empty());

        assertEquals("modified", wrapper.getObjectiveName());
        assertEquals(-5, wrapper.getMethod());
        assertEquals(EnumWrappers.RenderType.HEARTS, wrapper.getRenderType());
        assertEquals(Optional.empty(), wrapper.getNumberFormat());

        assertEquals("modified", source.getObjectiveName());
        assertEquals(EnumWrappers.RenderType.HEARTS, source.getRenderType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetObjectivePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
