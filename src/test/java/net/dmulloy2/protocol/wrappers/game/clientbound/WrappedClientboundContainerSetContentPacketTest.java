package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundContainerSetContentPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        List<ItemStack> items = Arrays.asList(
                new ItemStack(Material.STONE, 1),
                new ItemStack(Material.DIRT, 2)
        );
        ItemStack carried = new ItemStack(Material.DIAMOND, 1);

        WrappedClientboundContainerSetContentPacket w = new WrappedClientboundContainerSetContentPacket();
        w.setContainerId(5);
        w.setStateId(3);
        w.setItems(items);
        w.setCarriedItem(carried);

        assertEquals(PacketType.Play.Server.WINDOW_ITEMS, w.getHandle().getType());

        ClientboundContainerSetContentPacket p = (ClientboundContainerSetContentPacket) w.getHandle().getHandle();

        assertEquals(5, p.containerId());
        assertEquals(3, p.stateId());
        assertNotNull(p.items());
        assertNotNull(p.carriedItem());
    }

    @Test
    void testReadFromExistingPacket() {
        List<ItemStack> items = Arrays.asList(
                new ItemStack(Material.OAK_LOG, 5),
                new ItemStack(Material.COBBLESTONE, 10)
        );
        ItemStack carried = new ItemStack(Material.AIR);

        WrappedClientboundContainerSetContentPacket src = new WrappedClientboundContainerSetContentPacket();
        src.setContainerId(2);
        src.setStateId(1);
        src.setItems(items);
        src.setCarriedItem(carried);

        PacketContainer container = src.getHandle();
        WrappedClientboundContainerSetContentPacket wrapper =
                new WrappedClientboundContainerSetContentPacket(container);

        assertEquals(2, wrapper.getContainerId());
        assertEquals(1, wrapper.getStateId());
        assertNotNull(wrapper.getItems());
        assertNotNull(wrapper.getCarriedItem());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundContainerSetContentPacket w = new WrappedClientboundContainerSetContentPacket();
        w.setContainerId(1);
        w.setStateId(0);
        w.setItems(List.of());
        w.setCarriedItem(new ItemStack(Material.AIR));

        w.setContainerId(10);
        w.setStateId(7);

        assertEquals(10, w.getContainerId());
        assertEquals(7,  w.getStateId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetContentPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
