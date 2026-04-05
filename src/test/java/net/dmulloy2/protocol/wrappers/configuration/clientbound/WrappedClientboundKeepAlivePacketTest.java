package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundKeepAlivePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundKeepAlivePacket w = new WrappedClientboundKeepAlivePacket(123456789L);

        assertEquals(PacketType.Configuration.Server.KEEP_ALIVE, w.getHandle().getType());

        ClientboundKeepAlivePacket p = (ClientboundKeepAlivePacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundKeepAlivePacket w = new WrappedClientboundKeepAlivePacket();

        assertEquals(PacketType.Configuration.Server.KEEP_ALIVE, w.getHandle().getType());

        ClientboundKeepAlivePacket p = (ClientboundKeepAlivePacket) w.getHandle().getHandle();

        assertEquals(0L, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundKeepAlivePacket nmsPacket = new ClientboundKeepAlivePacket(123456789L);
        PacketContainer container = new PacketContainer(WrappedClientboundKeepAlivePacket.TYPE, nmsPacket);
        WrappedClientboundKeepAlivePacket wrapper = new WrappedClientboundKeepAlivePacket(container);

        assertEquals(123456789L, wrapper.getId());

        wrapper.setId(987654321L);

        assertEquals(987654321L, nmsPacket.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundKeepAlivePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
