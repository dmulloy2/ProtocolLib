package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetTimePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetTimePacket w = new WrappedClientboundSetTimePacket();
        w.setWorldAge(100000L);

        ClientboundSetTimePacket nmsPacket = (ClientboundSetTimePacket) w.getHandle().getHandle();
        assertEquals(PacketType.Play.Server.UPDATE_TIME, w.getHandle().getType());
        assertEquals(100000L, nmsPacket.gameTime());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetTimePacket nmsPacket = new ClientboundSetTimePacket(999L, Map.of());
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTimePacket wrapper = new WrappedClientboundSetTimePacket(container);

        assertEquals(999L, wrapper.getWorldAge());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetTimePacket nmsPacket = new ClientboundSetTimePacket(100L, Map.of());
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTimePacket wrapper = new WrappedClientboundSetTimePacket(container);

        wrapper.setWorldAge(50000L);

        assertEquals(50000L, wrapper.getWorldAge());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTimePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
