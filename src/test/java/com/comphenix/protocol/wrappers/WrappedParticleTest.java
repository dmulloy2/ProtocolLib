package com.comphenix.protocol.wrappers;

import static com.comphenix.protocol.utility.TestUtils.assertItemsEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedParticleTest {

	@BeforeAll
	public static void beforeClass() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testBlockData() {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);

		WrappedParticle before = WrappedParticle.create(Particle.BLOCK_CRACK,
				WrappedBlockData.createData(Material.LAPIS_BLOCK));
		packet.getNewParticles().write(0, before);

		WrappedParticle after = packet.getNewParticles().read(0);
		assertEquals(before.getParticle(), after.getParticle());
		assertEquals(before.getData(), after.getData());
	}

	@Test
	public void testItemStacks() {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
		WrappedParticle before = WrappedParticle.create(Particle.ITEM_CRACK, new ItemStack(Material.FLINT_AND_STEEL));
		packet.getNewParticles().write(0, before);

		WrappedParticle after = packet.getNewParticles().read(0);
		assertEquals(before.getParticle(), after.getParticle());
		assertItemsEqual((ItemStack) before.getData(), (ItemStack) after.getData());
	}

	@Test
	public void testRedstone() {
		PacketContainer packet = new PacketContainer(PacketType.Play.Server.WORLD_PARTICLES);
		WrappedParticle before = WrappedParticle.create(Particle.REDSTONE, new DustOptions(Color.BLUE, 1));
		packet.getNewParticles().write(0, before);

		WrappedParticle after = packet.getNewParticles().read(0);
		assertEquals(before.getParticle(), after.getParticle());

		DustOptions beforeDust = (DustOptions) before.getData();
		DustOptions afterDust = (DustOptions) after.getData();
		assertEquals(beforeDust.getColor(), afterDust.getColor());
		assertEquals(beforeDust.getSize(), afterDust.getSize(), 0);
	}
}
