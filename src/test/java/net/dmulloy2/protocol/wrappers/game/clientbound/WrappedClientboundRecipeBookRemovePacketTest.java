package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRecipeBookRemovePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_REMOVE, new WrappedClientboundRecipeBookRemovePacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRecipeBookRemovePacket w = new WrappedClientboundRecipeBookRemovePacket();
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_REMOVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.RECIPE_BOOK_REMOVE);
        WrappedClientboundRecipeBookRemovePacket wrapper = new WrappedClientboundRecipeBookRemovePacket(container);
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_REMOVE, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRecipeBookRemovePacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
