package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundContainerSlotStateChangedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundContainerSlotStateChangedPacket w = new WrappedServerboundContainerSlotStateChangedPacket(3, 7, true);

        assertEquals(PacketType.Play.Client.CONTAINER_SLOT_STATE_CHANGED, w.getHandle().getType());

        ServerboundContainerSlotStateChangedPacket p = (ServerboundContainerSlotStateChangedPacket) w.getHandle().getHandle();

        assertEquals(3, p.slotId());
        assertEquals(7, p.containerId());
        assertTrue(p.newState());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundContainerSlotStateChangedPacket w = new WrappedServerboundContainerSlotStateChangedPacket();

        assertEquals(PacketType.Play.Client.CONTAINER_SLOT_STATE_CHANGED, w.getHandle().getType());

        ServerboundContainerSlotStateChangedPacket p = (ServerboundContainerSlotStateChangedPacket) w.getHandle().getHandle();

        assertEquals(0, p.slotId());
        assertEquals(0, p.containerId());
        assertFalse(p.newState());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundContainerSlotStateChangedPacket nmsPacket = new ServerboundContainerSlotStateChangedPacket(3, 7, true);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerSlotStateChangedPacket wrapper = new WrappedServerboundContainerSlotStateChangedPacket(container);

        assertEquals(3, wrapper.getSlotId());
        assertEquals(7, wrapper.getContainerId());
        assertTrue(wrapper.isNewState());

        wrapper.setSlotId(9);
        wrapper.setContainerId(-5);
        wrapper.setNewState(false);

        assertEquals(9, nmsPacket.slotId());
        assertEquals(-5, nmsPacket.containerId());
        assertFalse(nmsPacket.newState());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundContainerSlotStateChangedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
