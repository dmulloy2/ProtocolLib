package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundKickDisconnectTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundKickDisconnect w = new WrapperGameClientboundKickDisconnect();
        w.setReason(WrappedChatComponent.fromText("You were kicked"));
        assertTrue(w.getReason().getJson().contains("You were kicked"));
        assertEquals(PacketType.Play.Server.KICK_DISCONNECT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.KICK_DISCONNECT);
        raw.getModifier().writeDefaults();
        raw.getChatComponents().write(0, WrappedChatComponent.fromText("Banned"));

        WrapperGameClientboundKickDisconnect w = new WrapperGameClientboundKickDisconnect(raw);
        assertTrue(w.getReason().getJson().contains("Banned"));
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundKickDisconnect w = new WrapperGameClientboundKickDisconnect();
        w.setReason(WrappedChatComponent.fromText("old"));

        new WrapperGameClientboundKickDisconnect(w.getHandle()).setReason(WrappedChatComponent.fromText("new reason"));

        assertTrue(w.getReason().getJson().contains("new reason"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundKickDisconnect(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
