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
    void testAllArgsCreate() {
        WrappedClientboundUpdateMobEffectPacket w = new WrappedClientboundUpdateMobEffectPacket(3, PotionEffectType.STRENGTH, 5, 3, (byte) 1);

        assertEquals(PacketType.Play.Server.ENTITY_EFFECT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(PotionEffectType.STRENGTH, w.getEffectType());
        assertEquals(5, w.getAmplifier());
        assertEquals(3, w.getDuration());
        assertEquals((byte) 1, w.getFlags());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateMobEffectPacket w = new WrappedClientboundUpdateMobEffectPacket();

        assertEquals(PacketType.Play.Server.ENTITY_EFFECT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundUpdateMobEffectPacket p;
        WrappedClientboundUpdateMobEffectPacket source = new WrappedClientboundUpdateMobEffectPacket(3, PotionEffectType.STRENGTH, 5, 3, (byte) 1);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundUpdateMobEffectPacket wrapper = new WrappedClientboundUpdateMobEffectPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(PotionEffectType.STRENGTH, wrapper.getEffectType());
        assertEquals(5, wrapper.getAmplifier());
        assertEquals(3, wrapper.getDuration());
        assertEquals((byte) 1, wrapper.getFlags());

        wrapper.setEntityId(9);
        wrapper.setEffectType(PotionEffectType.SLOWNESS);
        wrapper.setAmplifier(0);
        wrapper.setDuration(42);
        wrapper.setFlags((byte) -1);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, wrapper.getEffectType());
        assertEquals(0, wrapper.getAmplifier());
        assertEquals(42, wrapper.getDuration());
        assertEquals((byte) -1, wrapper.getFlags());

        assertEquals(9, source.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, source.getEffectType());
        assertEquals(0, source.getAmplifier());
        assertEquals(42, source.getDuration());
        assertEquals((byte) -1, source.getFlags());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateMobEffectPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
