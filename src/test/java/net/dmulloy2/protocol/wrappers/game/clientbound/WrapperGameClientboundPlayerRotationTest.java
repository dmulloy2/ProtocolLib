package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundPlayerRotationTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundPlayerRotation w = new WrapperGameClientboundPlayerRotation();
        w.setYaw(90.0f);
        w.setPitch(-30.0f);
        assertEquals(90.0f, w.getYaw(), 1e-6f);
        assertEquals(-30.0f, w.getPitch(), 1e-6f);
        assertEquals(PacketType.Play.Server.PLAYER_ROTATION, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.PLAYER_ROTATION);
        raw.getModifier().writeDefaults();
        raw.getFloat().write(0, 45.0f);
        raw.getFloat().write(1, 10.0f);

        WrapperGameClientboundPlayerRotation w = new WrapperGameClientboundPlayerRotation(raw);
        assertEquals(45.0f, w.getYaw(), 1e-6f);
        assertEquals(10.0f, w.getPitch(), 1e-6f);
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundPlayerRotation w = new WrapperGameClientboundPlayerRotation();
        w.setYaw(0.0f);

        new WrapperGameClientboundPlayerRotation(w.getHandle()).setYaw(180.0f);

        assertEquals(180.0f, w.getYaw(), 1e-6f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundPlayerRotation(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
