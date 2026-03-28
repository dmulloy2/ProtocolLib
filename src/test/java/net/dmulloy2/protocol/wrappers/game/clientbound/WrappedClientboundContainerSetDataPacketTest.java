package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundContainerSetDataPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundContainerSetDataPacket w = new WrappedClientboundContainerSetDataPacket();
        w.setWindowId(1);
        w.setProperty(4);
        w.setValue(100);

        assertEquals(PacketType.Play.Server.WINDOW_DATA, w.getHandle().getType());

        ClientboundContainerSetDataPacket p = (ClientboundContainerSetDataPacket) w.getHandle().getHandle();

        assertEquals(1, p.getContainerId());
        assertEquals(4, p.getId());
        assertEquals(100, p.getValue());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundContainerSetDataPacket nmsPacket = new ClientboundContainerSetDataPacket(2, 0, 50);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerSetDataPacket wrapper = new WrappedClientboundContainerSetDataPacket(container);

        assertEquals(2, wrapper.getWindowId());
        assertEquals(0, wrapper.getProperty());
        assertEquals(50, wrapper.getValue());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundContainerSetDataPacket nmsPacket = new ClientboundContainerSetDataPacket(2, 0, 50);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerSetDataPacket wrapper = new WrappedClientboundContainerSetDataPacket(container);

        wrapper.setValue(200);

        assertEquals(2, wrapper.getWindowId());
        assertEquals(0, wrapper.getProperty());
        assertEquals(200, wrapper.getValue());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetDataPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
