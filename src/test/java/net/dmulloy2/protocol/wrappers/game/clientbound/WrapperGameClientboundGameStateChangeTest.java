package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundGameStateChangeTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundGameStateChange w = new WrapperGameClientboundGameStateChange();
        w.setEvent(WrapperGameClientboundGameStateChange.BEGIN_RAINING);
        w.setValue(0.0f);

        assertEquals(WrapperGameClientboundGameStateChange.BEGIN_RAINING, w.getEvent());
        assertEquals(0.0f, w.getValue(), 1e-4f);
        assertEquals(PacketType.Play.Server.GAME_STATE_CHANGE, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.GAME_STATE_CHANGE);
        raw.getModifier().writeDefaults();
        raw.getGameStateIDs().write(0, WrapperGameClientboundGameStateChange.CHANGE_GAME_MODE);
        raw.getFloat().write(0, 1.0f);

        WrapperGameClientboundGameStateChange w = new WrapperGameClientboundGameStateChange(raw);
        assertEquals(WrapperGameClientboundGameStateChange.CHANGE_GAME_MODE, w.getEvent());
        assertEquals(1.0f, w.getValue(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundGameStateChange w = new WrapperGameClientboundGameStateChange();
        w.setValue(0.0f);

        new WrapperGameClientboundGameStateChange(w.getHandle()).setValue(0.5f);

        assertEquals(0.5f, w.getValue(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundGameStateChange(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
