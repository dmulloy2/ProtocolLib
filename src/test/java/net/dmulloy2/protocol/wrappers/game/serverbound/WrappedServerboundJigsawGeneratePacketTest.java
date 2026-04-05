package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundJigsawGeneratePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundJigsawGeneratePacket w = new WrappedServerboundJigsawGeneratePacket(new BlockPosition(1, 2, 3), 7, true);

        assertEquals(PacketType.Play.Client.JIGSAW_GENERATE, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(7, w.getLevels());
        assertTrue(w.isKeepJigsaws());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundJigsawGeneratePacket w = new WrappedServerboundJigsawGeneratePacket();

        assertEquals(PacketType.Play.Client.JIGSAW_GENERATE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundJigsawGeneratePacket source = new WrappedServerboundJigsawGeneratePacket(new BlockPosition(1, 2, 3), 7, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundJigsawGeneratePacket wrapper = new WrappedServerboundJigsawGeneratePacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(7, wrapper.getLevels());
        assertTrue(wrapper.isKeepJigsaws());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setLevels(-5);
        wrapper.setKeepJigsaws(false);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(-5, wrapper.getLevels());
        assertFalse(wrapper.isKeepJigsaws());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(-5, source.getLevels());
        assertFalse(source.isKeepJigsaws());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundJigsawGeneratePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
