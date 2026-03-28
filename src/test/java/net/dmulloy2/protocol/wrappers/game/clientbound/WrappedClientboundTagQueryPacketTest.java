package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTagQueryPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket();
        w.setTransactionId(42);
        assertEquals(42, w.getTransactionId());
        assertEquals(PacketType.Play.Server.NBT_QUERY, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.NBT_QUERY);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 99);

        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket(raw);
        assertEquals(99, w.getTransactionId());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundTagQueryPacket w = new WrappedClientboundTagQueryPacket();
        w.setTransactionId(1);

        new WrappedClientboundTagQueryPacket(w.getHandle()).setTransactionId(7);

        assertEquals(7, w.getTransactionId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTagQueryPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
