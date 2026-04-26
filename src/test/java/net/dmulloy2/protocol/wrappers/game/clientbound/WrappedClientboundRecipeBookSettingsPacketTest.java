package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.wrappers.game.clientbound.WrappedClientboundRecipeBookSettingsPacket.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRecipeBookSettingsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRecipeBookSettingsPacket w = new WrappedClientboundRecipeBookSettingsPacket();
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_SETTINGS, w.getHandle().getType());
    }

    @Test
    void testSetAndGetBookSettings() {
        WrappedClientboundRecipeBookSettingsPacket w = new WrappedClientboundRecipeBookSettingsPacket();

        WrappedRecipeBookSettings settings = new WrappedRecipeBookSettings();
        settings.crafting = new WrappedTypeSettings(true, false);
        settings.furnace = new WrappedTypeSettings(false, true);
        settings.blastFurnace = new WrappedTypeSettings(true, true);
        settings.smoker = new WrappedTypeSettings(false, false);

        w.setBookSettings(settings);
        WrappedRecipeBookSettings result = w.getBookSettings();

        assertTrue(result.crafting.open);
        assertFalse(result.crafting.filtering);
        assertFalse(result.furnace.open);
        assertTrue(result.furnace.filtering);
        assertTrue(result.blastFurnace.open);
        assertTrue(result.blastFurnace.filtering);
        assertFalse(result.smoker.open);
        assertFalse(result.smoker.filtering);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRecipeBookSettingsPacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
