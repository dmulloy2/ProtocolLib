package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateAttributesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundUpdateAttributesPacket w = new WrappedClientboundUpdateAttributesPacket(3, List.of());

        assertEquals(PacketType.Play.Server.UPDATE_ATTRIBUTES, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(List.of(), w.getAttributes());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateAttributesPacket w = new WrappedClientboundUpdateAttributesPacket();

        assertEquals(PacketType.Play.Server.UPDATE_ATTRIBUTES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundUpdateAttributesPacket source = new WrappedClientboundUpdateAttributesPacket(3, List.of());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundUpdateAttributesPacket wrapper = new WrappedClientboundUpdateAttributesPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(List.of(), wrapper.getAttributes());

        wrapper.setEntityId(9);
        wrapper.setAttributes(List.of());

        assertEquals(9, wrapper.getEntityId());
        assertEquals(List.of(), wrapper.getAttributes());

        assertEquals(9, source.getEntityId());
        assertEquals(List.of(), source.getAttributes());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateAttributesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
