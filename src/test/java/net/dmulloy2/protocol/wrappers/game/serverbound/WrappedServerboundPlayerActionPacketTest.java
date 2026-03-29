package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
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
        BlockPosition pos = new BlockPosition(10, 64, -5);
        WrappedServerboundPlayerActionPacket w = new WrappedServerboundPlayerActionPacket(
                pos, EnumWrappers.Direction.UP, EnumWrappers.PlayerDigType.START_DESTROY_BLOCK, 42);

        assertEquals(PacketType.Play.Client.BLOCK_DIG, w.getHandle().getType());

        ServerboundPlayerActionPacket p = (ServerboundPlayerActionPacket) w.getHandle().getHandle();

        assertEquals(new BlockPos(10, 64, -5), p.getPos());
        assertEquals(Direction.UP, p.getDirection());
        assertEquals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, p.getAction());
        assertEquals(42, p.getSequence());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerActionPacket w = new WrappedServerboundPlayerActionPacket();

        assertEquals(PacketType.Play.Client.BLOCK_DIG, w.getHandle().getType());

        assertNotNull(w.getPos());
        assertNotNull(w.getDirection());
        assertNotNull(w.getAction());
        assertEquals(0, w.getSequence());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundPlayerActionPacket nmsPacket = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                new BlockPos(1, 2, 3), Direction.NORTH, 1);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerActionPacket wrapper = new WrappedServerboundPlayerActionPacket(container);

        assertEquals(new BlockPos(1, 2, 3), new BlockPos(wrapper.getPos().getX(), wrapper.getPos().getY(), wrapper.getPos().getZ()));
        assertEquals(EnumWrappers.Direction.NORTH, wrapper.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK, wrapper.getAction());
        assertEquals(1, wrapper.getSequence());

        wrapper.setPos(new BlockPosition(9, 8, 7));
        wrapper.setDirection(EnumWrappers.Direction.SOUTH);
        wrapper.setAction(EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK);
        wrapper.setSequence(99);

        assertEquals(new BlockPos(9, 8, 7), nmsPacket.getPos());
        assertEquals(Direction.SOUTH, nmsPacket.getDirection());
        assertEquals(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, nmsPacket.getAction());
        assertEquals(99, nmsPacket.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerActionPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
