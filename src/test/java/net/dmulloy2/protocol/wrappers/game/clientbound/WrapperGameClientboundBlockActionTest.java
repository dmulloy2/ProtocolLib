package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundBlockActionTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundBlockAction w = new WrapperGameClientboundBlockAction();
        BlockPosition pos = new BlockPosition(0, 64, 0);
        w.setPos(pos);
        w.setActionId(1);
        w.setActionParam(0);
        w.setBlockType(Material.CHEST);
        assertEquals(pos, w.getPos());
        assertEquals(1, w.getActionId());
        assertEquals(0, w.getActionParam());
        assertEquals(Material.CHEST, w.getBlockType());
        assertEquals(PacketType.Play.Server.BLOCK_ACTION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        raw.getModifier().writeDefaults();
        raw.getBlockPositionModifier().write(0, new BlockPosition(5, 10, 5));
        raw.getIntegers().write(0, 2);
        raw.getIntegers().write(1, 3);
        raw.getBlocks().write(0, Material.NOTE_BLOCK);

        WrapperGameClientboundBlockAction w = new WrapperGameClientboundBlockAction(raw);
        assertEquals(new BlockPosition(5, 10, 5), w.getPos());
        assertEquals(2, w.getActionId());
        assertEquals(3, w.getActionParam());
        assertEquals(Material.NOTE_BLOCK, w.getBlockType());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundBlockAction w = new WrapperGameClientboundBlockAction();
        w.setActionId(0);

        new WrapperGameClientboundBlockAction(w.getHandle()).setActionId(5);

        assertEquals(5, w.getActionId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundBlockAction(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
