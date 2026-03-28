package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundBlockDestructionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundBlockDestructionPacket w = new WrappedClientboundBlockDestructionPacket();
        w.setId(1);
        w.setPos(new BlockPosition(10, 64, -20));
        w.setDestroyStage(5);

        assertEquals(PacketType.Play.Server.BLOCK_BREAK_ANIMATION, w.getHandle().getType());

        ClientboundBlockDestructionPacket p = (ClientboundBlockDestructionPacket) w.getHandle().getHandle();

        assertEquals(1, p.getId());
        assertEquals(5, p.getProgress());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundBlockDestructionPacket nmsPacket = new ClientboundBlockDestructionPacket(
                7, new net.minecraft.core.BlockPos(0, 100, 0), 9
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockDestructionPacket wrapper = new WrappedClientboundBlockDestructionPacket(container);

        assertEquals(7, wrapper.getId());
        assertEquals(new BlockPosition(0, 100, 0), wrapper.getPos());
        assertEquals(9, wrapper.getDestroyStage());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundBlockDestructionPacket nmsPacket = new ClientboundBlockDestructionPacket(
                7, new net.minecraft.core.BlockPos(0, 100, 0), 9
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundBlockDestructionPacket wrapper = new WrappedClientboundBlockDestructionPacket(container);

        wrapper.setDestroyStage(3);

        assertEquals(7, wrapper.getId());
        assertEquals(new BlockPosition(0, 100, 0), wrapper.getPos());
        assertEquals(3, wrapper.getDestroyStage());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundBlockDestructionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
