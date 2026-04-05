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
        WrappedServerboundRecipeBookSeenRecipePacket w = new WrappedServerboundRecipeBookSeenRecipePacket(42);

        assertEquals(PacketType.Play.Client.RECIPE_DISPLAYED, w.getHandle().getType());

        assertEquals(42, w.getRecipeIndex());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundRecipeBookSeenRecipePacket w = new WrappedServerboundRecipeBookSeenRecipePacket();

        assertEquals(PacketType.Play.Client.RECIPE_DISPLAYED, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundRecipeBookSeenRecipePacket source = new WrappedServerboundRecipeBookSeenRecipePacket(42);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundRecipeBookSeenRecipePacket wrapper = new WrappedServerboundRecipeBookSeenRecipePacket(container);

        assertEquals(42, wrapper.getRecipeIndex());

        wrapper.setRecipeIndex(99);

        assertEquals(99, wrapper.getRecipeIndex());
        assertEquals(99, source.getRecipeIndex());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundRecipeBookSeenRecipePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
