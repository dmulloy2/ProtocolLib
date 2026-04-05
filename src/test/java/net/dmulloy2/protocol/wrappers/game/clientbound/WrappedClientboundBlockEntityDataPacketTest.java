package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedRegistrable;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockEntityDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundBlockEntityDataPacket w = new WrappedClientboundBlockEntityDataPacket(new BlockPosition(1, 2, 3), null, null);

        assertEquals(PacketType.Play.Server.TILE_ENTITY_DATA, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(null, w.getBlockEntityType());
        assertEquals(null, w.getTag());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBlockEntityDataPacket w = new WrappedClientboundBlockEntityDataPacket();

        assertEquals(PacketType.Play.Server.TILE_ENTITY_DATA, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundBlockEntityDataPacket source = new WrappedClientboundBlockEntityDataPacket(new BlockPosition(1, 2, 3), null, null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockEntityDataPacket wrapper = new WrappedClientboundBlockEntityDataPacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(null, wrapper.getBlockEntityType());
        assertEquals(null, wrapper.getTag());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setBlockEntityType(null);
        wrapper.setTag(null);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(null, wrapper.getBlockEntityType());
        assertEquals(null, wrapper.getTag());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(null, source.getBlockEntityType());
        assertEquals(null, source.getTag());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockEntityDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
