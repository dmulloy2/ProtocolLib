package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundClientInformationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Client.CLIENT_INFORMATION, new WrappedServerboundClientInformationPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundClientInformationPacket w = new WrappedServerboundClientInformationPacket();

        assertEquals(PacketType.Configuration.Client.CLIENT_INFORMATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Client.CLIENT_INFORMATION);
        WrappedServerboundClientInformationPacket wrapper = new WrappedServerboundClientInformationPacket(container);

        assertEquals(PacketType.Configuration.Client.CLIENT_INFORMATION, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientInformationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
