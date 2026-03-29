package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSoundPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSoundPacket w = new WrappedClientboundSoundPacket();
        w.setCategory(EnumWrappers.SoundCategory.PLAYERS);
        w.setPosition(10.0, 64.0, -5.0);
        w.setVolume(1.0f);
        w.setPitch(1.5f);
        w.setSeed(77777L);

        assertEquals(PacketType.Play.Server.NAMED_SOUND_EFFECT, w.getHandle().getType());

        ClientboundSoundPacket p = (ClientboundSoundPacket) w.getHandle().getHandle();

        assertEquals(1.0f, p.getVolume(), 1e-4f);
        assertEquals(1.5f, p.getPitch(), 1e-4f);
        assertEquals(77777L, p.getSeed());
        assertEquals(10.0, w.getActualX(), 1e-6);
        assertEquals(64.0, w.getActualY(), 1e-6);
        assertEquals(-5.0, w.getActualZ(), 1e-6);
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 80);  // x * 8 = 10.0
        raw.getIntegers().write(1, 512); // y * 8 = 64.0
        raw.getIntegers().write(2, -40); // z * 8 = -5.0
        raw.getFloat().write(0, 0.8f);
        raw.getFloat().write(1, 1.0f);
        raw.getLongs().write(0, 42L);

        WrappedClientboundSoundPacket wrapper = new WrappedClientboundSoundPacket(raw);

        assertEquals(80, wrapper.getX());
        assertEquals(10.0, wrapper.getActualX(), 1e-6);
        assertEquals(0.8f, wrapper.getVolume(), 1e-4f);
        assertEquals(1.0f, wrapper.getPitch(), 1e-4f);
        assertEquals(42L, wrapper.getSeed());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.NAMED_SOUND_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getFloat().write(0, 1.0f);
        raw.getFloat().write(1, 1.0f);
        raw.getLongs().write(0, 1L);

        WrappedClientboundSoundPacket wrapper = new WrappedClientboundSoundPacket(raw);
        wrapper.setVolume(0.25f);

        assertEquals(0.25f, wrapper.getVolume(), 1e-4f);
        assertEquals(1.0f, wrapper.getPitch(), 1e-4f);
        assertEquals(1L, wrapper.getSeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSoundPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
