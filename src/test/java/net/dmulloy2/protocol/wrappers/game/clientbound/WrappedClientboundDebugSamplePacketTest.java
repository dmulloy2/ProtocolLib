package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundDebugSamplePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundDebugSamplePacket w = new WrappedClientboundDebugSamplePacket(new long[] { 1L, 2L, 3L }, WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME);

        assertEquals(PacketType.Play.Server.DEBUG_SAMPLE, w.getHandle().getType());

        assertArrayEquals(new long[] { 1L, 2L, 3L }, w.getSample());
        assertEquals(WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME, w.getDebugSampleType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundDebugSamplePacket w = new WrappedClientboundDebugSamplePacket();

        assertEquals(PacketType.Play.Server.DEBUG_SAMPLE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundDebugSamplePacket source = new WrappedClientboundDebugSamplePacket(new long[] { 1L, 2L, 3L }, WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundDebugSamplePacket wrapper = new WrappedClientboundDebugSamplePacket(container);

        assertArrayEquals(new long[] { 1L, 2L, 3L }, wrapper.getSample());
        assertEquals(WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME, wrapper.getDebugSampleType());

        wrapper.setSample(new long[] { 10L, 20L, 30L });
        wrapper.setDebugSampleType(WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME);

        assertArrayEquals(new long[] { 10L, 20L, 30L }, wrapper.getSample());
        assertEquals(WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME, wrapper.getDebugSampleType());

        assertArrayEquals(new long[] { 10L, 20L, 30L }, source.getSample());
        assertEquals(WrappedClientboundDebugSamplePacket.DebugSampleType.TICK_TIME, source.getDebugSampleType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundDebugSamplePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
