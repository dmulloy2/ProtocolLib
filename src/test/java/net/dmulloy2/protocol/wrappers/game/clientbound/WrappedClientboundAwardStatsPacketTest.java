package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAwardStatsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundAwardStatsPacket w = new WrappedClientboundAwardStatsPacket(Map.of());

        assertEquals(PacketType.Play.Server.STATISTIC, w.getHandle().getType());

        assertEquals(Map.of(), w.getStats());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundAwardStatsPacket w = new WrappedClientboundAwardStatsPacket();

        assertEquals(PacketType.Play.Server.STATISTIC, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundAwardStatsPacket source = new WrappedClientboundAwardStatsPacket(Map.of());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAwardStatsPacket wrapper = new WrappedClientboundAwardStatsPacket(container);

        assertEquals(Map.of(), wrapper.getStats());
        wrapper.setStats(Map.of());
        assertEquals(Map.of(), wrapper.getStats());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAwardStatsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
