package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetPlayerTeamPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetPlayerTeamPacket w = new WrappedClientboundSetPlayerTeamPacket("hello", 7, Optional.empty(), List.of("hello"));

        assertEquals(PacketType.Play.Server.SCOREBOARD_TEAM, w.getHandle().getType());

        assertEquals("hello", w.getName());
        assertEquals(7, w.getMethod());
        assertEquals(Optional.empty(), w.getParameters());
        assertEquals(List.of("hello"), w.getPlayers());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetPlayerTeamPacket w = new WrappedClientboundSetPlayerTeamPacket();

        assertEquals(PacketType.Play.Server.SCOREBOARD_TEAM, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetPlayerTeamPacket source = new WrappedClientboundSetPlayerTeamPacket("hello", 7, Optional.empty(), List.of("hello"));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetPlayerTeamPacket wrapper = new WrappedClientboundSetPlayerTeamPacket(container);

        assertEquals("hello", wrapper.getName());
        assertEquals(7, wrapper.getMethod());
        assertEquals(Optional.empty(), wrapper.getParameters());
        assertEquals(List.of("hello"), wrapper.getPlayers());

        wrapper.setName("modified");
        wrapper.setMethod(-5);
        wrapper.setParameters(Optional.empty());
        wrapper.setPlayers(List.of("modified"));

        assertEquals("modified", wrapper.getName());
        assertEquals(-5, wrapper.getMethod());
        assertEquals(Optional.empty(), wrapper.getParameters());
        assertEquals(List.of("modified"), wrapper.getPlayers());

        assertEquals("modified", source.getName());
        assertEquals(-5, source.getMethod());
        assertEquals(Optional.empty(), source.getParameters());
        assertEquals(List.of("modified"), source.getPlayers());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetPlayerTeamPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
