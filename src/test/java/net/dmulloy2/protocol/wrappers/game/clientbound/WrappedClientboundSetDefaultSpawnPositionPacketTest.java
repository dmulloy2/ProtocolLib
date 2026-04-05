package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetDefaultSpawnPositionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetDefaultSpawnPositionPacket w = new WrappedClientboundSetDefaultSpawnPositionPacket((Location) null);

        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());

        assertEquals(null, w.getLocation());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetDefaultSpawnPositionPacket w = new WrappedClientboundSetDefaultSpawnPositionPacket();

        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSetDefaultSpawnPositionPacket source = new WrappedClientboundSetDefaultSpawnPositionPacket((Location) null);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetDefaultSpawnPositionPacket wrapper = new WrappedClientboundSetDefaultSpawnPositionPacket(container);

        assertEquals(null, wrapper.getLocation());

        wrapper.setLocation(null);

        assertEquals(null, wrapper.getLocation());

        assertEquals(null, source.getLocation());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetDefaultSpawnPositionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
