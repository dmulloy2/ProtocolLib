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
    void testAllArgsCreate() {
        WrappedClientboundSetChunkCacheRadiusPacket w = new WrappedClientboundSetChunkCacheRadiusPacket(3);

        assertEquals(PacketType.Play.Server.VIEW_DISTANCE, w.getHandle().getType());

        ClientboundSetChunkCacheRadiusPacket p = (ClientboundSetChunkCacheRadiusPacket) w.getHandle().getHandle();

        assertEquals(3, p.getRadius());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetChunkCacheRadiusPacket w = new WrappedClientboundSetChunkCacheRadiusPacket();

        assertEquals(PacketType.Play.Server.VIEW_DISTANCE, w.getHandle().getType());

        ClientboundSetChunkCacheRadiusPacket p = (ClientboundSetChunkCacheRadiusPacket) w.getHandle().getHandle();

        assertEquals(0, p.getRadius());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetChunkCacheRadiusPacket nmsPacket = new ClientboundSetChunkCacheRadiusPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetChunkCacheRadiusPacket wrapper = new WrappedClientboundSetChunkCacheRadiusPacket(container);

        assertEquals(3, wrapper.getViewDistance());

        wrapper.setViewDistance(9);

        assertEquals(9, nmsPacket.getRadius());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetChunkCacheRadiusPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
