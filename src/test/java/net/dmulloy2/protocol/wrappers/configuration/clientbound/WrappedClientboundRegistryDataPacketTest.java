package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRegistryDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        // TODO: packet has no suitable all-args constructor
        assertEquals(PacketType.Configuration.Server.REGISTRY_DATA, new WrappedClientboundRegistryDataPacket().getHandle().getType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRegistryDataPacket w = new WrappedClientboundRegistryDataPacket();

        assertEquals(PacketType.Configuration.Server.REGISTRY_DATA, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Configuration.Server.REGISTRY_DATA);
        WrappedClientboundRegistryDataPacket wrapper = new WrappedClientboundRegistryDataPacket(container);

        assertEquals(PacketType.Configuration.Server.REGISTRY_DATA, wrapper.getHandle().getType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRegistryDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
