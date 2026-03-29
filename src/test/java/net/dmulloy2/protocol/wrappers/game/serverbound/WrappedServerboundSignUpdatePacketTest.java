package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSignUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        BlockPosition pos = new BlockPosition(3, 64, -7);
        String[] lines = {"Hello", "World", "", ""};
        WrappedServerboundSignUpdatePacket w = new WrappedServerboundSignUpdatePacket(pos, true, lines);

        assertEquals(PacketType.Play.Client.UPDATE_SIGN, w.getHandle().getType());

        ServerboundSignUpdatePacket p = (ServerboundSignUpdatePacket) w.getHandle().getHandle();

        assertEquals(new BlockPos(3, 64, -7), p.getPos());
        assertTrue(p.isFrontText());
        assertArrayEquals(lines, p.getLines());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSignUpdatePacket w = new WrappedServerboundSignUpdatePacket();

        assertEquals(PacketType.Play.Client.UPDATE_SIGN, w.getHandle().getType());

        assertNotNull(w.getPos());
        assertFalse(w.isFrontText());
        assertNotNull(w.getLines());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSignUpdatePacket nmsPacket = new ServerboundSignUpdatePacket(
                new BlockPos(1, 2, 3), true, "A", "B", "C", "D");

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSignUpdatePacket wrapper = new WrappedServerboundSignUpdatePacket(container);

        assertEquals(new BlockPos(1, 2, 3), new BlockPos(wrapper.getPos().getX(), wrapper.getPos().getY(), wrapper.getPos().getZ()));
        assertTrue(wrapper.isFrontText());
        assertArrayEquals(new String[]{"A", "B", "C", "D"}, wrapper.getLines());

        wrapper.setPos(new BlockPosition(5, 10, 15));
        wrapper.setFrontText(false);
        wrapper.setLines(new String[]{"W", "X", "Y", "Z"});

        assertEquals(new BlockPos(5, 10, 15), nmsPacket.getPos());
        assertFalse(nmsPacket.isFrontText());
        assertArrayEquals(new String[]{"W", "X", "Y", "Z"}, nmsPacket.getLines());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSignUpdatePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
