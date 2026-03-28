package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetChunkCacheCenterPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetChunkCacheCenterPacket w = new WrappedClientboundSetChunkCacheCenterPacket();
        w.setChunkX(10);
        w.setChunkZ(-5);

        assertEquals(PacketType.Play.Server.VIEW_CENTRE, w.getHandle().getType());

        ClientboundSetChunkCacheCenterPacket p = (ClientboundSetChunkCacheCenterPacket) w.getHandle().getHandle();

        assertEquals(10, p.getX());
        assertEquals(-5, p.getZ());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetChunkCacheCenterPacket nmsPacket = new ClientboundSetChunkCacheCenterPacket(3, 7);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheCenterPacket wrapper = new WrappedClientboundSetChunkCacheCenterPacket(container);

        assertEquals(3, wrapper.getChunkX());
        assertEquals(7, wrapper.getChunkZ());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetChunkCacheCenterPacket nmsPacket = new ClientboundSetChunkCacheCenterPacket(0, 0);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheCenterPacket wrapper = new WrappedClientboundSetChunkCacheCenterPacket(container);

        wrapper.setChunkZ(15);

        assertEquals(0, wrapper.getChunkX());
        assertEquals(15, wrapper.getChunkZ());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetChunkCacheCenterPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
