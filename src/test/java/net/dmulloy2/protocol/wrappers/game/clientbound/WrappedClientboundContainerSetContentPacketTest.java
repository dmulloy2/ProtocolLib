package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundContainerSetContentPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundContainerSetContentPacket w = new WrappedClientboundContainerSetContentPacket(3, 7, List.of(new ItemStack(Material.STONE)), new ItemStack(Material.STONE));

        assertEquals(PacketType.Play.Server.WINDOW_ITEMS, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
        assertEquals(7, w.getStateId());
        assertEquals(List.of(new ItemStack(Material.STONE)), w.getItems());
        assertEquals(new ItemStack(Material.STONE), w.getCarriedItem());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundContainerSetContentPacket w = new WrappedClientboundContainerSetContentPacket();

        assertEquals(PacketType.Play.Server.WINDOW_ITEMS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundContainerSetContentPacket source = new WrappedClientboundContainerSetContentPacket(3, 7, List.of(new ItemStack(Material.STONE)), new ItemStack(Material.STONE));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerSetContentPacket wrapper = new WrappedClientboundContainerSetContentPacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertEquals(7, wrapper.getStateId());
        assertEquals(List.of(new ItemStack(Material.STONE)), wrapper.getItems());
        assertEquals(new ItemStack(Material.STONE), wrapper.getCarriedItem());

        wrapper.setContainerId(9);
        wrapper.setStateId(-5);
        wrapper.setItems(List.of(new ItemStack(Material.DIRT)));
        wrapper.setCarriedItem(new ItemStack(Material.DIRT));

        assertEquals(9, wrapper.getContainerId());
        assertEquals(-5, wrapper.getStateId());
        assertEquals(List.of(new ItemStack(Material.DIRT)), wrapper.getItems());
        assertEquals(new ItemStack(Material.DIRT), wrapper.getCarriedItem());

        assertEquals(9, source.getContainerId());
        assertEquals(-5, source.getStateId());
        assertEquals(List.of(new ItemStack(Material.DIRT)), source.getItems());
        assertEquals(new ItemStack(Material.DIRT), source.getCarriedItem());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetContentPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
