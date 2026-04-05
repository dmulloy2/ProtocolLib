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
    void testAllArgsCreate() {
        WrappedClientboundSetChunkCacheCenterPacket w = new WrappedClientboundSetChunkCacheCenterPacket(3, 7);

        assertEquals(PacketType.Play.Server.VIEW_CENTRE, w.getHandle().getType());

        ClientboundSetChunkCacheCenterPacket p = (ClientboundSetChunkCacheCenterPacket) w.getHandle().getHandle();

        assertEquals(3, p.getX());
        assertEquals(7, p.getZ());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetChunkCacheCenterPacket w = new WrappedClientboundSetChunkCacheCenterPacket();

        assertEquals(PacketType.Play.Server.VIEW_CENTRE, w.getHandle().getType());

        ClientboundSetChunkCacheCenterPacket p = (ClientboundSetChunkCacheCenterPacket) w.getHandle().getHandle();

        assertEquals(0, p.getX());
        assertEquals(0, p.getZ());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetChunkCacheCenterPacket nmsPacket = new ClientboundSetChunkCacheCenterPacket(3, 7);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheCenterPacket wrapper = new WrappedClientboundSetChunkCacheCenterPacket(container);

        assertEquals(3, wrapper.getChunkX());
        assertEquals(7, wrapper.getChunkZ());

        wrapper.setChunkX(9);
        wrapper.setChunkZ(-5);

        assertEquals(9, nmsPacket.getX());
        assertEquals(-5, nmsPacket.getZ());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetChunkCacheCenterPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
