package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetJigsawBlockPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetJigsawBlockPacket w = new WrappedServerboundSetJigsawBlockPacket("hello", 7, 5, new BlockPosition(1, 2, 3));

        assertEquals(PacketType.Play.Client.SET_JIGSAW, w.getHandle().getType());

        assertEquals("hello", w.getFinalState());
        assertEquals(7, w.getSelectionPriority());
        assertEquals(5, w.getPlacementPriority());
        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetJigsawBlockPacket w = new WrappedServerboundSetJigsawBlockPacket();

        assertEquals(PacketType.Play.Client.SET_JIGSAW, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetJigsawBlockPacket source = new WrappedServerboundSetJigsawBlockPacket("hello", 7, 5, new BlockPosition(1, 2, 3));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetJigsawBlockPacket wrapper = new WrappedServerboundSetJigsawBlockPacket(container);

        assertEquals("hello", wrapper.getFinalState());
        assertEquals(7, wrapper.getSelectionPriority());
        assertEquals(5, wrapper.getPlacementPriority());
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());

        wrapper.setFinalState("modified");
        wrapper.setSelectionPriority(-5);
        wrapper.setPlacementPriority(0);
        wrapper.setPos(new BlockPosition(10, 20, 30));

        assertEquals("modified", wrapper.getFinalState());
        assertEquals(-5, wrapper.getSelectionPriority());
        assertEquals(0, wrapper.getPlacementPriority());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());

        assertEquals("modified", source.getFinalState());
        assertEquals(-5, source.getSelectionPriority());
        assertEquals(0, source.getPlacementPriority());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetJigsawBlockPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
