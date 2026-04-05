package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetPlayerInventoryPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetPlayerInventoryPacket w = new WrappedClientboundSetPlayerInventoryPacket(3, new ItemStack(Material.STONE));
        assertEquals(PacketType.Play.Server.SET_PLAYER_INVENTORY, w.getHandle().getType());
        assertEquals(3, w.getSlot());
        assertEquals(new ItemStack(Material.STONE), w.getContents());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetPlayerInventoryPacket w = new WrappedClientboundSetPlayerInventoryPacket();
        assertEquals(PacketType.Play.Server.SET_PLAYER_INVENTORY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetPlayerInventoryPacket src = new WrappedClientboundSetPlayerInventoryPacket(3, new ItemStack(Material.STONE));
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundSetPlayerInventoryPacket wrapper = new WrappedClientboundSetPlayerInventoryPacket(container);
        assertEquals(3, wrapper.getSlot());
        assertEquals(new ItemStack(Material.STONE), wrapper.getContents());
        wrapper.setSlot(9);
        wrapper.setContents(new ItemStack(Material.DIRT));
        assertEquals(9, wrapper.getSlot());
        assertEquals(new ItemStack(Material.DIRT), wrapper.getContents());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetPlayerInventoryPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
