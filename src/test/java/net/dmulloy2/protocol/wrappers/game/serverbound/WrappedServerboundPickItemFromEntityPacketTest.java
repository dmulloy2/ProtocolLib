package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPickItemFromEntityPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundPickItemFromEntityPacket w = new WrappedServerboundPickItemFromEntityPacket(3, true);
        assertEquals(PacketType.Play.Client.PICK_ITEM, w.getHandle().getType());
        assertEquals(3, w.getId());
        assertTrue(w.isIncludeData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPickItemFromEntityPacket w = new WrappedServerboundPickItemFromEntityPacket();
        assertEquals(PacketType.Play.Client.PICK_ITEM, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPickItemFromEntityPacket src = new WrappedServerboundPickItemFromEntityPacket(3, true);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundPickItemFromEntityPacket wrapper = new WrappedServerboundPickItemFromEntityPacket(container);
        assertEquals(3, wrapper.getId());
        assertTrue(wrapper.isIncludeData());
        wrapper.setId(9);
        wrapper.setIncludeData(false);
        assertEquals(9, wrapper.getId());
        assertFalse(wrapper.isIncludeData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPickItemFromEntityPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
