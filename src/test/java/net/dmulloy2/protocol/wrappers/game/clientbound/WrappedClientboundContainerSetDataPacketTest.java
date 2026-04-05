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
    void testAllArgsCreate() {
        WrappedClientboundContainerSetDataPacket w = new WrappedClientboundContainerSetDataPacket(3, 7, 5);

        assertEquals(PacketType.Play.Server.WINDOW_DATA, w.getHandle().getType());

        ClientboundContainerSetDataPacket p = (ClientboundContainerSetDataPacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
        assertEquals(7, p.getId());
        assertEquals(5, p.getValue());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundContainerSetDataPacket w = new WrappedClientboundContainerSetDataPacket();

        assertEquals(PacketType.Play.Server.WINDOW_DATA, w.getHandle().getType());

        ClientboundContainerSetDataPacket p = (ClientboundContainerSetDataPacket) w.getHandle().getHandle();

        assertEquals(0, p.getContainerId());
        assertEquals(0, p.getId());
        assertEquals(0, p.getValue());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundContainerSetDataPacket nmsPacket = new ClientboundContainerSetDataPacket(3, 7, 5);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerSetDataPacket wrapper = new WrappedClientboundContainerSetDataPacket(container);

        assertEquals(3, wrapper.getWindowId());
        assertEquals(7, wrapper.getProperty());
        assertEquals(5, wrapper.getValue());

        wrapper.setWindowId(9);
        wrapper.setProperty(-5);
        wrapper.setValue(0);

        assertEquals(9, nmsPacket.getContainerId());
        assertEquals(-5, nmsPacket.getId());
        assertEquals(0, nmsPacket.getValue());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerSetDataPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
