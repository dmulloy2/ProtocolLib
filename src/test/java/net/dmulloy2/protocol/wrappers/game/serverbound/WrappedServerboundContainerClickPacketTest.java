package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundContainerClickPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundContainerClickPacket w = new WrappedServerboundContainerClickPacket(3, 7, (short) -1, (byte) 3);

        assertEquals(PacketType.Play.Client.WINDOW_CLICK, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
        assertEquals(7, w.getStateId());
        assertEquals((short) -1, w.getSlotNum());
        assertEquals((byte) 3, w.getButtonNum());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundContainerClickPacket w = new WrappedServerboundContainerClickPacket();

        assertEquals(PacketType.Play.Client.WINDOW_CLICK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundContainerClickPacket source = new WrappedServerboundContainerClickPacket(3, 7, (short) -1, (byte) 3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerClickPacket wrapper = new WrappedServerboundContainerClickPacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertEquals(7, wrapper.getStateId());
        assertEquals((short) -1, wrapper.getSlotNum());
        assertEquals((byte) 3, wrapper.getButtonNum());

        wrapper.setContainerId(9);
        wrapper.setStateId(-5);
        wrapper.setSlotNum((short) 0);
        wrapper.setButtonNum((byte) 15);

        assertEquals(9, wrapper.getContainerId());
        assertEquals(-5, wrapper.getStateId());
        assertEquals((short) 0, wrapper.getSlotNum());
        assertEquals((byte) 15, wrapper.getButtonNum());

        assertEquals(9, source.getContainerId());
        assertEquals(-5, source.getStateId());
        assertEquals((short) 0, source.getSlotNum());
        assertEquals((byte) 15, source.getButtonNum());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundContainerClickPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
