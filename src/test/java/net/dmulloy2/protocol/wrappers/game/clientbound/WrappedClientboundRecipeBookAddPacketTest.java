package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRecipeBookAddPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        // No full all-args constructor: entries field has no ProtocolLib accessor
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_ADD, new WrappedClientboundRecipeBookAddPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRecipeBookAddPacket w = new WrappedClientboundRecipeBookAddPacket();
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_ADD, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.RECIPE_BOOK_ADD);
        WrappedClientboundRecipeBookAddPacket wrapper = new WrappedClientboundRecipeBookAddPacket(container);
        wrapper.setReplace(true);
        assertTrue(wrapper.isReplace());
        wrapper.setReplace(false);
        assertFalse(wrapper.isReplace());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRecipeBookAddPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
