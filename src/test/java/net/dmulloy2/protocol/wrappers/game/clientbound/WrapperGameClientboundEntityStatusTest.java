package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundEntityStatusTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundEntityStatus w = new WrapperGameClientboundEntityStatus();
        w.setEntityId(15);
        w.setStatus((byte) 2); // generic hurt animation

        assertEquals(15,     w.getEntityId());
        assertEquals((byte) 2, w.getStatus());
        assertEquals(PacketType.Play.Server.ENTITY_STATUS, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_STATUS);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 42);
        raw.getBytes().write(0, (byte) 3);

        WrapperGameClientboundEntityStatus w = new WrapperGameClientboundEntityStatus(raw);
        assertEquals(42,     w.getEntityId());
        assertEquals((byte) 3, w.getStatus());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundEntityStatus w = new WrapperGameClientboundEntityStatus();
        w.setEntityId(1);
        w.setStatus((byte) 0);

        new WrapperGameClientboundEntityStatus(w.getHandle()).setStatus((byte) 7);

        assertEquals((byte) 7, w.getStatus());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundEntityStatus(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
