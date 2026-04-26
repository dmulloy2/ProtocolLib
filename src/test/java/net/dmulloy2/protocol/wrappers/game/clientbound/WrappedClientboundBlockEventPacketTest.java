package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Material;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockEventPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundBlockEventPacket w = new WrappedClientboundBlockEventPacket(new BlockPosition(1, 2, 3), 7, 5, Material.STONE);

        assertEquals(PacketType.Play.Server.BLOCK_ACTION, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(7, w.getActionId());
        assertEquals(5, w.getActionParam());
        assertEquals(Material.STONE, w.getBlockType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundBlockEventPacket w = new WrappedClientboundBlockEventPacket();

        assertEquals(PacketType.Play.Server.BLOCK_ACTION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundBlockEventPacket source = new WrappedClientboundBlockEventPacket(new BlockPosition(1, 2, 3), 7, 5, Material.STONE);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockEventPacket wrapper = new WrappedClientboundBlockEventPacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertEquals(7, wrapper.getActionId());
        assertEquals(5, wrapper.getActionParam());
        assertEquals(Material.STONE, wrapper.getBlockType());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setActionId(-5);
        wrapper.setActionParam(0);
        wrapper.setBlockType(Material.DIRT);

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertEquals(-5, wrapper.getActionId());
        assertEquals(0, wrapper.getActionParam());
        assertEquals(Material.DIRT, wrapper.getBlockType());

        assertEquals(new BlockPosition(10, 20, 30), source.getPos());
        assertEquals(-5, source.getActionId());
        assertEquals(0, source.getActionParam());
        assertEquals(Material.DIRT, source.getBlockType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockEventPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
