package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
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
    void testCreate() {
        WrappedClientboundRemoveMobEffectPacket w = new WrappedClientboundRemoveMobEffectPacket();
        w.setEntityId(8);
        w.setEffectType(PotionEffectType.SPEED);

        assertEquals(PacketType.Play.Server.REMOVE_ENTITY_EFFECT, w.getHandle().getType());

        ClientboundRemoveMobEffectPacket p = (ClientboundRemoveMobEffectPacket) w.getHandle().getHandle();

        assertEquals(8, p.entityId());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 15);
        container.getEffectTypes().write(0, PotionEffectType.SLOWNESS);

        WrappedClientboundRemoveMobEffectPacket wrapper = new WrappedClientboundRemoveMobEffectPacket(container);

        assertEquals(15, wrapper.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, wrapper.getEffectType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 15);
        container.getEffectTypes().write(0, PotionEffectType.SLOWNESS);

        WrappedClientboundRemoveMobEffectPacket wrapper = new WrappedClientboundRemoveMobEffectPacket(container);
        wrapper.setEntityId(50);

        assertEquals(50, wrapper.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, wrapper.getEffectType());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRemoveMobEffectPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
