package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRemoveEntitiesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundRemoveEntitiesPacket w = new WrappedClientboundRemoveEntitiesPacket(1, 2, 3);

        assertEquals(PacketType.Play.Server.ENTITY_DESTROY, w.getHandle().getType());

        assertEquals(List.of(1, 2, 3), w.getEntityIds());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRemoveEntitiesPacket w = new WrappedClientboundRemoveEntitiesPacket();

        assertEquals(PacketType.Play.Server.ENTITY_DESTROY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundRemoveEntitiesPacket source = new WrappedClientboundRemoveEntitiesPacket(1, 2, 3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRemoveEntitiesPacket wrapper = new WrappedClientboundRemoveEntitiesPacket(container);

        assertEquals(List.of(1, 2, 3), wrapper.getEntityIds());

        wrapper.setEntityIds(List.of(99, 100));

        assertEquals(List.of(99, 100), wrapper.getEntityIds());

        assertEquals(List.of(99, 100), source.getEntityIds());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRemoveEntitiesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
