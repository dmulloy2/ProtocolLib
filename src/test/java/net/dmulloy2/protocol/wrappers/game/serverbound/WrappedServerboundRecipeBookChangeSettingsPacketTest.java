package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundRecipeBookChangeSettingsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundRecipeBookChangeSettingsPacket w = new WrappedServerboundRecipeBookChangeSettingsPacket(true, false);

        assertEquals(PacketType.Play.Client.RECIPE_SETTINGS, w.getHandle().getType());

        assertTrue(w.isOpen());
        assertFalse(w.isFiltering());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundRecipeBookChangeSettingsPacket w = new WrappedServerboundRecipeBookChangeSettingsPacket();

        assertEquals(PacketType.Play.Client.RECIPE_SETTINGS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundRecipeBookChangeSettingsPacket source = new WrappedServerboundRecipeBookChangeSettingsPacket(true, false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundRecipeBookChangeSettingsPacket wrapper = new WrappedServerboundRecipeBookChangeSettingsPacket(container);

        assertTrue(wrapper.isOpen());
        assertFalse(wrapper.isFiltering());

        wrapper.setOpen(false);
        wrapper.setFiltering(true);

        assertFalse(wrapper.isOpen());
        assertTrue(wrapper.isFiltering());

        assertFalse(source.isOpen());
        assertTrue(source.isFiltering());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundRecipeBookChangeSettingsPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
