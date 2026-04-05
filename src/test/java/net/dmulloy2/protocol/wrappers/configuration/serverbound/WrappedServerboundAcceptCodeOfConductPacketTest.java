package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundAcceptCodeOfConductPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Packet has no fields; no all-args constructor.
        assertEquals(PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT, new WrappedServerboundAcceptCodeOfConductPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundAcceptCodeOfConductPacket w = new WrappedServerboundAcceptCodeOfConductPacket();
        assertEquals(PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT);
        WrappedServerboundAcceptCodeOfConductPacket wrapper = new WrappedServerboundAcceptCodeOfConductPacket(container);
        assertEquals(PacketType.Configuration.Client.ACCEPT_CODE_OF_CONDUCT, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundAcceptCodeOfConductPacket(new PacketContainer(PacketType.Configuration.Client.KEEP_ALIVE)));
    }
}
