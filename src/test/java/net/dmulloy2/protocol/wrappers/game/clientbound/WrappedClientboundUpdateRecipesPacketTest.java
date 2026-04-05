package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateRecipesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Server.RECIPE_UPDATE, new WrappedClientboundUpdateRecipesPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateRecipesPacket w = new WrappedClientboundUpdateRecipesPacket();

        assertEquals(PacketType.Play.Server.RECIPE_UPDATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.RECIPE_UPDATE);
        WrappedClientboundUpdateRecipesPacket wrapper = new WrappedClientboundUpdateRecipesPacket(container);

        assertEquals(PacketType.Play.Server.RECIPE_UPDATE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateRecipesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
