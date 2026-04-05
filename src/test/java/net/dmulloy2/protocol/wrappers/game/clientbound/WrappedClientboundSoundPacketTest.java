package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.Sound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSoundPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSoundPacket w = new WrappedClientboundSoundPacket(Sound.ENTITY_PLAYER_HURT, EnumWrappers.SoundCategory.MASTER, 5, 3, 7, -3.0f, 0.75f, 42L);

        assertEquals(PacketType.Play.Server.NAMED_SOUND_EFFECT, w.getHandle().getType());

        assertEquals(Sound.ENTITY_PLAYER_HURT, w.getSound());
        assertEquals(EnumWrappers.SoundCategory.MASTER, w.getCategory());
        assertEquals(5, w.getX());
        assertEquals(3, w.getY());
        assertEquals(7, w.getZ());
        assertEquals(-3.0f, w.getVolume(), 1e-4f);
        assertEquals(0.75f, w.getPitch(), 1e-4f);
        assertEquals(42L, w.getSeed());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSoundPacket w = new WrappedClientboundSoundPacket();

        assertEquals(PacketType.Play.Server.NAMED_SOUND_EFFECT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSoundPacket source = new WrappedClientboundSoundPacket(Sound.ENTITY_PLAYER_HURT, EnumWrappers.SoundCategory.MASTER, 5, 3, 7, -3.0f, 0.75f, 42L);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSoundPacket wrapper = new WrappedClientboundSoundPacket(container);

        assertEquals(Sound.ENTITY_PLAYER_HURT, wrapper.getSound());
        assertEquals(EnumWrappers.SoundCategory.MASTER, wrapper.getCategory());
        assertEquals(5, wrapper.getX());
        assertEquals(3, wrapper.getY());
        assertEquals(7, wrapper.getZ());
        assertEquals(-3.0f, wrapper.getVolume(), 1e-4f);
        assertEquals(0.75f, wrapper.getPitch(), 1e-4f);
        assertEquals(42L, wrapper.getSeed());

        wrapper.setSound(Sound.ENTITY_ZOMBIE_HURT);
        wrapper.setCategory(EnumWrappers.SoundCategory.MUSIC);
        wrapper.setX(0);
        wrapper.setY(42);
        wrapper.setZ(9);
        wrapper.setVolume(1.0f);
        wrapper.setPitch(1.0f);
        wrapper.setSeed(987654321L);

        assertEquals(Sound.ENTITY_ZOMBIE_HURT, wrapper.getSound());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, wrapper.getCategory());
        assertEquals(0, wrapper.getX());
        assertEquals(42, wrapper.getY());
        assertEquals(9, wrapper.getZ());
        assertEquals(1.0f, wrapper.getVolume(), 1e-4f);
        assertEquals(1.0f, wrapper.getPitch(), 1e-4f);
        assertEquals(987654321L, wrapper.getSeed());

        assertEquals(Sound.ENTITY_ZOMBIE_HURT, source.getSound());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, source.getCategory());
        assertEquals(0, source.getX());
        assertEquals(42, source.getY());
        assertEquals(9, source.getZ());
        assertEquals(1.0f, source.getVolume(), 1e-4f);
        assertEquals(1.0f, source.getPitch(), 1e-4f);
        assertEquals(987654321L, source.getSeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSoundPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
