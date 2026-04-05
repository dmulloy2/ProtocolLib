package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedExplosionParticleInfo;
import com.comphenix.protocol.wrappers.WrappedParticle;
import com.comphenix.protocol.wrappers.WrappedWeightedList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Sound;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundExplodePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundExplodePacket w = new WrappedClientboundExplodePacket(new Vector(1.0, 2.0, 3.0), 0.5f, 5, Optional.empty(), null, Sound.BLOCK_STONE_BREAK, new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 1.0f, 1.0f), 1))));

        assertEquals(PacketType.Play.Server.EXPLOSION, w.getHandle().getType());

        assertEquals(new Vector(1.0, 2.0, 3.0), w.getCenter());
        assertEquals(0.5f, w.getRadius(), 1e-4f);
        assertEquals(5, w.getBlockCount());
        assertEquals(Optional.empty(), w.getPlayerKnockback());
        assertEquals(null, w.getExplosionParticle());
        assertEquals(Sound.BLOCK_STONE_BREAK, w.getExplosionSound());
        assertEquals(new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 1.0f, 1.0f), 1))), w.getBlockParticles());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundExplodePacket w = new WrappedClientboundExplodePacket();

        assertEquals(PacketType.Play.Server.EXPLOSION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundExplodePacket source = new WrappedClientboundExplodePacket(new Vector(1.0, 2.0, 3.0), 0.5f, 5, Optional.empty(), null, Sound.BLOCK_STONE_BREAK, new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 1.0f, 1.0f), 1))));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundExplodePacket wrapper = new WrappedClientboundExplodePacket(container);

        assertEquals(new Vector(1.0, 2.0, 3.0), wrapper.getCenter());
        assertEquals(0.5f, wrapper.getRadius(), 1e-4f);
        assertEquals(5, wrapper.getBlockCount());
        assertEquals(Optional.empty(), wrapper.getPlayerKnockback());
        assertEquals(null, wrapper.getExplosionParticle());
        assertEquals(Sound.BLOCK_STONE_BREAK, wrapper.getExplosionSound());
        assertEquals(new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 1.0f, 1.0f), 1))), wrapper.getBlockParticles());

        wrapper.setCenter(new Vector(10.0, 20.0, 30.0));
        wrapper.setRadius(-3.0f);
        wrapper.setBlockCount(0);
        wrapper.setPlayerKnockback(Optional.empty());
        wrapper.setExplosionParticle(null);
        wrapper.setExplosionSound(Sound.ENTITY_ZOMBIE_HURT);
        wrapper.setBlockParticles(new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 2.0f, 0.25f), 3))));

        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getCenter());
        assertEquals(-3.0f, wrapper.getRadius(), 1e-4f);
        assertEquals(0, wrapper.getBlockCount());
        assertEquals(Optional.empty(), wrapper.getPlayerKnockback());
        assertEquals(null, wrapper.getExplosionParticle());
        assertEquals(Sound.ENTITY_ZOMBIE_HURT, wrapper.getExplosionSound());
        assertEquals(new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 2.0f, 0.25f), 3))), wrapper.getBlockParticles());

        assertEquals(new Vector(10.0, 20.0, 30.0), source.getCenter());
        assertEquals(-3.0f, source.getRadius(), 1e-4f);
        assertEquals(0, source.getBlockCount());
        assertEquals(Optional.empty(), source.getPlayerKnockback());
        assertEquals(null, source.getExplosionParticle());
        assertEquals(Sound.ENTITY_ZOMBIE_HURT, source.getExplosionSound());
        assertEquals(new WrappedWeightedList<>(List.of(new WrappedWeightedList.Entry<>(new WrappedExplosionParticleInfo(null, 2.0f, 0.25f), 3))), source.getBlockParticles());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundExplodePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
