package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrapperGameClientboundRemoveEntityEffectTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrapperGameClientboundRemoveEntityEffect w = new WrapperGameClientboundRemoveEntityEffect();
        w.setEntityId(8);
        w.setEffectType(PotionEffectType.SPEED);
        assertEquals(8, w.getEntityId());
        assertEquals(PotionEffectType.SPEED, w.getEffectType());
        assertEquals(PacketType.Play.Server.REMOVE_ENTITY_EFFECT, w.getHandle().getType());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer raw = new PacketContainer(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        raw.getModifier().writeDefaults();
        raw.getIntegers().write(0, 15);
        raw.getEffectTypes().write(0, PotionEffectType.SLOWNESS);

        WrapperGameClientboundRemoveEntityEffect w = new WrapperGameClientboundRemoveEntityEffect(raw);
        assertEquals(15, w.getEntityId());
        assertEquals(PotionEffectType.SLOWNESS, w.getEffectType());
    }

    @Test
    void testModifyExistingPacket() {
        WrapperGameClientboundRemoveEntityEffect w = new WrapperGameClientboundRemoveEntityEffect();
        w.setEntityId(1);

        new WrapperGameClientboundRemoveEntityEffect(w.getHandle()).setEntityId(50);

        assertEquals(50, w.getEntityId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrapperGameClientboundRemoveEntityEffect(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
