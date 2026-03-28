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
    void testCreate() {
        WrappedClientboundPongResponsePacket w = new WrappedClientboundPongResponsePacket();
        w.setTime(123456789L);

        assertEquals(PacketType.Play.Server.PONG_RESPONSE, w.getHandle().getType());

        ClientboundPongResponsePacket p = (ClientboundPongResponsePacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.time());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundPongResponsePacket nmsPacket = new ClientboundPongResponsePacket(987654321L);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPongResponsePacket wrapper = new WrappedClientboundPongResponsePacket(container);

        assertEquals(987654321L, wrapper.getTime());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundPongResponsePacket nmsPacket = new ClientboundPongResponsePacket(987654321L);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPongResponsePacket wrapper = new WrappedClientboundPongResponsePacket(container);

        wrapper.setTime(42L);

        assertEquals(42L, wrapper.getTime());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPongResponsePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
