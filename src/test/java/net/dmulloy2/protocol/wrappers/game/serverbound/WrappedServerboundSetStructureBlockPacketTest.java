package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundSetStructureBlockPacket.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetStructureBlockPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    private static WrappedServerboundSetStructureBlockPacket createFull() {
        return new WrappedServerboundSetStructureBlockPacket(
                new BlockPosition(7, 8, 9),
                UpdateType.SAVE_AREA,
                StructureMode.LOAD,
                "hello",
                new BlockPosition(1, 2, 3),
                new BlockPosition(5, 5, 5),
                Mirror.LEFT_RIGHT,
                Rotation.CLOCKWISE_90,
                "world",
                true, true, false, true,
                0.75f, 42L);
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetStructureBlockPacket w = createFull();

        assertEquals(PacketType.Play.Client.STRUCT, w.getHandle().getType());

        assertEquals(new BlockPosition(7, 8, 9), w.getPos());
        assertEquals(UpdateType.SAVE_AREA, w.getUpdateType());
        assertEquals(StructureMode.LOAD, w.getMode());
        assertEquals("hello", w.getName());
        assertEquals(new BlockPosition(1, 2, 3), w.getOffset());
        assertEquals(new BlockPosition(5, 5, 5), w.getSize());
        assertEquals(Mirror.LEFT_RIGHT, w.getMirror());
        assertEquals(Rotation.CLOCKWISE_90, w.getRotation());
        assertEquals("world", w.getData());
        assertTrue(w.isIgnoreEntities());
        assertTrue(w.isStrict());
        assertFalse(w.isShowAir());
        assertTrue(w.isShowBoundingBox());
        assertEquals(0.75f, w.getIntegrity(), 1e-4f);
        assertEquals(42L, w.getSeed());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetStructureBlockPacket w = new WrappedServerboundSetStructureBlockPacket();
        assertEquals(PacketType.Play.Client.STRUCT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetStructureBlockPacket source = createFull();
        PacketContainer container = PacketContainer.fromPacket(source.getHandle().getHandle());
        WrappedServerboundSetStructureBlockPacket w = new WrappedServerboundSetStructureBlockPacket(container);

        assertEquals(new BlockPosition(7, 8, 9), w.getPos());
        assertEquals(UpdateType.SAVE_AREA, w.getUpdateType());
        assertEquals(StructureMode.LOAD, w.getMode());
        assertEquals("hello", w.getName());
        assertEquals(new BlockPosition(1, 2, 3), w.getOffset());
        assertEquals(new BlockPosition(5, 5, 5), w.getSize());
        assertEquals(Mirror.LEFT_RIGHT, w.getMirror());
        assertEquals(Rotation.CLOCKWISE_90, w.getRotation());
        assertEquals("world", w.getData());
        assertTrue(w.isIgnoreEntities());
        assertTrue(w.isStrict());
        assertFalse(w.isShowAir());
        assertTrue(w.isShowBoundingBox());
        assertEquals(0.75f, w.getIntegrity(), 1e-4f);
        assertEquals(42L, w.getSeed());

        w.setPos(new BlockPosition(0, 0, 0));
        w.setUpdateType(UpdateType.LOAD_AREA);
        w.setMode(StructureMode.SAVE);
        w.setName("modified");
        w.setOffset(new BlockPosition(0, 0, 0));
        w.setSize(new BlockPosition(3, 3, 3));
        w.setMirror(Mirror.NONE);
        w.setRotation(Rotation.NONE);
        w.setData("updated");
        w.setIgnoreEntities(false);
        w.setStrict(false);
        w.setShowAir(true);
        w.setShowBoundingBox(false);
        w.setIntegrity(1.0f);
        w.setSeed(0L);

        assertEquals(new BlockPosition(0, 0, 0), w.getPos());
        assertEquals(UpdateType.LOAD_AREA, w.getUpdateType());
        assertEquals(StructureMode.SAVE, w.getMode());
        assertEquals("modified", w.getName());
        assertEquals(new BlockPosition(0, 0, 0), w.getOffset());
        assertEquals(new BlockPosition(3, 3, 3), w.getSize());
        assertEquals(Mirror.NONE, w.getMirror());
        assertEquals(Rotation.NONE, w.getRotation());
        assertEquals("updated", w.getData());
        assertFalse(w.isIgnoreEntities());
        assertFalse(w.isStrict());
        assertTrue(w.isShowAir());
        assertFalse(w.isShowBoundingBox());
        assertEquals(1.0f, w.getIntegrity(), 1e-4f);
        assertEquals(0L, w.getSeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetStructureBlockPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
