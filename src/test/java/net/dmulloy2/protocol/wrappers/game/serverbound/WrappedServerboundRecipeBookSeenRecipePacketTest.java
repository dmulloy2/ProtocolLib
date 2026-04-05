package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundRecipeBookSeenRecipePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Play.Client.RECIPE_DISPLAYED, new WrappedServerboundRecipeBookSeenRecipePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundRecipeBookSeenRecipePacket w = new WrappedServerboundRecipeBookSeenRecipePacket();

        assertEquals(PacketType.Play.Client.RECIPE_DISPLAYED, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Client.RECIPE_DISPLAYED);
        WrappedServerboundRecipeBookSeenRecipePacket wrapper = new WrappedServerboundRecipeBookSeenRecipePacket(container);

        assertEquals(PacketType.Play.Client.RECIPE_DISPLAYED, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundRecipeBookSeenRecipePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
