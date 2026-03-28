package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateMobEffectPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundUpdateMobEffectPacket w = new WrappedClientboundUpdateMobEffectPacket();
        w.setEntityId(4);
        w.setEffectType(PotionEffectType.SPEED);
        w.setAmplifier(1);
        w.setDuration(200);
        w.setFlags(WrappedClientboundUpdateMobEffectPacket.FLAG_SHOW_PARTICLES);
        assertEquals(4, w.getEntityId());
        assertEquals(PotionEffectType.SPEED, w.getEffectType());
        assertEquals(1, w.getAmplifier());
        assertEquals(200, w.getDuration());
        assertEquals(WrappedClientboundUpdateMobEffectPacket.FLAG_SHOW_PARTICLES, w.getFlags());
        assertEquals(PacketType.Play.Server.ENTITY_EFFECT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 7);
        raw.getEffectTypes().write(0, PotionEffectType.REGENERATION);
        raw.getIntegers().write(1, 0);
        raw.getIntegers().write(2, 100);
        raw.getBytes().write(0, (byte) 0x06);

        WrappedClientboundUpdateMobEffectPacket w = new WrappedClientboundUpdateMobEffectPacket(raw);
        assertEquals(7, w.getEntityId());
        assertEquals(PotionEffectType.REGENERATION, w.getEffectType());
        assertEquals(0, w.getAmplifier());
        assertEquals(100, w.getDuration());
        assertEquals((byte) 0x06, w.getFlags());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundUpdateMobEffectPacket w = new WrappedClientboundUpdateMobEffectPacket();
        w.setDuration(100);

        new WrappedClientboundUpdateMobEffectPacket(w.getHandle()).setDuration(300);

        assertEquals(300, w.getDuration());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateMobEffectPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
