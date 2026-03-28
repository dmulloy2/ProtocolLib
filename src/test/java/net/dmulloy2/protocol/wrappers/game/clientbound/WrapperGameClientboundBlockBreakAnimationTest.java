package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundBlockBreakAnimationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        BlockPosition pos = new BlockPosition(10, 64, -20);

        WrapperGameClientboundBlockBreakAnimation w = new WrapperGameClientboundBlockBreakAnimation();
        w.setId(1);
        w.setPos(pos);
        w.setDestroyStage(5);

        assertEquals(1,   w.getId());
        assertEquals(pos, w.getPos());
        assertEquals(5,   w.getDestroyStage());
        assertEquals(PacketType.Play.Server.BLOCK_BREAK_ANIMATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        BlockPosition pos = new BlockPosition(0, 100, 0);

        PacketContainer raw = new PacketContainer(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);
        raw.getBlockPositionModifier().write(0, pos);
        raw.getIntegers().write(1, 9);

        WrapperGameClientboundBlockBreakAnimation w = new WrapperGameClientboundBlockBreakAnimation(raw);
        assertEquals(7,   w.getId());
        assertEquals(pos, w.getPos());
        assertEquals(9,   w.getDestroyStage());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundBlockBreakAnimation w = new WrapperGameClientboundBlockBreakAnimation();
        w.setId(1);
        w.setPos(new BlockPosition(0, 0, 0));
        w.setDestroyStage(0);

        new WrapperGameClientboundBlockBreakAnimation(w.getHandle()).setDestroyStage(8);

        assertEquals(8, w.getDestroyStage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundBlockBreakAnimation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
