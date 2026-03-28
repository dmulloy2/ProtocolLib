package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundWorldEventTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundWorldEvent w = new WrapperGameClientboundWorldEvent();
        w.setType(1004);
        w.setPos(new BlockPosition(5, 64, -3));
        w.setData(0);
        w.setBroadcastToAll(false);
        assertEquals(1004, w.getType());
        assertEquals(new BlockPosition(5, 64, -3), w.getPos());
        assertEquals(0, w.getData());
        assertFalse(w.isBroadcastToAll());
        assertEquals(PacketType.Play.Server.WORLD_EVENT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.WORLD_EVENT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 2001);
        raw.getBlockPositionModifier().write(0, new BlockPosition(1, 2, 3));
        raw.getIntegers().write(1, 10);
        raw.getBooleans().write(0, true);

        WrapperGameClientboundWorldEvent w = new WrapperGameClientboundWorldEvent(raw);
        assertEquals(2001, w.getType());
        assertEquals(new BlockPosition(1, 2, 3), w.getPos());
        assertEquals(10, w.getData());
        assertTrue(w.isBroadcastToAll());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundWorldEvent w = new WrapperGameClientboundWorldEvent();
        w.setData(0);

        new WrapperGameClientboundWorldEvent(w.getHandle()).setData(42);

        assertEquals(42, w.getData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundWorldEvent(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
