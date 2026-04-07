package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerActionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPlayerActionPacket w = new WrappedServerboundPlayerActionPacket(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK, new BlockPosition(1, 2, 3), EnumWrappers.Direction.DOWN, 3);

        assertEquals(PacketType.Play.Client.BLOCK_DIG, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(EnumWrappers.Direction.DOWN, w.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK, w.getAction());
        assertEquals(3, w.getSequence());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerActionPacket w = new WrappedServerboundPlayerActionPacket();

        assertEquals(PacketType.Play.Client.BLOCK_DIG, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerActionPacket source = new WrappedServerboundPlayerActionPacket(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK, new BlockPosition(1, 2, 3), EnumWrappers.Direction.DOWN, 3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerActionPacket wrapper = new WrappedServerboundPlayerActionPacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(EnumWrappers.Direction.DOWN, wrapper.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK, wrapper.getAction());
        assertEquals(3, wrapper.getSequence());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setDirection(EnumWrappers.Direction.UP);
        wrapper.setAction(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK);
        wrapper.setSequence(42);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(EnumWrappers.Direction.UP, wrapper.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK, wrapper.getAction());
        assertEquals(42, wrapper.getSequence());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(EnumWrappers.Direction.UP, source.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK, source.getAction());
        assertEquals(42, source.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerActionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
