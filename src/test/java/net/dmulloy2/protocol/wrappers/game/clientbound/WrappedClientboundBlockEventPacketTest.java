package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
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
    void testCreate() {
        WrappedClientboundBlockEventPacket w = new WrappedClientboundBlockEventPacket();
        w.setPos(new BlockPosition(0, 64, 0));
        w.setActionId(1);
        w.setActionParam(0);
        w.setBlockType(Material.CHEST);

        assertEquals(PacketType.Play.Server.BLOCK_ACTION, w.getHandle().getType());

        ClientboundBlockEventPacket p = (ClientboundBlockEventPacket) w.getHandle().getHandle();

        assertEquals(1, p.getB0());
        assertEquals(0, p.getB1());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        container.getModifier().writeDefaults();
        container.getBlockPositionModifier().write(0, new BlockPosition(5, 10, 5));
        container.getIntegers().write(0, 2);
        container.getIntegers().write(1, 3);
        container.getBlocks().write(0, Material.NOTE_BLOCK);

        WrappedClientboundBlockEventPacket wrapper = new WrappedClientboundBlockEventPacket(container);

        assertEquals(new BlockPosition(5, 10, 5), wrapper.getPos());
        assertEquals(2, wrapper.getActionId());
        assertEquals(3, wrapper.getActionParam());
        assertEquals(Material.NOTE_BLOCK, wrapper.getBlockType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_ACTION);
        container.getModifier().writeDefaults();
        container.getBlockPositionModifier().write(0, new BlockPosition(5, 10, 5));
        container.getIntegers().write(0, 2);
        container.getIntegers().write(1, 3);
        container.getBlocks().write(0, Material.NOTE_BLOCK);

        WrappedClientboundBlockEventPacket wrapper = new WrappedClientboundBlockEventPacket(container);
        wrapper.setActionId(5);

        assertEquals(new BlockPosition(5, 10, 5), wrapper.getPos());
        assertEquals(5, wrapper.getActionId());
        assertEquals(3, wrapper.getActionParam());
        assertEquals(Material.NOTE_BLOCK, wrapper.getBlockType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockEventPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
