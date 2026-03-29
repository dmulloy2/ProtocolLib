package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
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
    void testCreate() {
        WrappedServerboundPlayerActionPacket w = new WrappedServerboundPlayerActionPacket();
        w.setPos(new BlockPosition(1, 64, -1));
        w.setDirection(EnumWrappers.Direction.NORTH);
        w.setAction(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK);
        w.setSequence(3);

        assertEquals(PacketType.Play.Client.BLOCK_DIG, w.getHandle().getType());

        ServerboundPlayerActionPacket p = (ServerboundPlayerActionPacket) w.getHandle().getHandle();

        assertNotNull(p.getPos());
        assertEquals(Direction.NORTH, p.getDirection());
        assertEquals(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, p.getAction());
        assertEquals(3, p.getSequence());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundPlayerActionPacket nmsPacket = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                new net.minecraft.core.BlockPos(1, 64, -1),
                Direction.NORTH,
                3
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerActionPacket wrapper = new WrappedServerboundPlayerActionPacket(container);

        assertEquals(new BlockPosition(1, 64, -1), wrapper.getPos());
        assertEquals(EnumWrappers.Direction.NORTH, wrapper.getDirection());
        assertEquals(EnumWrappers.PlayerDigType.START_DESTROY_BLOCK, wrapper.getAction());
        assertEquals(3, wrapper.getSequence());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundPlayerActionPacket nmsPacket = new ServerboundPlayerActionPacket(
                ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                new net.minecraft.core.BlockPos(1, 64, -1),
                Direction.NORTH,
                3
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerActionPacket wrapper = new WrappedServerboundPlayerActionPacket(container);

        wrapper.setSequence(10);

        assertEquals(10, wrapper.getSequence());
        assertEquals(new BlockPosition(1, 64, -1), wrapper.getPos());
        assertEquals(EnumWrappers.Direction.NORTH, wrapper.getDirection());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerActionPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
