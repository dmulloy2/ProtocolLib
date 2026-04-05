package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPongResponsePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPongResponsePacket w = new WrappedClientboundPongResponsePacket(123456789L);

        assertEquals(PacketType.Play.Server.PONG_RESPONSE, w.getHandle().getType());

        ClientboundPongResponsePacket p = (ClientboundPongResponsePacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.time());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPongResponsePacket w = new WrappedClientboundPongResponsePacket();

        assertEquals(PacketType.Play.Server.PONG_RESPONSE, w.getHandle().getType());

        ClientboundPongResponsePacket p = (ClientboundPongResponsePacket) w.getHandle().getHandle();

        assertEquals(0L, p.time());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPongResponsePacket nmsPacket = new ClientboundPongResponsePacket(123456789L);
        PacketContainer container = new PacketContainer(WrappedClientboundPongResponsePacket.TYPE, nmsPacket);
        WrappedClientboundPongResponsePacket wrapper = new WrappedClientboundPongResponsePacket(container);

        assertEquals(123456789L, wrapper.getTime());

        wrapper.setTime(987654321L);

        assertEquals(987654321L, nmsPacket.time());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPongResponsePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
