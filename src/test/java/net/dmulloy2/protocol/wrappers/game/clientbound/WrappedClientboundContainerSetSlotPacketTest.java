package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
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
    void testCreate() {
        WrappedClientboundContainerSetSlotPacket w = new WrappedClientboundContainerSetSlotPacket();
        ItemStack item = new ItemStack(Material.DIAMOND, 5);
        w.setWindowId(0);
        w.setStateId(1);
        w.setSlot(36);
        w.setItem(item);

        assertEquals(PacketType.Play.Server.SET_SLOT, w.getHandle().getType());

        ClientboundContainerSetSlotPacket p = (ClientboundContainerSetSlotPacket) w.getHandle().getHandle();

        assertEquals(0, p.getContainerId());
        assertEquals(1, p.getStateId());
        assertEquals(36, p.getSlot());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 1);
        container.getIntegers().write(1, 0);
        container.getIntegers().write(2, 5);
        container.getItemModifier().write(0, new ItemStack(Material.GOLD_INGOT, 1));

        WrappedClientboundContainerSetSlotPacket wrapper = new WrappedClientboundContainerSetSlotPacket(container);

        assertEquals(1, wrapper.getWindowId());
        assertEquals(5, wrapper.getSlot());
        assertEquals(Material.GOLD_INGOT, wrapper.getItem().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 1);
        container.getIntegers().write(1, 0);
        container.getIntegers().write(2, 5);
        container.getItemModifier().write(0, new ItemStack(Material.GOLD_INGOT, 1));

        WrappedClientboundContainerSetSlotPacket wrapper = new WrappedClientboundContainerSetSlotPacket(container);
        wrapper.setSlot(9);

        assertEquals(1, wrapper.getWindowId());
        assertEquals(9, wrapper.getSlot());
        assertEquals(Material.GOLD_INGOT, wrapper.getItem().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetSlotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
