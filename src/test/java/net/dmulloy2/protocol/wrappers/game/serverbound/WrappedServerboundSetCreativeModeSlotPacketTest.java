package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
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
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        WrappedServerboundSetCreativeModeSlotPacket w = new WrappedServerboundSetCreativeModeSlotPacket((short) 5, item);

        assertEquals(PacketType.Play.Client.SET_CREATIVE_SLOT, w.getHandle().getType());

        ServerboundSetCreativeModeSlotPacket p = (ServerboundSetCreativeModeSlotPacket) w.getHandle().getHandle();

        assertEquals(5, p.slotNum());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetCreativeModeSlotPacket w = new WrappedServerboundSetCreativeModeSlotPacket();

        assertEquals(PacketType.Play.Client.SET_CREATIVE_SLOT, w.getHandle().getType());

        ServerboundSetCreativeModeSlotPacket p = (ServerboundSetCreativeModeSlotPacket) w.getHandle().getHandle();

        assertEquals(0, p.slotNum());
    }

    @Test
    void testModifyExistingPacket() {
        ItemStack item = new ItemStack(Material.STONE);
        ServerboundSetCreativeModeSlotPacket nmsPacket = new ServerboundSetCreativeModeSlotPacket(
                5, net.minecraft.world.item.ItemStack.EMPTY);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCreativeModeSlotPacket wrapper = new WrappedServerboundSetCreativeModeSlotPacket(container);

        assertEquals(5, wrapper.getSlotNum());

        wrapper.setSlotNum((short) 10);
        wrapper.setItemStack(item);

        assertEquals(10, nmsPacket.slotNum());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCreativeModeSlotPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
