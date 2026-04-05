package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPingRequestPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPingRequestPacket w = new WrappedServerboundPingRequestPacket(123456789L);

        assertEquals(PacketType.Play.Client.PING_REQUEST, w.getHandle().getType());

        ServerboundPingRequestPacket p = (ServerboundPingRequestPacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.getTime());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPingRequestPacket w = new WrappedServerboundPingRequestPacket();

        assertEquals(PacketType.Play.Client.PING_REQUEST, w.getHandle().getType());

        ServerboundPingRequestPacket p = (ServerboundPingRequestPacket) w.getHandle().getHandle();

        assertEquals(0L, p.getTime());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundPingRequestPacket nmsPacket = new ServerboundPingRequestPacket(123456789L);
        PacketContainer container = new PacketContainer(WrappedServerboundPingRequestPacket.TYPE, nmsPacket);
        WrappedServerboundPingRequestPacket wrapper = new WrappedServerboundPingRequestPacket(container);

        assertEquals(123456789L, wrapper.getTime());

        wrapper.setTime(987654321L);

        assertEquals(987654321L, nmsPacket.getTime());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPingRequestPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
