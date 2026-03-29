package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundUseItemOnPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        // NMS constructor complex (BlockHitResult); use wrapper-based approach
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);
        w.setSequence(1);

        assertEquals(PacketType.Play.Client.USE_ITEM_ON, w.getHandle().getType());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
        assertEquals(1, w.getSequence());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedServerboundUseItemOnPacket src = new WrappedServerboundUseItemOnPacket();
        src.setHand(EnumWrappers.Hand.OFF_HAND);
        src.setSequence(5);

        WrappedServerboundUseItemOnPacket wrapper = new WrappedServerboundUseItemOnPacket(src.getHandle());

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertEquals(5, wrapper.getSequence());
        assertNotNull(wrapper.getBlockHit());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);
        w.setSequence(1);

        w.setHand(EnumWrappers.Hand.OFF_HAND);

        assertEquals(EnumWrappers.Hand.OFF_HAND, w.getHand());
        assertEquals(1, w.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemOnPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
