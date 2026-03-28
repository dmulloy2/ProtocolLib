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

class WrapperGameClientboundBlockChangeTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundBlockChange w = new WrapperGameClientboundBlockChange();
        BlockPosition pos = new BlockPosition(10, 64, -5);
        WrappedBlockData blockData = WrappedBlockData.createData(Material.STONE);
        w.setPos(pos);
        w.setBlockData(blockData);
        assertEquals(pos, w.getPos());
        assertNotNull(w.getBlockData());
        assertEquals(PacketType.Play.Server.BLOCK_CHANGE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        raw.getModifier().writeDefaults();
        raw.getBlockPositionModifier().write(0, new BlockPosition(1, 2, 3));
        raw.getBlockData().write(0, WrappedBlockData.createData(Material.DIRT));

        WrapperGameClientboundBlockChange w = new WrapperGameClientboundBlockChange(raw);
        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertNotNull(w.getBlockData());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundBlockChange w = new WrapperGameClientboundBlockChange();
        w.setPos(new BlockPosition(0, 0, 0));

        new WrapperGameClientboundBlockChange(w.getHandle()).setPos(new BlockPosition(5, 5, 5));

        assertEquals(new BlockPosition(5, 5, 5), w.getPos());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundBlockChange(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
