package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRecipeBookSettingsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_SETTINGS, new WrappedClientboundRecipeBookSettingsPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRecipeBookSettingsPacket w = new WrappedClientboundRecipeBookSettingsPacket();
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_SETTINGS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.RECIPE_BOOK_SETTINGS);
        WrappedClientboundRecipeBookSettingsPacket wrapper = new WrappedClientboundRecipeBookSettingsPacket(container);
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_SETTINGS, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRecipeBookSettingsPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
