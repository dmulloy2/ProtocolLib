package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSetSlotTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSetSlot w = new WrapperGameClientboundSetSlot();
        ItemStack item = new ItemStack(Material.DIAMOND, 5);
        w.setWindowId(0);
        w.setStateId(1);
        w.setSlot(36);
        w.setItem(item);
        assertEquals(0, w.getWindowId());
        assertEquals(1, w.getStateId());
        assertEquals(36, w.getSlot());
        assertNotNull(w.getItem());
        assertEquals(Material.DIAMOND, w.getItem().getType());
        assertEquals(PacketType.Play.Server.SET_SLOT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 1);
        raw.getIntegers().write(1, 0);
        raw.getIntegers().write(2, 5);
        raw.getItemModifier().write(0, new ItemStack(Material.GOLD_INGOT, 1));

        WrapperGameClientboundSetSlot w = new WrapperGameClientboundSetSlot(raw);
        assertEquals(1, w.getWindowId());
        assertEquals(5, w.getSlot());
        assertEquals(Material.GOLD_INGOT, w.getItem().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSetSlot w = new WrapperGameClientboundSetSlot();
        w.setSlot(0);

        new WrapperGameClientboundSetSlot(w.getHandle()).setSlot(9);

        assertEquals(9, w.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSetSlot(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
