package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetEntityDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetEntityDataPacket w = new WrappedClientboundSetEntityDataPacket(3, List.of());

        assertEquals(PacketType.Play.Server.ENTITY_METADATA, w.getHandle().getType());

        assertEquals(3, w.getId());
        assertEquals(List.of(), w.getPackedItems());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetEntityDataPacket w = new WrappedClientboundSetEntityDataPacket();

        assertEquals(PacketType.Play.Server.ENTITY_METADATA, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetEntityDataPacket source = new WrappedClientboundSetEntityDataPacket(3, List.of());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetEntityDataPacket wrapper = new WrappedClientboundSetEntityDataPacket(container);

        assertEquals(3, wrapper.getId());
        assertEquals(List.of(), wrapper.getPackedItems());

        wrapper.setId(9);
        wrapper.setPackedItems(List.of());

        assertEquals(9, wrapper.getId());
        assertEquals(List.of(), wrapper.getPackedItems());

        assertEquals(9, source.getId());
        assertEquals(List.of(), source.getPackedItems());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetEntityDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
