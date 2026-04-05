package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPongPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPongPacket w = new WrappedServerboundPongPacket(3);

        assertEquals(PacketType.Configuration.Client.PONG, w.getHandle().getType());

        ServerboundPongPacket p = (ServerboundPongPacket) w.getHandle().getHandle();

        assertEquals(3, p.getId());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPongPacket w = new WrappedServerboundPongPacket();

        assertEquals(PacketType.Configuration.Client.PONG, w.getHandle().getType());

        ServerboundPongPacket p = (ServerboundPongPacket) w.getHandle().getHandle();

        assertEquals(0, p.getId());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundPongPacket nmsPacket = new ServerboundPongPacket(3);
        PacketContainer container = new PacketContainer(WrappedServerboundPongPacket.TYPE, nmsPacket);
        WrappedServerboundPongPacket wrapper = new WrappedServerboundPongPacket(container);

        assertEquals(3, wrapper.getId());

        wrapper.setId(9);

        assertEquals(9, nmsPacket.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPongPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
