package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSectionBlocksUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSectionBlocksUpdatePacket w = new WrappedClientboundSectionBlocksUpdatePacket(new BlockPosition(1, 2, 3), new short[] { 4, 5, 6 }, new WrappedBlockData[0]);

        assertEquals(PacketType.Play.Server.MULTI_BLOCK_CHANGE, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getSectionPos());
        assertArrayEquals(new short[] { 4, 5, 6 }, w.getPositions());
        assertArrayEquals(new WrappedBlockData[0], w.getStates());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSectionBlocksUpdatePacket w = new WrappedClientboundSectionBlocksUpdatePacket();

        assertEquals(PacketType.Play.Server.MULTI_BLOCK_CHANGE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSectionBlocksUpdatePacket source = new WrappedClientboundSectionBlocksUpdatePacket(new BlockPosition(1, 2, 3), new short[] { 4, 5, 6 }, new WrappedBlockData[0]);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSectionBlocksUpdatePacket wrapper = new WrappedClientboundSectionBlocksUpdatePacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getSectionPos());
        assertArrayEquals(new short[] { 4, 5, 6 }, wrapper.getPositions());
        assertArrayEquals(new WrappedBlockData[0], wrapper.getStates());

        wrapper.setSectionPos(new BlockPosition(10, 20, 30));
        wrapper.setPositions(new short[] { 10, 20, 30 });
        wrapper.setStates(new WrappedBlockData[0]);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getSectionPos());
        assertArrayEquals(new short[] { 10, 20, 30 }, wrapper.getPositions());
        assertArrayEquals(new WrappedBlockData[0], wrapper.getStates());

        assertEquals(new BlockPosition(10, 20, 30), source.getSectionPos());
        assertArrayEquals(new short[] { 10, 20, 30 }, source.getPositions());
        assertArrayEquals(new WrappedBlockData[0], source.getStates());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSectionBlocksUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
