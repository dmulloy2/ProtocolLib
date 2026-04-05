package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundKeepAlivePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundKeepAlivePacket w = new WrappedServerboundKeepAlivePacket(123456789L);

        assertEquals(PacketType.Configuration.Client.KEEP_ALIVE, w.getHandle().getType());

        ServerboundKeepAlivePacket p = (ServerboundKeepAlivePacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundKeepAlivePacket w = new WrappedServerboundKeepAlivePacket();

        assertEquals(PacketType.Configuration.Client.KEEP_ALIVE, w.getHandle().getType());

        ServerboundKeepAlivePacket p = (ServerboundKeepAlivePacket) w.getHandle().getHandle();

        assertEquals(0L, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundKeepAlivePacket nmsPacket = new ServerboundKeepAlivePacket(123456789L);
        PacketContainer container = new PacketContainer(WrappedServerboundKeepAlivePacket.TYPE, nmsPacket);
        WrappedServerboundKeepAlivePacket wrapper = new WrappedServerboundKeepAlivePacket(container);

        assertEquals(123456789L, wrapper.getId());

        wrapper.setId(987654321L);

        assertEquals(987654321L, nmsPacket.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundKeepAlivePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
