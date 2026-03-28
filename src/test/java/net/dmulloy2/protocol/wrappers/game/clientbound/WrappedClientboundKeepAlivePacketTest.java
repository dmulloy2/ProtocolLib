package net.dmulloy2.protocol.wrappers.game.clientbound;

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
    void testCreate() {
        WrappedClientboundKeepAlivePacket w = new WrappedClientboundKeepAlivePacket();
        w.setId(123456789L);

        assertEquals(PacketType.Play.Server.KEEP_ALIVE, w.getHandle().getType());

        ClientboundKeepAlivePacket p = (ClientboundKeepAlivePacket) w.getHandle().getHandle();

        assertEquals(123456789L, p.getId());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundKeepAlivePacket nmsPacket = new ClientboundKeepAlivePacket(987654321L);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundKeepAlivePacket wrapper = new WrappedClientboundKeepAlivePacket(container);

        assertEquals(987654321L, wrapper.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundKeepAlivePacket nmsPacket = new ClientboundKeepAlivePacket(987654321L);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundKeepAlivePacket wrapper = new WrappedClientboundKeepAlivePacket(container);

        wrapper.setId(42L);

        assertEquals(42L, wrapper.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundKeepAlivePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
