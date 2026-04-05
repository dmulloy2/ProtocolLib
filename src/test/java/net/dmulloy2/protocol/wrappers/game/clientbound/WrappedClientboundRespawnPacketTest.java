package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRespawnPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundRespawnPacket w = new WrappedClientboundRespawnPacket((byte) 3);

        assertEquals(PacketType.Play.Server.RESPAWN, w.getHandle().getType());

        assertEquals((byte) 3, w.getDataToKeep());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRespawnPacket w = new WrappedClientboundRespawnPacket();

        assertEquals(PacketType.Play.Server.RESPAWN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundRespawnPacket source = new WrappedClientboundRespawnPacket((byte) 3);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRespawnPacket wrapper = new WrappedClientboundRespawnPacket(container);

        assertEquals((byte) 3, wrapper.getDataToKeep());

        wrapper.setDataToKeep((byte) 15);

        assertEquals((byte) 15, wrapper.getDataToKeep());

        assertEquals((byte) 15, source.getDataToKeep());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRespawnPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
