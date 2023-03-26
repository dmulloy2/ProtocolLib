package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.wrappers.Either.Left;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BukkitConvertersTest {

	@BeforeAll
	public static void beforeClass() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void testItemStacks() {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD, 16);
		item.addEnchantment(Enchantment.DAMAGE_ALL, 4);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Diamond Sword");
		item.setItemMeta(meta);

		EquivalentConverter<ItemStack> converter = BukkitConverters.getItemStackConverter();
		Object nmsStack = converter.getGeneric(item);
		ItemStack back = converter.getSpecific(nmsStack);

		assertEquals(item.getType(), back.getType());
		assertEquals(item.getDurability(), back.getDurability());
		assertEquals(item.hasItemMeta(), back.hasItemMeta());
		assertTrue(Bukkit.getItemFactory().equals(item.getItemMeta(), back.getItemMeta()));
	}

    @Test
    public void testEither() {
        Either<String, String> test = new Left<>("bla");

        EquivalentConverter<Either<String, String>> converter = BukkitConverters.getEitherConverter(
            Converters.passthrough(String.class), Converters.passthrough(String.class)
        );

        com.mojang.datafixers.util.Either<String, String> nmsEither = (com.mojang.datafixers.util.Either<String, String>) converter.getGeneric(test);
        Either<String, String> wrapped = converter.getSpecific(nmsEither);

        assertEquals(wrapped.left(), nmsEither.left());
        assertEquals(wrapped.right(), nmsEither.right());
    }

	@Test
	public void testPacketContainerConverter() {
		for (PacketType type : PacketType.values()) {
			if(!type.isSupported()) {
				continue;
			}
			PacketContainer container = new PacketContainer(type);
			Object generic = BukkitConverters.getPacketContainerConverter().getGeneric(container);
			Object specific = BukkitConverters.getPacketContainerConverter().getSpecific(generic);
			assertTrue(EqualsBuilder.reflectionEquals(container, specific)); // PacketContainer does not properly implement equals(.)
		}
	}
}
