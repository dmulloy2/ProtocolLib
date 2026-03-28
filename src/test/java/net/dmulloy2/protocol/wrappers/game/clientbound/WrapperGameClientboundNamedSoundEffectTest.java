package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundNamedSoundEffectTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundNamedSoundEffect w = new WrapperGameClientboundNamedSoundEffect();
        w.setCategory(EnumWrappers.SoundCategory.PLAYERS);
        w.setPosition(10.0, 64.0, -5.0);
        w.setVolume(1.0f);
        w.setPitch(1.5f);
        w.setSeed(77777L);
        assertEquals(EnumWrappers.SoundCategory.PLAYERS, w.getCategory());
        assertEquals(10.0 * 8, w.getX(), 1.0);
        assertEquals(64.0 * 8, w.getY(), 1.0);
        assertEquals(-5.0 * 8, w.getZ(), 1.0);
        assertEquals(10.0, w.getActualX(), 1e-6);
        assertEquals(64.0, w.getActualY(), 1e-6);
        assertEquals(-5.0, w.getActualZ(), 1e-6);
        assertEquals(1.0f, w.getVolume(), 1e-6f);
        assertEquals(1.5f, w.getPitch(), 1e-6f);
        assertEquals(77777L, w.getSeed());
        assertEquals(PacketType.Play.Server.NAMED_SOUND_EFFECT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 80);  // x * 8 = 10
        raw.getIntegers().write(1, 512); // y * 8 = 64
        raw.getIntegers().write(2, -40); // z * 8 = -5
        raw.getFloat().write(0, 0.8f);
        raw.getFloat().write(1, 1.0f);
        raw.getLongs().write(0, 42L);

        WrapperGameClientboundNamedSoundEffect w = new WrapperGameClientboundNamedSoundEffect(raw);
        assertEquals(80, w.getX());
        assertEquals(10.0, w.getActualX(), 1e-6);
        assertEquals(0.8f, w.getVolume(), 1e-6f);
        assertEquals(42L, w.getSeed());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundNamedSoundEffect w = new WrapperGameClientboundNamedSoundEffect();
        w.setVolume(1.0f);

        new WrapperGameClientboundNamedSoundEffect(w.getHandle()).setVolume(0.25f);

        assertEquals(0.25f, w.getVolume(), 1e-6f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundNamedSoundEffect(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
