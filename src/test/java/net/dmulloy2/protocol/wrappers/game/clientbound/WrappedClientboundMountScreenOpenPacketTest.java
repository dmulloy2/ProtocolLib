package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundMountScreenOpenPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMountScreenOpenPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMountScreenOpenPacket w = new WrappedClientboundMountScreenOpenPacket(3, 7, 5);

        assertEquals(PacketType.Play.Server.OPEN_WINDOW_HORSE, w.getHandle().getType());

        ClientboundMountScreenOpenPacket p = (ClientboundMountScreenOpenPacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
        assertEquals(7, p.getInventoryColumns());
        assertEquals(5, p.getEntityId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMountScreenOpenPacket w = new WrappedClientboundMountScreenOpenPacket();

        assertEquals(PacketType.Play.Server.OPEN_WINDOW_HORSE, w.getHandle().getType());

        ClientboundMountScreenOpenPacket p = (ClientboundMountScreenOpenPacket) w.getHandle().getHandle();

        assertEquals(0, p.getContainerId());
        assertEquals(0, p.getInventoryColumns());
        assertEquals(0, p.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundMountScreenOpenPacket nmsPacket = new ClientboundMountScreenOpenPacket(3, 7, 5);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMountScreenOpenPacket wrapper = new WrappedClientboundMountScreenOpenPacket(container);

        assertEquals(3, wrapper.getWindowId());
        assertEquals(7, wrapper.getContainerSize());
        assertEquals(5, wrapper.getEntityId());

        wrapper.setWindowId(9);
        wrapper.setContainerSize(-5);
        wrapper.setEntityId(0);

        assertEquals(9, nmsPacket.getContainerId());
        assertEquals(-5, nmsPacket.getInventoryColumns());
        assertEquals(0, nmsPacket.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMountScreenOpenPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
