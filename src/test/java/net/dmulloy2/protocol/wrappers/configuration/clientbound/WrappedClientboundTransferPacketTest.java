package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTransferPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTransferPacket w = new WrappedClientboundTransferPacket("hello", 7);

        assertEquals(PacketType.Configuration.Server.TRANSFER, w.getHandle().getType());

        ClientboundTransferPacket p = (ClientboundTransferPacket) w.getHandle().getHandle();

        assertEquals("hello", p.host());
        assertEquals(7, p.port());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTransferPacket w = new WrappedClientboundTransferPacket();

        assertEquals(PacketType.Configuration.Server.TRANSFER, w.getHandle().getType());

        ClientboundTransferPacket p = (ClientboundTransferPacket) w.getHandle().getHandle();

        assertEquals(0, p.port());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTransferPacket nmsPacket = new ClientboundTransferPacket("hello", 7);
        PacketContainer container = new PacketContainer(WrappedClientboundTransferPacket.TYPE, nmsPacket);
        WrappedClientboundTransferPacket wrapper = new WrappedClientboundTransferPacket(container);

        assertEquals("hello", wrapper.getHost());
        assertEquals(7, wrapper.getPort());

        wrapper.setHost("modified");
        wrapper.setPort(-5);

        assertEquals("modified", nmsPacket.host());
        assertEquals(-5, nmsPacket.port());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTransferPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
