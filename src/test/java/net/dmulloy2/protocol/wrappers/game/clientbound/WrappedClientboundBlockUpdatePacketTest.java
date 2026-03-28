package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
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
    void testCreate() {
        WrappedClientboundBlockUpdatePacket w = new WrappedClientboundBlockUpdatePacket();
        w.setPos(new BlockPosition(10, 64, -5));
        w.setBlockData(WrappedBlockData.createData(Material.STONE));

        assertEquals(PacketType.Play.Server.BLOCK_CHANGE, w.getHandle().getType());

        ClientboundBlockUpdatePacket p = (ClientboundBlockUpdatePacket) w.getHandle().getHandle();

        assertNotNull(p.getBlockState());
        assertNotNull(p.getPos());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        container.getModifier().writeDefaults();
        container.getBlockPositionModifier().write(0, new BlockPosition(1, 2, 3));
        container.getBlockData().write(0, WrappedBlockData.createData(Material.DIRT));

        WrappedClientboundBlockUpdatePacket wrapper = new WrappedClientboundBlockUpdatePacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertNotNull(wrapper.getBlockData());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        container.getModifier().writeDefaults();
        container.getBlockPositionModifier().write(0, new BlockPosition(1, 2, 3));
        container.getBlockData().write(0, WrappedBlockData.createData(Material.DIRT));

        WrappedClientboundBlockUpdatePacket wrapper = new WrappedClientboundBlockUpdatePacket(container);
        wrapper.setPos(new BlockPosition(5, 5, 5));

        assertEquals(new BlockPosition(5, 5, 5), wrapper.getPos());
        assertNotNull(wrapper.getBlockData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
