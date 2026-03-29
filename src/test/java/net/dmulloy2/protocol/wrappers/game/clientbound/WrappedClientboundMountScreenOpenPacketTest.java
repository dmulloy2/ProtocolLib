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
    void testCreate() {
        WrappedClientboundMountScreenOpenPacket w = new WrappedClientboundMountScreenOpenPacket();
        w.setWindowId(3);
        w.setContainerSize(6);
        w.setEntityId(55);

        assertEquals(PacketType.Play.Server.OPEN_WINDOW_HORSE, w.getHandle().getType());

        ClientboundMountScreenOpenPacket p = (ClientboundMountScreenOpenPacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
        assertEquals(6, p.getInventoryColumns());
        assertEquals(55, p.getEntityId());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundMountScreenOpenPacket nmsPacket = new ClientboundMountScreenOpenPacket(1, 3, 100);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMountScreenOpenPacket wrapper = new WrappedClientboundMountScreenOpenPacket(container);

        assertEquals(1, wrapper.getWindowId());
        assertEquals(3, wrapper.getContainerSize());
        assertEquals(100, wrapper.getEntityId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundMountScreenOpenPacket nmsPacket = new ClientboundMountScreenOpenPacket(1, 3, 100);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMountScreenOpenPacket wrapper = new WrappedClientboundMountScreenOpenPacket(container);

        wrapper.setEntityId(77);

        assertEquals(1, wrapper.getWindowId());
        assertEquals(3, wrapper.getContainerSize());
        assertEquals(77, wrapper.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMountScreenOpenPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
