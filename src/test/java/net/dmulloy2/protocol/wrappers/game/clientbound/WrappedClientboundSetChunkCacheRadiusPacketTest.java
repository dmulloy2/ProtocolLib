package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetChunkCacheRadiusPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetChunkCacheRadiusPacket w = new WrappedClientboundSetChunkCacheRadiusPacket();
        w.setViewDistance(10);

        assertEquals(PacketType.Play.Server.VIEW_DISTANCE, w.getHandle().getType());

        ClientboundSetChunkCacheRadiusPacket p = (ClientboundSetChunkCacheRadiusPacket) w.getHandle().getHandle();

        assertEquals(10, p.getRadius());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetChunkCacheRadiusPacket nmsPacket = new ClientboundSetChunkCacheRadiusPacket(12);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheRadiusPacket wrapper = new WrappedClientboundSetChunkCacheRadiusPacket(container);

        assertEquals(12, wrapper.getViewDistance());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetChunkCacheRadiusPacket nmsPacket = new ClientboundSetChunkCacheRadiusPacket(8);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheRadiusPacket wrapper = new WrappedClientboundSetChunkCacheRadiusPacket(container);

        wrapper.setViewDistance(16);

        assertEquals(16, wrapper.getViewDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetChunkCacheRadiusPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
