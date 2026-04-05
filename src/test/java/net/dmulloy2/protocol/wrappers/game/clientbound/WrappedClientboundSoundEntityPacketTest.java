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
    void testAllArgsCreate() {
        WrappedClientboundSoundEntityPacket w = new WrappedClientboundSoundEntityPacket(Sound.ENTITY_PLAYER_HURT, EnumWrappers.SoundCategory.MASTER, 5, 0.75f, 0.5f, -1L);

        assertEquals(PacketType.Play.Server.ENTITY_SOUND, w.getHandle().getType());

        assertEquals(Sound.ENTITY_PLAYER_HURT, w.getSound());
        assertEquals(EnumWrappers.SoundCategory.MASTER, w.getCategory());
        assertEquals(5, w.getEntityId());
        assertEquals(0.75f, w.getVolume(), 1e-4f);
        assertEquals(0.5f, w.getPitch(), 1e-4f);
        assertEquals(-1L, w.getSeed());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSoundEntityPacket w = new WrappedClientboundSoundEntityPacket();

        assertEquals(PacketType.Play.Server.ENTITY_SOUND, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundSoundEntityPacket source = new WrappedClientboundSoundEntityPacket(Sound.ENTITY_PLAYER_HURT, EnumWrappers.SoundCategory.MASTER, 5, 0.75f, 0.5f, -1L);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSoundEntityPacket wrapper = new WrappedClientboundSoundEntityPacket(container);

        assertEquals(Sound.ENTITY_PLAYER_HURT, wrapper.getSound());
        assertEquals(EnumWrappers.SoundCategory.MASTER, wrapper.getCategory());
        assertEquals(5, wrapper.getEntityId());
        assertEquals(0.75f, wrapper.getVolume(), 1e-4f);
        assertEquals(0.5f, wrapper.getPitch(), 1e-4f);
        assertEquals(-1L, wrapper.getSeed());

        wrapper.setSound(Sound.ENTITY_ZOMBIE_HURT);
        wrapper.setCategory(EnumWrappers.SoundCategory.MUSIC);
        wrapper.setEntityId(0);
        wrapper.setVolume(10.5f);
        wrapper.setPitch(0.25f);
        wrapper.setSeed(0L);

        assertEquals(Sound.ENTITY_ZOMBIE_HURT, wrapper.getSound());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, wrapper.getCategory());
        assertEquals(0, wrapper.getEntityId());
        assertEquals(10.5f, wrapper.getVolume(), 1e-4f);
        assertEquals(0.25f, wrapper.getPitch(), 1e-4f);
        assertEquals(0L, wrapper.getSeed());

        assertEquals(Sound.ENTITY_ZOMBIE_HURT, source.getSound());
        assertEquals(EnumWrappers.SoundCategory.MUSIC, source.getCategory());
        assertEquals(0, source.getEntityId());
        assertEquals(10.5f, source.getVolume(), 1e-4f);
        assertEquals(0.25f, source.getPitch(), 1e-4f);
        assertEquals(0L, source.getSeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSoundEntityPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
