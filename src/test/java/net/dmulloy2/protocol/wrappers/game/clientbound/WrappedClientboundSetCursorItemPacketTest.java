package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetCursorItemPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetCursorItemPacket w = new WrappedClientboundSetCursorItemPacket(new ItemStack(Material.STONE));
        assertEquals(PacketType.Play.Server.SET_CURSOR_ITEM, w.getHandle().getType());
        assertEquals(new ItemStack(Material.STONE), w.getContents());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetCursorItemPacket w = new WrappedClientboundSetCursorItemPacket();
        assertEquals(PacketType.Play.Server.SET_CURSOR_ITEM, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetCursorItemPacket src = new WrappedClientboundSetCursorItemPacket(new ItemStack(Material.STONE));
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedClientboundSetCursorItemPacket wrapper = new WrappedClientboundSetCursorItemPacket(container);
        assertEquals(new ItemStack(Material.STONE), wrapper.getContents());
        wrapper.setContents(new ItemStack(Material.DIRT));
        assertEquals(new ItemStack(Material.DIRT), wrapper.getContents());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetCursorItemPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
