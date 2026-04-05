package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlaceGhostRecipePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlaceGhostRecipePacket w = new WrappedClientboundPlaceGhostRecipePacket(3);

        assertEquals(PacketType.Play.Server.AUTO_RECIPE, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlaceGhostRecipePacket w = new WrappedClientboundPlaceGhostRecipePacket();

        assertEquals(PacketType.Play.Server.AUTO_RECIPE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlaceGhostRecipePacket source = new WrappedClientboundPlaceGhostRecipePacket(3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlaceGhostRecipePacket wrapper = new WrappedClientboundPlaceGhostRecipePacket(container);

        assertEquals(3, wrapper.getContainerId());

        wrapper.setContainerId(9);

        assertEquals(9, wrapper.getContainerId());

        assertEquals(9, source.getContainerId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlaceGhostRecipePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
