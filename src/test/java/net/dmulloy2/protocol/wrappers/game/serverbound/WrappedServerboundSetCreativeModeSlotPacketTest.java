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
    void testCreate() {
        WrappedServerboundSetCreativeModeSlotPacket w = new WrappedServerboundSetCreativeModeSlotPacket();
        w.setSlotNum((short) 5);
        w.setItemStack(new ItemStack(Material.STONE));

        assertEquals(PacketType.Play.Client.SET_CREATIVE_SLOT, w.getHandle().getType());

        ServerboundSetCreativeModeSlotPacket p = (ServerboundSetCreativeModeSlotPacket) w.getHandle().getHandle();

        assertEquals(5, p.slotNum());
        assertNotNull(p.itemStack());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundSetCreativeModeSlotPacket nmsPacket = new ServerboundSetCreativeModeSlotPacket(
                5, net.minecraft.world.item.ItemStack.EMPTY
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCreativeModeSlotPacket wrapper = new WrappedServerboundSetCreativeModeSlotPacket(container);

        assertEquals((short) 5, wrapper.getSlotNum());
        assertNotNull(wrapper.getItemStack());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSetCreativeModeSlotPacket nmsPacket = new ServerboundSetCreativeModeSlotPacket(
                5, net.minecraft.world.item.ItemStack.EMPTY
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetCreativeModeSlotPacket wrapper = new WrappedServerboundSetCreativeModeSlotPacket(container);

        wrapper.setItemStack(new ItemStack(Material.STONE, 3));

        assertEquals((short) 5, wrapper.getSlotNum());
        assertNotNull(wrapper.getItemStack());
        assertEquals(Material.STONE, wrapper.getItemStack().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetCreativeModeSlotPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
