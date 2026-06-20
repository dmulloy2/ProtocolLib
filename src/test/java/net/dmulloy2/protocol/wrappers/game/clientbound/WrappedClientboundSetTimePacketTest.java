package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetTimePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetTimePacket w = new WrappedClientboundSetTimePacket(123456789L, Map.of());

        assertEquals(PacketType.Play.Server.UPDATE_TIME, w.getHandle().getType());

        assertEquals(123456789L, w.getWorldAge());
        assertEquals(Map.of(), w.getClockUpdates());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetTimePacket w = new WrappedClientboundSetTimePacket();

        assertEquals(PacketType.Play.Server.UPDATE_TIME, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetTimePacket source = new WrappedClientboundSetTimePacket(123456789L);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTimePacket wrapper = new WrappedClientboundSetTimePacket(container);

        assertEquals(123456789L, wrapper.getWorldAge());

        wrapper.setWorldAge(987654321L);
        wrapper.setClockUpdates(Map.of());

        assertEquals(987654321L, wrapper.getWorldAge());
        assertEquals(Map.of(), wrapper.getClockUpdates());

        assertEquals(987654321L, source.getWorldAge());
        assertEquals(Map.of(), source.getClockUpdates());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTimePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
