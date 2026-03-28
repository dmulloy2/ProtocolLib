package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSoundEntityPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSoundEntityPacket w = new WrappedClientboundSoundEntityPacket();
        w.setSound(Sound.ENTITY_CAT_HISS);
        w.setCategory(EnumWrappers.SoundCategory.PLAYERS);
        w.setEntityId(15);
        w.setVolume(1.0f);
        w.setPitch(1.0f);
        w.setSeed(12345L);
        assertEquals(Sound.ENTITY_CAT_HISS, w.getSound());
        assertEquals(EnumWrappers.SoundCategory.PLAYERS, w.getCategory());
        assertEquals(15, w.getEntityId());
        assertEquals(1.0f, w.getVolume(), 1e-6f);
        assertEquals(1.0f, w.getPitch(), 1e-6f);
        assertEquals(12345L, w.getSeed());
        assertEquals(PacketType.Play.Server.ENTITY_SOUND, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 20);
        raw.getFloat().write(0, 0.5f);
        raw.getFloat().write(1, 2.0f);
        raw.getLongs().write(0, 999L);

        WrappedClientboundSoundEntityPacket w = new WrappedClientboundSoundEntityPacket(raw);
        assertEquals(20, w.getEntityId());
        assertEquals(0.5f, w.getVolume(), 1e-6f);
        assertEquals(2.0f, w.getPitch(), 1e-6f);
        assertEquals(999L, w.getSeed());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSoundEntityPacket w = new WrappedClientboundSoundEntityPacket();
        w.setVolume(1.0f);

        new WrappedClientboundSoundEntityPacket(w.getHandle()).setVolume(0.5f);

        assertEquals(0.5f, w.getVolume(), 1e-6f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSoundEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
