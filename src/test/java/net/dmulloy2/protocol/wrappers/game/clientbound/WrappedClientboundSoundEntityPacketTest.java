package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
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

        assertEquals(PacketType.Play.Server.ENTITY_SOUND, w.getHandle().getType());

        ClientboundSoundEntityPacket p = (ClientboundSoundEntityPacket) w.getHandle().getHandle();

        assertEquals(15, p.getId());
        assertEquals(1.0f, p.getVolume(), 1e-4f);
        assertEquals(1.0f, p.getPitch(), 1e-4f);
        assertEquals(12345L, p.getSeed());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 20);
        raw.getFloat().write(0, 0.5f);
        raw.getFloat().write(1, 2.0f);
        raw.getLongs().write(0, 999L);

        WrappedClientboundSoundEntityPacket wrapper = new WrappedClientboundSoundEntityPacket(raw);

        assertEquals(20, wrapper.getEntityId());
        assertEquals(0.5f, wrapper.getVolume(), 1e-4f);
        assertEquals(2.0f, wrapper.getPitch(), 1e-4f);
        assertEquals(999L, wrapper.getSeed());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_SOUND);
        raw.getModifier().writeDefaults();
        raw.getFloat().write(0, 1.0f);
        raw.getFloat().write(1, 1.0f);
        raw.getLongs().write(0, 1L);

        WrappedClientboundSoundEntityPacket wrapper = new WrappedClientboundSoundEntityPacket(raw);
        wrapper.setVolume(0.5f);

        assertEquals(0.5f, wrapper.getVolume(), 1e-4f);
        assertEquals(1.0f, wrapper.getPitch(), 1e-4f);
        assertEquals(1L, wrapper.getSeed());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSoundEntityPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
