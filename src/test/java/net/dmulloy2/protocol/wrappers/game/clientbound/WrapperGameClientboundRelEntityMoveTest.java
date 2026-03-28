package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundRelEntityMoveTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundRelEntityMove w = new WrapperGameClientboundRelEntityMove();
        w.setEntityId(12);
        w.setDx((short) 128);
        w.setDy((short) 0);
        w.setDz((short) -256);
        w.setOnGround(true);
        assertEquals(12, w.getEntityId());
        assertEquals((short) 128, w.getDx());
        assertEquals((short) 0, w.getDy());
        assertEquals((short) -256, w.getDz());
        assertTrue(w.isOnGround());
        assertEquals(PacketType.Play.Server.REL_ENTITY_MOVE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);
        raw.getShorts().write(0, (short) 100);
        raw.getShorts().write(1, (short) 50);
        raw.getShorts().write(2, (short) -100);
        raw.getBooleans().write(0, false);

        WrapperGameClientboundRelEntityMove w = new WrapperGameClientboundRelEntityMove(raw);
        assertEquals(7, w.getEntityId());
        assertEquals((short) 100, w.getDx());
        assertEquals((short) 50, w.getDy());
        assertEquals((short) -100, w.getDz());
        assertFalse(w.isOnGround());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundRelEntityMove w = new WrapperGameClientboundRelEntityMove();
        w.setDx((short) 0);

        new WrapperGameClientboundRelEntityMove(w.getHandle()).setDx((short) 4096);

        assertEquals((short) 4096, w.getDx());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundRelEntityMove(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
