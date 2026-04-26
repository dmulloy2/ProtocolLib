package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundAnimatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        // Entity packets use the int-entityId constructor in tests (per spec)
        WrappedClientboundAnimatePacket w = new WrappedClientboundAnimatePacket(3, 7);

        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());

        ClientboundAnimatePacket p = (ClientboundAnimatePacket) w.getHandle().getHandle();
        assertEquals(3, p.getId());
        assertEquals(7, p.getAction());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundAnimatePacket w = new WrappedClientboundAnimatePacket();

        assertEquals(PacketType.Play.Server.ANIMATION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        // Per spec: for entity packets, use the int-entityId constructor, grab its handle,
        // and create another wrapper from that handle to verify the fields.
        WrappedClientboundAnimatePacket source = new WrappedClientboundAnimatePacket(3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundAnimatePacket wrapper = new WrappedClientboundAnimatePacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(7, wrapper.getAnimationId());

        wrapper.setEntityId(9);
        wrapper.setAnimationId(4);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(4, wrapper.getAnimationId());

        assertEquals(9, source.getEntityId());
        assertEquals(4, source.getAnimationId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundAnimatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
