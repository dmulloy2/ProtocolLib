package net.dmulloy2.protocol.wrappers.status.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundStatusRequestPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Status.Client.START, new WrappedServerboundStatusRequestPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundStatusRequestPacket w = new WrappedServerboundStatusRequestPacket();

        assertEquals(PacketType.Status.Client.START, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Status.Client.START);
        WrappedServerboundStatusRequestPacket wrapper = new WrappedServerboundStatusRequestPacket(container);

        assertEquals(PacketType.Status.Client.START, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundStatusRequestPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
