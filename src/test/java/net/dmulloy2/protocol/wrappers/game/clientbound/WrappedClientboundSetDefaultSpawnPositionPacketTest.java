package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetDefaultSpawnPositionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetDefaultSpawnPositionPacket w = new WrappedClientboundSetDefaultSpawnPositionPacket();
        w.setYaw(90.0f);
        w.setPitch(-10.0f);

        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());
        assertEquals(90.0f, w.getYaw(), 1e-4f);
        assertEquals(-10.0f, w.getPitch(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SPAWN_POSITION);
        container.getModifier().writeDefaults();
        container.getStructures().read(0).getFloat().write(0, 45.0f);
        container.getStructures().read(0).getFloat().write(1, -5.0f);

        WrappedClientboundSetDefaultSpawnPositionPacket wrapper = new WrappedClientboundSetDefaultSpawnPositionPacket(container);

        assertEquals(45.0f, wrapper.getYaw(), 1e-4f);
        assertEquals(-5.0f, wrapper.getPitch(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SPAWN_POSITION);
        container.getModifier().writeDefaults();
        container.getStructures().read(0).getFloat().write(0, 0.0f);

        WrappedClientboundSetDefaultSpawnPositionPacket wrapper = new WrappedClientboundSetDefaultSpawnPositionPacket(container);
        wrapper.setYaw(180.0f);

        assertEquals(180.0f, wrapper.getYaw(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetDefaultSpawnPositionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
