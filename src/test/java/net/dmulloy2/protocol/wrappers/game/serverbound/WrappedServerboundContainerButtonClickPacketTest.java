package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundContainerButtonClickPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundContainerButtonClickPacket w = new WrappedServerboundContainerButtonClickPacket(3, 7);

        assertEquals(PacketType.Play.Client.ENCHANT_ITEM, w.getHandle().getType());

        ServerboundContainerButtonClickPacket p = (ServerboundContainerButtonClickPacket) w.getHandle().getHandle();

        assertEquals(3, p.containerId());
        assertEquals(7, p.buttonId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundContainerButtonClickPacket w = new WrappedServerboundContainerButtonClickPacket();

        assertEquals(PacketType.Play.Client.ENCHANT_ITEM, w.getHandle().getType());

        ServerboundContainerButtonClickPacket p = (ServerboundContainerButtonClickPacket) w.getHandle().getHandle();

        assertEquals(0, p.containerId());
        assertEquals(0, p.buttonId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundContainerButtonClickPacket nmsPacket = new ServerboundContainerButtonClickPacket(3, 7);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundContainerButtonClickPacket wrapper = new WrappedServerboundContainerButtonClickPacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertEquals(7, wrapper.getButtonId());

        wrapper.setContainerId(9);
        wrapper.setButtonId(-5);

        assertEquals(9, nmsPacket.containerId());
        assertEquals(-5, nmsPacket.buttonId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundContainerButtonClickPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
