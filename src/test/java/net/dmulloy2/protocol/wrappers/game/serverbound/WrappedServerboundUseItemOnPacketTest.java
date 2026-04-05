package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import net.minecraft.world.InteractionHand;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundUseItemOnPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static void assertMovingObjectPositionBlockEquals(MovingObjectPositionBlock expected, MovingObjectPositionBlock actual) {
        if (expected == null || actual == null) {
            assertEquals(expected, actual);
            return;
        }
        assertEquals(expected.getBlockPosition(), actual.getBlockPosition());
        assertEquals(expected.getPosVector(), actual.getPosVector());
        assertEquals(expected.getDirection(), actual.getDirection());
        assertEquals(expected.isInsideBlock(), actual.isInsideBlock());
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket(EnumWrappers.Hand.OFF_HAND, new MovingObjectPositionBlock(new BlockPosition(4, 5, 6), new Vector(0.5, 0.0, 0.5), EnumWrappers.Direction.DOWN, true), 5);

        assertEquals(PacketType.Play.Client.USE_ITEM_ON, w.getHandle().getType());

        assertEquals(EnumWrappers.Hand.OFF_HAND, w.getHand());
        assertMovingObjectPositionBlockEquals(new MovingObjectPositionBlock(new BlockPosition(4, 5, 6), new Vector(0.5, 0.0, 0.5), EnumWrappers.Direction.DOWN, true), w.getBlockHit());
        assertEquals(5, w.getSequence());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket();

        assertEquals(PacketType.Play.Client.USE_ITEM_ON, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundUseItemOnPacket source = new WrappedServerboundUseItemOnPacket(EnumWrappers.Hand.OFF_HAND, new MovingObjectPositionBlock(new BlockPosition(4, 5, 6), new Vector(0.5, 0.0, 0.5), EnumWrappers.Direction.DOWN, true), 5);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemOnPacket wrapper = new WrappedServerboundUseItemOnPacket(container);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertMovingObjectPositionBlockEquals(new MovingObjectPositionBlock(new BlockPosition(4, 5, 6), new Vector(0.5, 0.0, 0.5), EnumWrappers.Direction.DOWN, true), wrapper.getBlockHit());
        assertEquals(5, wrapper.getSequence());

        wrapper.setHand(EnumWrappers.Hand.MAIN_HAND);
        wrapper.setBlockHit(new MovingObjectPositionBlock(new BlockPosition(10, 20, 30), new Vector(0.5, 0.5, 0.5), EnumWrappers.Direction.SOUTH, true));
        wrapper.setSequence(0);

        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());
        assertMovingObjectPositionBlockEquals(new MovingObjectPositionBlock(new BlockPosition(10, 20, 30), new Vector(0.5, 0.5, 0.5), EnumWrappers.Direction.SOUTH, true), wrapper.getBlockHit());
        assertEquals(0, wrapper.getSequence());

        assertEquals(EnumWrappers.Hand.MAIN_HAND, source.getHand());
        assertMovingObjectPositionBlockEquals(new MovingObjectPositionBlock(new BlockPosition(10, 20, 30), new Vector(0.5, 0.5, 0.5), EnumWrappers.Direction.SOUTH, true), source.getBlockHit());
        assertEquals(0, source.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemOnPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
