package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundInitializeBorderPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundInitializeBorderPacket w = new WrappedClientboundInitializeBorderPacket(3.14, 100.0, -2.5, 3.14, 42L, 5, 3, 7);

        assertEquals(PacketType.Play.Server.INITIALIZE_BORDER, w.getHandle().getType());

        assertEquals(3.14, w.getNewCenterX(), 1e-9);
        assertEquals(100.0, w.getNewCenterZ(), 1e-9);
        assertEquals(-2.5, w.getOldSize(), 1e-9);
        assertEquals(3.14, w.getNewSize(), 1e-9);
        assertEquals(42L, w.getLerpTime());
        assertEquals(5, w.getNewAbsoluteMaxSize());
        assertEquals(3, w.getWarningBlocks());
        assertEquals(7, w.getWarningTime());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundInitializeBorderPacket w = new WrappedClientboundInitializeBorderPacket();

        assertEquals(PacketType.Play.Server.INITIALIZE_BORDER, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundInitializeBorderPacket source = new WrappedClientboundInitializeBorderPacket(3.14, 100.0, -2.5, 3.14, 42L, 5, 3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundInitializeBorderPacket wrapper = new WrappedClientboundInitializeBorderPacket(container);

        assertEquals(3.14, wrapper.getNewCenterX(), 1e-9);
        assertEquals(100.0, wrapper.getNewCenterZ(), 1e-9);
        assertEquals(-2.5, wrapper.getOldSize(), 1e-9);
        assertEquals(3.14, wrapper.getNewSize(), 1e-9);
        assertEquals(42L, wrapper.getLerpTime());
        assertEquals(5, wrapper.getNewAbsoluteMaxSize());
        assertEquals(3, wrapper.getWarningBlocks());
        assertEquals(7, wrapper.getWarningTime());

        wrapper.setNewCenterX(2.71);
        wrapper.setNewCenterZ(-5.0);
        wrapper.setOldSize(0.0);
        wrapper.setNewSize(100.0);
        wrapper.setLerpTime(987654321L);
        wrapper.setNewAbsoluteMaxSize(-5);
        wrapper.setWarningBlocks(0);
        wrapper.setWarningTime(42);

        assertEquals(2.71, wrapper.getNewCenterX(), 1e-9);
        assertEquals(-5.0, wrapper.getNewCenterZ(), 1e-9);
        assertEquals(0.0, wrapper.getOldSize(), 1e-9);
        assertEquals(100.0, wrapper.getNewSize(), 1e-9);
        assertEquals(987654321L, wrapper.getLerpTime());
        assertEquals(-5, wrapper.getNewAbsoluteMaxSize());
        assertEquals(0, wrapper.getWarningBlocks());
        assertEquals(42, wrapper.getWarningTime());

        assertEquals(2.71, source.getNewCenterX(), 1e-9);
        assertEquals(-5.0, source.getNewCenterZ(), 1e-9);
        assertEquals(0.0, source.getOldSize(), 1e-9);
        assertEquals(100.0, source.getNewSize(), 1e-9);
        assertEquals(987654321L, source.getLerpTime());
        assertEquals(-5, source.getNewAbsoluteMaxSize());
        assertEquals(0, source.getWarningBlocks());
        assertEquals(42, source.getWarningTime());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundInitializeBorderPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
