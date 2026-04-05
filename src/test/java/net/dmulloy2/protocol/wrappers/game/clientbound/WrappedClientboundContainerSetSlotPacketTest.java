package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundContainerSetSlotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundContainerSetSlotPacket w = new WrappedClientboundContainerSetSlotPacket(3, 7, 5, new ItemStack(Material.STONE));

        assertEquals(PacketType.Play.Server.SET_SLOT, w.getHandle().getType());

        assertEquals(3, w.getWindowId());
        assertEquals(7, w.getStateId());
        assertEquals(5, w.getSlot());
        assertEquals(new ItemStack(Material.STONE), w.getItem());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundContainerSetSlotPacket w = new WrappedClientboundContainerSetSlotPacket();

        assertEquals(PacketType.Play.Server.SET_SLOT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundContainerSetSlotPacket source = new WrappedClientboundContainerSetSlotPacket(3, 7, 5, new ItemStack(Material.STONE));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerSetSlotPacket wrapper = new WrappedClientboundContainerSetSlotPacket(container);

        assertEquals(3, wrapper.getWindowId());
        assertEquals(7, wrapper.getStateId());
        assertEquals(5, wrapper.getSlot());
        assertEquals(new ItemStack(Material.STONE), wrapper.getItem());

        wrapper.setWindowId(9);
        wrapper.setStateId(-5);
        wrapper.setSlot(0);
        wrapper.setItem(new ItemStack(Material.DIRT));

        assertEquals(9, wrapper.getWindowId());
        assertEquals(-5, wrapper.getStateId());
        assertEquals(0, wrapper.getSlot());
        assertEquals(new ItemStack(Material.DIRT), wrapper.getItem());

        assertEquals(9, source.getWindowId());
        assertEquals(-5, source.getStateId());
        assertEquals(0, source.getSlot());
        assertEquals(new ItemStack(Material.DIRT), source.getItem());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetSlotPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
