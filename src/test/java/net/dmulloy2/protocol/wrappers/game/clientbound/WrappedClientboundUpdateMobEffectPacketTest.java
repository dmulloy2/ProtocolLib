package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
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

        assertEquals(PacketType.Play.Server.ENTITY_EFFECT, w.getHandle().getType());

        ClientboundUpdateMobEffectPacket p = (ClientboundUpdateMobEffectPacket) w.getHandle().getHandle();

        assertEquals(4, p.getEntityId());
        assertEquals(1, p.getEffectAmplifier());
        assertEquals(200, p.getEffectDurationTicks());
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

        WrappedClientboundUpdateMobEffectPacket wrapper = new WrappedClientboundUpdateMobEffectPacket(raw);

        assertEquals(7, wrapper.getEntityId());
        assertEquals(PotionEffectType.REGENERATION, wrapper.getEffectType());
        assertEquals(0, wrapper.getAmplifier());
        assertEquals(100, wrapper.getDuration());
        assertEquals((byte) 0x06, wrapper.getFlags());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.ENTITY_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 5);
        raw.getEffectTypes().write(0, PotionEffectType.SPEED);
        raw.getIntegers().write(1, 0);
        raw.getIntegers().write(2, 100);

        WrappedClientboundUpdateMobEffectPacket wrapper = new WrappedClientboundUpdateMobEffectPacket(raw);
        wrapper.setDuration(300);

        assertEquals(5, wrapper.getEntityId());
        assertEquals(PotionEffectType.SPEED, wrapper.getEffectType());
        assertEquals(300, wrapper.getDuration());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateMobEffectPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
