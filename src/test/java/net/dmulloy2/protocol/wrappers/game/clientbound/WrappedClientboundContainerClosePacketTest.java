package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundContainerClosePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundContainerClosePacket w = new WrappedClientboundContainerClosePacket(3);

        assertEquals(PacketType.Play.Server.CLOSE_WINDOW, w.getHandle().getType());

        ClientboundContainerClosePacket p = (ClientboundContainerClosePacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundContainerClosePacket w = new WrappedClientboundContainerClosePacket();

        assertEquals(PacketType.Play.Server.CLOSE_WINDOW, w.getHandle().getType());

        ClientboundContainerClosePacket p = (ClientboundContainerClosePacket) w.getHandle().getHandle();

        assertEquals(0, p.getContainerId());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundContainerClosePacket nmsPacket = new ClientboundContainerClosePacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundContainerClosePacket wrapper = new WrappedClientboundContainerClosePacket(container);

        assertEquals(3, wrapper.getWindowId());

        wrapper.setWindowId(9);

        assertEquals(9, nmsPacket.getContainerId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundContainerClosePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
