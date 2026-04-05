package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEntityLinkPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetEntityLinkPacket w = new WrappedClientboundSetEntityLinkPacket(3, 7);

        assertEquals(PacketType.Play.Server.ATTACH_ENTITY, w.getHandle().getType());

        assertEquals(3, w.getAttachedEntityId());
        assertEquals(7, w.getHoldingEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetEntityLinkPacket w = new WrappedClientboundSetEntityLinkPacket();

        assertEquals(PacketType.Play.Server.ATTACH_ENTITY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetEntityLinkPacket source = new WrappedClientboundSetEntityLinkPacket(3, 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEntityLinkPacket wrapper = new WrappedClientboundSetEntityLinkPacket(container);

        assertEquals(3, wrapper.getAttachedEntityId());
        assertEquals(7, wrapper.getHoldingEntityId());

        wrapper.setAttachedEntityId(9);
        wrapper.setHoldingEntityId(-5);

        assertEquals(9, wrapper.getAttachedEntityId());
        assertEquals(-5, wrapper.getHoldingEntityId());

        assertEquals(9, source.getAttachedEntityId());
        assertEquals(-5, source.getHoldingEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEntityLinkPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
