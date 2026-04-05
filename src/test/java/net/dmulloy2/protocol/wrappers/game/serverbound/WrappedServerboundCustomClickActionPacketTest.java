package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundCustomClickActionPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundCustomClickActionPacket w = new WrappedServerboundCustomClickActionPacket(
                new MinecraftKey("minecraft", "stone"), Optional.empty());
        assertEquals(PacketType.Play.Client.CUSTOM_CLICK_ACTION, w.getHandle().getType());
        assertEquals(new MinecraftKey("minecraft", "stone"), w.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundCustomClickActionPacket w = new WrappedServerboundCustomClickActionPacket();
        assertEquals(PacketType.Play.Client.CUSTOM_CLICK_ACTION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundCustomClickActionPacket src = new WrappedServerboundCustomClickActionPacket(
                new MinecraftKey("minecraft", "stone"), Optional.empty());
        PacketContainer container = PacketContainer.fromPacket(src.getHandle().getHandle());
        WrappedServerboundCustomClickActionPacket wrapper = new WrappedServerboundCustomClickActionPacket(container);
        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getId());
        wrapper.setId(new MinecraftKey("minecraft", "dirt"));
        assertEquals(new MinecraftKey("minecraft", "dirt"), wrapper.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundCustomClickActionPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
