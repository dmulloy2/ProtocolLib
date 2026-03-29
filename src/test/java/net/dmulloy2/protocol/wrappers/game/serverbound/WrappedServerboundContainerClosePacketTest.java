package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundContainerClosePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundContainerClosePacket w = new WrappedServerboundContainerClosePacket(5);

        assertEquals(PacketType.Play.Client.CLOSE_WINDOW, w.getHandle().getType());

        ServerboundContainerClosePacket p = (ServerboundContainerClosePacket) w.getHandle().getHandle();

        assertEquals(5, p.getContainerId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundContainerClosePacket w = new WrappedServerboundContainerClosePacket();

        assertEquals(PacketType.Play.Client.CLOSE_WINDOW, w.getHandle().getType());

        ServerboundContainerClosePacket p = (ServerboundContainerClosePacket) w.getHandle().getHandle();

        assertEquals(0, p.getContainerId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundContainerClosePacket nmsPacket = new ServerboundContainerClosePacket(5);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerClosePacket wrapper = new WrappedServerboundContainerClosePacket(container);

        assertEquals(5, wrapper.getContainerId());

        wrapper.setContainerId(12);

        assertEquals(12, nmsPacket.getContainerId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundContainerClosePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
