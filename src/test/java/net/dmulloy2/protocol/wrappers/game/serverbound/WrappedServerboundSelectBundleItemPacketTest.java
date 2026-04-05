package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSelectBundleItemPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSelectBundleItemPacket w = new WrappedServerboundSelectBundleItemPacket(3, 7);
        assertEquals(PacketType.Play.Client.SELECT_BUNDLE_ITEM, w.getHandle().getType());
        assertEquals(3, w.getSlotId());
        assertEquals(7, w.getSelectedItemIndex());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSelectBundleItemPacket w = new WrappedServerboundSelectBundleItemPacket();
        assertEquals(PacketType.Play.Client.SELECT_BUNDLE_ITEM, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSelectBundleItemPacket src = new WrappedServerboundSelectBundleItemPacket(3, 7);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundSelectBundleItemPacket wrapper = new WrappedServerboundSelectBundleItemPacket(container);
        assertEquals(3, wrapper.getSlotId());
        assertEquals(7, wrapper.getSelectedItemIndex());
        wrapper.setSlotId(9);
        wrapper.setSelectedItemIndex(-5);
        assertEquals(9, wrapper.getSlotId());
        assertEquals(-5, wrapper.getSelectedItemIndex());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSelectBundleItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
