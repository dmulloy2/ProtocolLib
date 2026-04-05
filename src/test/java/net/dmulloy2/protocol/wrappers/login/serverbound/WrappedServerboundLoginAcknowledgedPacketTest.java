package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundLoginAcknowledgedPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Login.Client.LOGIN_ACK, new WrappedServerboundLoginAcknowledgedPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundLoginAcknowledgedPacket w = new WrappedServerboundLoginAcknowledgedPacket();

        assertEquals(PacketType.Login.Client.LOGIN_ACK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Login.Client.LOGIN_ACK);
        WrappedServerboundLoginAcknowledgedPacket wrapper = new WrappedServerboundLoginAcknowledgedPacket(container);

        assertEquals(PacketType.Login.Client.LOGIN_ACK, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundLoginAcknowledgedPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
