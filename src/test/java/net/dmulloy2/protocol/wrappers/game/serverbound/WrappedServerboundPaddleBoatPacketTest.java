package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPaddleBoatPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPaddleBoatPacket w = new WrappedServerboundPaddleBoatPacket(true, false);

        assertEquals(PacketType.Play.Client.BOAT_MOVE, w.getHandle().getType());

        ServerboundPaddleBoatPacket p = (ServerboundPaddleBoatPacket) w.getHandle().getHandle();

        assertTrue(p.getLeft());
        assertFalse(p.getRight());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPaddleBoatPacket w = new WrappedServerboundPaddleBoatPacket();

        assertEquals(PacketType.Play.Client.BOAT_MOVE, w.getHandle().getType());

        ServerboundPaddleBoatPacket p = (ServerboundPaddleBoatPacket) w.getHandle().getHandle();

        assertFalse(p.getLeft());
        assertFalse(p.getRight());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundPaddleBoatPacket nmsPacket = new ServerboundPaddleBoatPacket(true, false);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPaddleBoatPacket wrapper = new WrappedServerboundPaddleBoatPacket(container);

        assertTrue(wrapper.isLeft());
        assertFalse(wrapper.isRight());

        wrapper.setLeft(false);
        wrapper.setRight(true);

        assertFalse(nmsPacket.getLeft());
        assertTrue(nmsPacket.getRight());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPaddleBoatPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
