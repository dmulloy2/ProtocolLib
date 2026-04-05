package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlaceRecipePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPlaceRecipePacket w = new WrappedServerboundPlaceRecipePacket(3, false);

        assertEquals(PacketType.Play.Client.AUTO_RECIPE, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
        assertFalse(w.isUseMaxItems());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlaceRecipePacket w = new WrappedServerboundPlaceRecipePacket();

        assertEquals(PacketType.Play.Client.AUTO_RECIPE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlaceRecipePacket source = new WrappedServerboundPlaceRecipePacket(3, false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlaceRecipePacket wrapper = new WrappedServerboundPlaceRecipePacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertFalse(wrapper.isUseMaxItems());

        wrapper.setContainerId(9);
        wrapper.setUseMaxItems(true);

        assertEquals(9, wrapper.getContainerId());
        assertTrue(wrapper.isUseMaxItems());

        assertEquals(9, source.getContainerId());
        assertTrue(source.isUseMaxItems());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlaceRecipePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
