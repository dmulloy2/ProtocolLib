package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.world.level.storage.LevelData;
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
        Location loc = new Location(null, 10, 64, 20, 45.0f, 0.0f);
        WrappedClientboundSetDefaultSpawnPositionPacket w = new WrappedClientboundSetDefaultSpawnPositionPacket(loc);

        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());

        Location result = w.getLocation();
        assertNotNull(result);
        assertEquals(10, result.getBlockX());
        assertEquals(64, result.getBlockY());
        assertEquals(20, result.getBlockZ());
        assertEquals(45.0f, result.getYaw(), 0.001f);
        assertEquals(0.0f, result.getPitch(), 0.001f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetDefaultSpawnPositionPacket w = new WrappedClientboundSetDefaultSpawnPositionPacket();

        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        Location loc = new Location(null, 10, 64, 20, 45.0f, 0.0f);
        WrappedClientboundSetDefaultSpawnPositionPacket source = new WrappedClientboundSetDefaultSpawnPositionPacket(loc);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetDefaultSpawnPositionPacket wrapper = new WrappedClientboundSetDefaultSpawnPositionPacket(container);

        assertEquals(10, wrapper.getLocation().getBlockX());
        assertEquals(64, wrapper.getLocation().getBlockY());
        assertEquals(20, wrapper.getLocation().getBlockZ());

        Location newLoc = new Location(null, -50, 100, 30, 90.0f, -10.0f);
        wrapper.setLocation(newLoc);

        assertEquals(-50, wrapper.getLocation().getBlockX());
        assertEquals(100, wrapper.getLocation().getBlockY());
        assertEquals(30, wrapper.getLocation().getBlockZ());
        assertEquals(90.0f, wrapper.getLocation().getYaw(), 0.001f);
        assertEquals(-10.0f, wrapper.getLocation().getPitch(), 0.001f);

        assertEquals(-50, source.getLocation().getBlockX());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetDefaultSpawnPositionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
