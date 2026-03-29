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
    void testCreate() {
        WrappedServerboundContainerClosePacket w = new WrappedServerboundContainerClosePacket();
        w.setContainerId(3);

        assertEquals(PacketType.Play.Client.CLOSE_WINDOW, w.getHandle().getType());

        ServerboundContainerClosePacket p = (ServerboundContainerClosePacket) w.getHandle().getHandle();

        assertEquals(3, p.getContainerId());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundContainerClosePacket nmsPacket = new ServerboundContainerClosePacket(5);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerClosePacket wrapper = new WrappedServerboundContainerClosePacket(container);

        assertEquals(5, wrapper.getContainerId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundContainerClosePacket nmsPacket = new ServerboundContainerClosePacket(5);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerClosePacket wrapper = new WrappedServerboundContainerClosePacket(container);

        wrapper.setContainerId(10);

        assertEquals(10, wrapper.getContainerId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundContainerClosePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
