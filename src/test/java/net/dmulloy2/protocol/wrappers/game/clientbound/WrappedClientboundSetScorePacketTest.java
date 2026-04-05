package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetScorePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetScorePacket w = new WrappedClientboundSetScorePacket("hello", "world", 5, Optional.empty(), Optional.empty());

        assertEquals(PacketType.Play.Server.SCOREBOARD_SCORE, w.getHandle().getType());

        assertEquals("hello", w.getOwner());
        assertEquals("world", w.getObjectiveName());
        assertEquals(5, w.getScore());
        assertEquals(Optional.empty(), w.getDisplay());
        assertEquals(Optional.empty(), w.getNumberFormat());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetScorePacket w = new WrappedClientboundSetScorePacket();

        assertEquals(PacketType.Play.Server.SCOREBOARD_SCORE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetScorePacket source = new WrappedClientboundSetScorePacket("hello", "world", 5, Optional.empty(), Optional.empty());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetScorePacket wrapper = new WrappedClientboundSetScorePacket(container);

        assertEquals("hello", wrapper.getOwner());
        assertEquals("world", wrapper.getObjectiveName());
        assertEquals(5, wrapper.getScore());
        assertEquals(Optional.empty(), wrapper.getDisplay());
        assertEquals(Optional.empty(), wrapper.getNumberFormat());

        wrapper.setOwner("modified");
        wrapper.setObjectiveName("hello");
        wrapper.setScore(0);
        wrapper.setDisplay(Optional.empty());
        wrapper.setNumberFormat(Optional.empty());

        assertEquals("modified", wrapper.getOwner());
        assertEquals("hello", wrapper.getObjectiveName());
        assertEquals(0, wrapper.getScore());
        assertEquals(Optional.empty(), wrapper.getDisplay());
        assertEquals(Optional.empty(), wrapper.getNumberFormat());

        assertEquals("modified", source.getOwner());
        assertEquals("hello", source.getObjectiveName());
        assertEquals(0, source.getScore());
        assertEquals(Optional.empty(), source.getDisplay());
        assertEquals(Optional.empty(), source.getNumberFormat());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetScorePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
