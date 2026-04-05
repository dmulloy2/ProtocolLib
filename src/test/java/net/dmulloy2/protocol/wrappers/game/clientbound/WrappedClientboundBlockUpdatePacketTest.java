package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundBlockUpdatePacket w = new WrappedClientboundBlockUpdatePacket(new BlockPosition(1, 2, 3), WrappedBlockData.createData(Material.DIRT));

        assertEquals(PacketType.Play.Server.BLOCK_CHANGE, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(WrappedBlockData.createData(Material.DIRT), w.getBlockData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBlockUpdatePacket w = new WrappedClientboundBlockUpdatePacket();

        assertEquals(PacketType.Play.Server.BLOCK_CHANGE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundBlockUpdatePacket source = new WrappedClientboundBlockUpdatePacket(new BlockPosition(1, 2, 3), WrappedBlockData.createData(Material.DIRT));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockUpdatePacket wrapper = new WrappedClientboundBlockUpdatePacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(WrappedBlockData.createData(Material.DIRT), wrapper.getBlockData());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setBlockData(WrappedBlockData.createData(Material.OAK_PLANKS));

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(WrappedBlockData.createData(Material.OAK_PLANKS), wrapper.getBlockData());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(WrappedBlockData.createData(Material.OAK_PLANKS), source.getBlockData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
