package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRemoveEntitiesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundRemoveEntitiesPacket w = new WrappedClientboundRemoveEntitiesPacket();
        w.setEntityIds(List.of(10, 20, 30));

        assertEquals(PacketType.Play.Server.ENTITY_DESTROY, w.getHandle().getType());

        ClientboundRemoveEntitiesPacket p = (ClientboundRemoveEntitiesPacket) w.getHandle().getHandle();

        assertEquals(3, p.getEntityIds().size());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundRemoveEntitiesPacket nmsPacket = new ClientboundRemoveEntitiesPacket(1, 2, 3);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRemoveEntitiesPacket wrapper = new WrappedClientboundRemoveEntitiesPacket(container);

        assertEquals(3, wrapper.getEntityIds().size());
        assertTrue(wrapper.getEntityIds().contains(1));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundRemoveEntitiesPacket nmsPacket = new ClientboundRemoveEntitiesPacket(1, 2, 3);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRemoveEntitiesPacket wrapper = new WrappedClientboundRemoveEntitiesPacket(container);

        wrapper.setEntityIds(List.of(5, 6, 7, 8));

        assertEquals(4, wrapper.getEntityIds().size());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRemoveEntitiesPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
