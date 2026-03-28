package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundNbtQueryTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundNbtQuery w = new WrapperGameClientboundNbtQuery();
        w.setTransactionId(42);
        assertEquals(42, w.getTransactionId());
        assertEquals(PacketType.Play.Server.NBT_QUERY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.NBT_QUERY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);

        WrapperGameClientboundNbtQuery w = new WrapperGameClientboundNbtQuery(raw);
        assertEquals(99, w.getTransactionId());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundNbtQuery w = new WrapperGameClientboundNbtQuery();
        w.setTransactionId(1);

        new WrapperGameClientboundNbtQuery(w.getHandle()).setTransactionId(7);

        assertEquals(7, w.getTransactionId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundNbtQuery(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
