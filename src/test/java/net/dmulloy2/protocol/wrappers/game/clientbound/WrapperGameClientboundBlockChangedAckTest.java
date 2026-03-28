package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundBlockChangedAckTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundBlockChangedAck w = new WrapperGameClientboundBlockChangedAck();
        w.setSequence(17);
        assertEquals(17, w.getSequence());
        assertEquals(PacketType.Play.Server.BLOCK_CHANGED_ACK, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGED_ACK);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 33);

        WrapperGameClientboundBlockChangedAck w = new WrapperGameClientboundBlockChangedAck(raw);
        assertEquals(33, w.getSequence());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundBlockChangedAck w = new WrapperGameClientboundBlockChangedAck();
        w.setSequence(0);

        new WrapperGameClientboundBlockChangedAck(w.getHandle()).setSequence(99);

        assertEquals(99, w.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundBlockChangedAck(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
