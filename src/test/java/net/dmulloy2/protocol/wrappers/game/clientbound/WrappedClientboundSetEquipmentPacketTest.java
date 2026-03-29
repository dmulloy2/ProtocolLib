package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEquipmentPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots = List.of(
                new Pair<>(EnumWrappers.ItemSlot.MAINHAND, new ItemStack(Material.DIAMOND_SWORD)),
                new Pair<>(EnumWrappers.ItemSlot.HEAD,     new ItemStack(Material.DIAMOND_HELMET))
        );

        WrappedClientboundSetEquipmentPacket w = new WrappedClientboundSetEquipmentPacket();
        w.setEntityId(123);
        w.setSlots(slots);

        assertEquals(PacketType.Play.Server.ENTITY_EQUIPMENT, w.getHandle().getType());

        ClientboundSetEquipmentPacket p = (ClientboundSetEquipmentPacket) w.getHandle().getHandle();

        assertEquals(123, p.getEntity());
        assertNotNull(p.getSlots());
    }

    @Test
    void testReadFromExistingPacket() {
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> slots = List.of(
                new Pair<>(EnumWrappers.ItemSlot.OFFHAND, new ItemStack(Material.SHIELD))
        );

        WrappedClientboundSetEquipmentPacket src = new WrappedClientboundSetEquipmentPacket();
        src.setEntityId(77);
        src.setSlots(slots);

        PacketContainer container = src.getHandle();
        WrappedClientboundSetEquipmentPacket wrapper = new WrappedClientboundSetEquipmentPacket(container);

        assertEquals(77, wrapper.getEntityId());
        assertNotNull(wrapper.getSlots());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetEquipmentPacket w = new WrappedClientboundSetEquipmentPacket();
        w.setEntityId(10);
        w.setSlots(List.of());

        w.setEntityId(999);

        assertEquals(999, w.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEquipmentPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
