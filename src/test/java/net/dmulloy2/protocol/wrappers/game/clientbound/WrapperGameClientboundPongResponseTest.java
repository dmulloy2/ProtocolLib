package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPongResponseTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPongResponse w = new WrapperGameClientboundPongResponse();
        w.setTime(123456789L);
        assertEquals(123456789L, w.getTime());
        assertEquals(PacketType.Play.Server.PONG_RESPONSE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PONG_RESPONSE);
        raw.getModifier().writeDefaults();
        raw.getLongs().write(0, 987654321L);

        WrapperGameClientboundPongResponse w = new WrapperGameClientboundPongResponse(raw);
        assertEquals(987654321L, w.getTime());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPongResponse w = new WrapperGameClientboundPongResponse();
        w.setTime(1L);

        new WrapperGameClientboundPongResponse(w.getHandle()).setTime(42L);

        assertEquals(42L, w.getTime());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPongResponse(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
