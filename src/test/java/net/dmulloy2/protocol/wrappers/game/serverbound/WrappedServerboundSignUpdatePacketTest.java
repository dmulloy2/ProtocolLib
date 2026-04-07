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
        WrappedServerboundSignUpdatePacket w = new WrappedServerboundSignUpdatePacket(new BlockPosition(1, 2, 3), false, "line1", "line2", "line3", "line4");

        assertEquals(PacketType.Play.Client.UPDATE_SIGN, w.getHandle().getType());

        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertFalse(w.isFrontText());
        assertArrayEquals(new String[] { "line1", "line2", "line3", "line4" }, w.getLines());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSignUpdatePacket w = new WrappedServerboundSignUpdatePacket();

        assertEquals(PacketType.Play.Client.UPDATE_SIGN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSignUpdatePacket nmsPacket = new ServerboundSignUpdatePacket(new BlockPos(1, 2, 3), false, "line1", "line2", "line3", "line4");
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSignUpdatePacket wrapper = new WrappedServerboundSignUpdatePacket(container);

        assertEquals(new BlockPosition(1, 2, 3), wrapper.getPos());
        assertFalse(wrapper.isFrontText());
        assertArrayEquals(new String[] { "line1", "line2", "line3", "line4" }, wrapper.getLines());

        wrapper.setPos(new BlockPosition(10, 20, 30));
        wrapper.setFrontText(true);
        wrapper.setLines(new String[] { "x", "y", "z", "w" });

        assertEquals(new BlockPosition(10, 20, 30), wrapper.getPos());
        assertTrue(wrapper.isFrontText());
        assertArrayEquals(new String[] { "x", "y", "z", "w" }, wrapper.getLines());

        assertEquals(10, nmsPacket.getPos().getX());
        assertEquals(20, nmsPacket.getPos().getY());
        assertEquals(30, nmsPacket.getPos().getZ());
        assertTrue(nmsPacket.isFrontText());
        assertArrayEquals(new String[] { "x", "y", "z", "w" }, nmsPacket.getLines());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSignUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
