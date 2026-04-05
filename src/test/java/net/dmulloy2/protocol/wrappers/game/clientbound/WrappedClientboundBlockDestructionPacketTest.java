package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockDestructionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundBlockDestructionPacket w = new WrappedClientboundBlockDestructionPacket(3, new BlockPosition(4, 5, 6), 5);

        assertEquals(PacketType.Play.Server.BLOCK_BREAK_ANIMATION, w.getHandle().getType());

        assertEquals(3, w.getId());
        assertEquals(new BlockPosition(4, 5, 6), w.getPos());
        assertEquals(5, w.getDestroyStage());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBlockDestructionPacket w = new WrappedClientboundBlockDestructionPacket();

        assertEquals(PacketType.Play.Server.BLOCK_BREAK_ANIMATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundBlockDestructionPacket source = new WrappedClientboundBlockDestructionPacket(3, new BlockPosition(4, 5, 6), 5);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockDestructionPacket wrapper = new WrappedClientboundBlockDestructionPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(new BlockPosition(4, 5, 6), wrapper.getPos());
        assertEquals(5, wrapper.getDestroyStage());

        wrapper.setId(9);
        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setDestroyStage(0);

        assertEquals(9, wrapper.getId());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(0, wrapper.getDestroyStage());

        assertEquals(9, source.getId());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(0, source.getDestroyStage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockDestructionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
