package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrappedParticleTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testBlockData() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);

        WrappedParticle before = WrappedParticle.create(Particle.BLOCK,
                WrappedBlockData.createData(Material.LAPIS_BLOCK));
        packet.getNewParticles().write(0, before);

        WrappedParticle after = packet.getNewParticles().read(0);
        assertEquals(before.getParticle(), after.getParticle());
        assertEquals(before.getData(), after.getData());
    }

    @Test
    public void testItemStacks() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
        WrappedParticle before = WrappedParticle.create(Particle.ITEM, new ItemStack(Material.FLINT_AND_STEEL));
        packet.getNewParticles().write(0, before);

        WrappedParticle after = packet.getNewParticles().read(0);
        assertEquals(before.getParticle(), after.getParticle());
        assertItemsEqual((ItemStack) before.getData(), (ItemStack) after.getData());
    }

    @Test
    public void testRedstone() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
        WrappedParticle before = WrappedParticle.create(Particle.DUST, new DustOptions(Color.BLUE, 1));
        packet.getNewParticles().write(0, before);

        WrappedParticle after = packet.getNewParticles().read(0);
        assertEquals(before.getParticle(), after.getParticle());

        DustOptions beforeDust = (DustOptions) before.getData();
        DustOptions afterDust = (DustOptions) after.getData();
        assertEquals(beforeDust.getColor(), afterDust.getColor());
        assertEquals(beforeDust.getSize(), afterDust.getSize(), 0);
    }

    @Test
    public void testDustColorTransition() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
        WrappedParticle before = WrappedParticle.create(Particle.DUST_COLOR_TRANSITION, new DustTransition(Color.BLUE, Color.RED, 1));
        packet.getNewParticles().write(0, before);

        WrappedParticle after = packet.getNewParticles().read(0);
        assertEquals(before.getParticle(), after.getParticle());

        Particle.DustTransition beforeDust = (Particle.DustTransition) before.getData();
        Particle.DustTransition afterDust = (Particle.DustTransition) after.getData();

        assertEquals(beforeDust.getColor(), afterDust.getColor());
        assertEquals(beforeDust.getToColor(), afterDust.getToColor());
        assertEquals(beforeDust.getSize(), afterDust.getSize(), 0);
    }
}
