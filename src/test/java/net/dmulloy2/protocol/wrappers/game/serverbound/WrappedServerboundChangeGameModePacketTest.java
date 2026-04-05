package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundChangeGameModePacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundChangeGameModePacket w = new WrappedServerboundChangeGameModePacket(EnumWrappers.NativeGameMode.SURVIVAL);
        assertEquals(PacketType.Play.Client.CHANGE_GAME_MODE, w.getHandle().getType());
        assertEquals(EnumWrappers.NativeGameMode.SURVIVAL, w.getMode());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundChangeGameModePacket w = new WrappedServerboundChangeGameModePacket();
        assertEquals(PacketType.Play.Client.CHANGE_GAME_MODE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundChangeGameModePacket src = new WrappedServerboundChangeGameModePacket(EnumWrappers.NativeGameMode.SURVIVAL);
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundChangeGameModePacket wrapper = new WrappedServerboundChangeGameModePacket(container);
        assertEquals(EnumWrappers.NativeGameMode.SURVIVAL, wrapper.getMode());
        wrapper.setMode(EnumWrappers.NativeGameMode.CREATIVE);
        assertEquals(EnumWrappers.NativeGameMode.CREATIVE, wrapper.getMode());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundChangeGameModePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
