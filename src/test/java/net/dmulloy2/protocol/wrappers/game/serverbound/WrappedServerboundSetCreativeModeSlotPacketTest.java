package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetCreativeModeSlotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetCreativeModeSlotPacket w = new WrappedServerboundSetCreativeModeSlotPacket((short) 12, new ItemStack(Material.STONE));

        assertEquals(PacketType.Play.Client.SET_CREATIVE_SLOT, w.getHandle().getType());

        assertEquals((short) 12, w.getSlotNum());
        assertEquals(new ItemStack(Material.STONE), w.getItemStack());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetCreativeModeSlotPacket w = new WrappedServerboundSetCreativeModeSlotPacket();

        assertEquals(PacketType.Play.Client.SET_CREATIVE_SLOT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetCreativeModeSlotPacket source = new WrappedServerboundSetCreativeModeSlotPacket((short) 12, new ItemStack(Material.STONE));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCreativeModeSlotPacket wrapper = new WrappedServerboundSetCreativeModeSlotPacket(container);

        assertEquals((short) 12, wrapper.getSlotNum());
        assertEquals(new ItemStack(Material.STONE), wrapper.getItemStack());

        wrapper.setSlotNum((short) 5);
        wrapper.setItemStack(new ItemStack(Material.DIRT));

        assertEquals((short) 5, wrapper.getSlotNum());
        assertEquals(new ItemStack(Material.DIRT), wrapper.getItemStack());

        assertEquals((short) 5, source.getSlotNum());
        assertEquals(new ItemStack(Material.DIRT), source.getItemStack());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCreativeModeSlotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
