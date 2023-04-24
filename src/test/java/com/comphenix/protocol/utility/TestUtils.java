package com.comphenix.protocol.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.comphenix.protocol.reflect.accessors.Accessors;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.comphenix.protocol.wrappers.WrappedProfilePublicKey;
import com.comphenix.protocol.wrappers.WrappedRemoteChatSessionData;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class TestUtils {

	public static void assertItemCollectionsEqual(List<ItemStack> first, List<ItemStack> second) {
		assertEquals(first.size(), second.size());
		for (int i = 0; i < first.size(); i++) {
			assertItemsEqual(first.get(i), second.get(i));
		}
	}

	public static void assertItemsEqual(ItemStack first, ItemStack second) {
		if (first == null) {
			assertNull(second);
		} else {
			assertNotNull(first);

			// The legacy check in ItemStack#isSimilar causes a null pointer
			assertEquals(first.getType(), second.getType());
			assertEquals(first.getDurability(), second.getDurability());
			assertEquals(first.hasItemMeta(), second.hasItemMeta());
			if (first.hasItemMeta()) {
				assertTrue(Bukkit.getItemFactory().equals(first.getItemMeta(), second.getItemMeta()));
			}
		}
	}

	public static boolean equivalentItem(ItemStack first, ItemStack second) {
		if (first == null) {
			return second == null;
		} else if (second == null) {
			return false;
		} else {
			return first.getType().equals(second.getType());
		}
	}

	public static void setFinalField(Object obj, Field field, Object newValue) {
		Accessors.getFieldAccessor(field).set(obj, newValue);
	}

	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		return keyPairGenerator.generateKeyPair();
	}

	public static WrappedRemoteChatSessionData creteDummyRemoteChatSessionData() throws Exception {
		byte[] signature = new byte[256];
		new Random().nextBytes(signature);

		return new WrappedRemoteChatSessionData(UUID.randomUUID(), new WrappedProfilePublicKey.WrappedProfileKeyData(Instant.now(), TestUtils.generateKeyPair().getPublic(), signature));
	}
}
