package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetJigsawBlockPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static final BlockPosition POS = new BlockPosition(1, 2, 3);
    private static final MinecraftKey NAME   = new MinecraftKey("minecraft", "top");
    private static final MinecraftKey TARGET = new MinecraftKey("minecraft", "bottom");
    private static final MinecraftKey POOL   = new MinecraftKey("minecraft", "village/plains/houses");

    private static WrappedServerboundSetJigsawBlockPacket makePacket() {
        return new WrappedServerboundSetJigsawBlockPacket(
                POS, NAME, TARGET, POOL, "minecraft:air",
                WrappedServerboundSetJigsawBlockPacket.JointType.ROLLABLE, 7, 5);
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetJigsawBlockPacket w = makePacket();

        assertEquals(PacketType.Play.Client.SET_JIGSAW, w.getHandle().getType());

        assertEquals(POS, w.getPos());
        assertEquals(NAME, w.getName());
        assertEquals(TARGET, w.getTarget());
        assertEquals(POOL, w.getPool());
        assertEquals("minecraft:air", w.getFinalState());
        assertEquals(WrappedServerboundSetJigsawBlockPacket.JointType.ROLLABLE, w.getJoint());
        assertEquals(7, w.getSelectionPriority());
        assertEquals(5, w.getPlacementPriority());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetJigsawBlockPacket w = new WrappedServerboundSetJigsawBlockPacket();

        assertEquals(PacketType.Play.Client.SET_JIGSAW, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetJigsawBlockPacket source = makePacket();
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetJigsawBlockPacket wrapper = new WrappedServerboundSetJigsawBlockPacket(container);

        assertEquals(POS, wrapper.getPos());
        assertEquals(NAME, wrapper.getName());
        assertEquals(TARGET, wrapper.getTarget());
        assertEquals(POOL, wrapper.getPool());
        assertEquals("minecraft:air", wrapper.getFinalState());
        assertEquals(WrappedServerboundSetJigsawBlockPacket.JointType.ROLLABLE, wrapper.getJoint());
        assertEquals(7, wrapper.getSelectionPriority());
        assertEquals(5, wrapper.getPlacementPriority());

        MinecraftKey newName = new MinecraftKey("minecraft", "side");
        BlockPosition newPos = new BlockPosition(10, 20, 30);

        wrapper.setPos(newPos);
        wrapper.setName(newName);
        wrapper.setFinalState("modified");
        wrapper.setJoint(WrappedServerboundSetJigsawBlockPacket.JointType.ALIGNED);
        wrapper.setSelectionPriority(-5);
        wrapper.setPlacementPriority(0);

        assertEquals(newPos, wrapper.getPos());
        assertEquals(newName, wrapper.getName());
        assertEquals("modified", wrapper.getFinalState());
        assertEquals(WrappedServerboundSetJigsawBlockPacket.JointType.ALIGNED, wrapper.getJoint());
        assertEquals(-5, wrapper.getSelectionPriority());
        assertEquals(0, wrapper.getPlacementPriority());

        assertEquals(newPos, source.getPos());
        assertEquals(newName, source.getName());
        assertEquals("modified", source.getFinalState());
        assertEquals(WrappedServerboundSetJigsawBlockPacket.JointType.ALIGNED, source.getJoint());
        assertEquals(-5, source.getSelectionPriority());
        assertEquals(0, source.getPlacementPriority());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetJigsawBlockPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
