package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSetStructureBlockPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundSetStructureBlockPacket w = new WrappedServerboundSetStructureBlockPacket("hello", "world", true, true, false, true, 0.75f, 42L, new BlockPosition(7, 8, 9), new BlockPosition(1, 2, 3));

        assertEquals(PacketType.Play.Client.STRUCT, w.getHandle().getType());

        assertEquals("hello", w.getName());
        assertEquals("world", w.getData());
        assertTrue(w.isIgnoreEntities());
        assertTrue(w.isStrict());
        assertFalse(w.isShowAir());
        assertTrue(w.isShowBoundingBox());
        assertEquals(0.75f, w.getIntegrity(), 1e-4f);
        assertEquals(42L, w.getSeed());
        assertEquals(new BlockPosition(7, 8, 9), w.getPos());
        assertEquals(new BlockPosition(1, 2, 3), w.getOffset());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSetStructureBlockPacket w = new WrappedServerboundSetStructureBlockPacket();

        assertEquals(PacketType.Play.Client.STRUCT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundSetStructureBlockPacket source = new WrappedServerboundSetStructureBlockPacket("hello", "world", true, true, false, true, 0.75f, 42L, new BlockPosition(7, 8, 9), new BlockPosition(1, 2, 3));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSetStructureBlockPacket wrapper = new WrappedServerboundSetStructureBlockPacket(container);

        assertEquals("hello", wrapper.getName());
        assertEquals("world", wrapper.getData());
        assertTrue(wrapper.isIgnoreEntities());
        assertTrue(wrapper.isStrict());
        assertFalse(wrapper.isShowAir());
        assertTrue(wrapper.isShowBoundingBox());
        assertEquals(0.75f, wrapper.getIntegrity(), 1e-4f);
        assertEquals(42L, wrapper.getSeed());
        assertEquals(new BlockPosition(7, 8, 9), wrapper.getPos());
        assertEquals(new BlockPosition(1, 2, 3), wrapper.getOffset());

        wrapper.setName("modified");
        wrapper.setData("hello");
        wrapper.setIgnoreEntities(false);
        wrapper.setStrict(false);
        wrapper.setShowAir(true);
        wrapper.setShowBoundingBox(false);
        wrapper.setIntegrity(1.0f);
        wrapper.setSeed(987654321L);
        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setOffset(new BlockPosition(10, 20, 30));

        assertEquals("modified", wrapper.getName());
        assertEquals("hello", wrapper.getData());
        assertFalse(wrapper.isIgnoreEntities());
        assertFalse(wrapper.isStrict());
        assertTrue(wrapper.isShowAir());
        assertFalse(wrapper.isShowBoundingBox());
        assertEquals(1.0f, wrapper.getIntegrity(), 1e-4f);
        assertEquals(987654321L, wrapper.getSeed());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(new BlockPosition(10, 20, 30), wrapper.getOffset());

        assertEquals("modified", source.getName());
        assertEquals("hello", source.getData());
        assertFalse(source.isIgnoreEntities());
        assertFalse(source.isStrict());
        assertTrue(source.isShowAir());
        assertFalse(source.isShowBoundingBox());
        assertEquals(1.0f, source.getIntegrity(), 1e-4f);
        assertEquals(987654321L, source.getSeed());
        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(new BlockPosition(10, 20, 30), source.getOffset());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSetStructureBlockPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
