package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundSpawnPositionTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundSpawnPosition w = new WrapperGameClientboundSpawnPosition();
        w.setYaw(90.0f);
        w.setPitch(-10.0f);
        assertEquals(90.0f, w.getYaw(), 1e-4f);
        assertEquals(-10.0f, w.getPitch(), 1e-4f);
        assertEquals(PacketType.Play.Server.SPAWN_POSITION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.SPAWN_POSITION);
        raw.getModifier().writeDefaults();
        raw.getStructures().read(0).getFloat().write(0, 45.0f);  // yaw
        raw.getStructures().read(0).getFloat().write(1, -5.0f);  // pitch

        WrapperGameClientboundSpawnPosition w = new WrapperGameClientboundSpawnPosition(raw);
        assertEquals(45.0f, w.getYaw(), 1e-4f);
        assertEquals(-5.0f, w.getPitch(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundSpawnPosition w = new WrapperGameClientboundSpawnPosition();
        w.setYaw(0.0f);

        new WrapperGameClientboundSpawnPosition(w.getHandle()).setYaw(180.0f);

        assertEquals(180.0f, w.getYaw(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundSpawnPosition(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
