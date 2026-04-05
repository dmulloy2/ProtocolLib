package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRemoveMobEffectPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundRemoveMobEffectPacket w = new WrappedClientboundRemoveMobEffectPacket(3, PotionEffectType.STRENGTH);

        assertEquals(PacketType.Play.Server.REMOVE_ENTITY_EFFECT, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(PotionEffectType.STRENGTH, w.getEffectType());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRemoveMobEffectPacket w = new WrappedClientboundRemoveMobEffectPacket();

        assertEquals(PacketType.Play.Server.REMOVE_ENTITY_EFFECT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundRemoveMobEffectPacket source = new WrappedClientboundRemoveMobEffectPacket(3, PotionEffectType.STRENGTH);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundRemoveMobEffectPacket wrapper = new WrappedClientboundRemoveMobEffectPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(PotionEffectType.STRENGTH, wrapper.getEffectType());

        wrapper.setEntityId(9);
        wrapper.setEffectType(PotionEffectType.SLOWNESS);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, wrapper.getEffectType());

        assertEquals(9, source.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, source.getEffectType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRemoveMobEffectPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
