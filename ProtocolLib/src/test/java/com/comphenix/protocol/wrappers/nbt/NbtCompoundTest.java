package com.comphenix.protocol.wrappers.nbt;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.utility.MinecraftReflection;

public class NbtCompoundTest {
	@BeforeClass
	public static void setupBukkit() {
		MinecraftReflection.setMinecraftPackage("net.minecraft.server.v1_4_6", "org.bukkit.craftbukkit.v1_4_6");
	}
	
	@Test
	public void testCustomTags() {
		NbtCustomTag<Integer> test = new NbtCustomTag<Integer>("hello", 12);

		NbtCompound map = NbtCompound.fromName("test");
		map.put(test);
		
		// Note that the custom tag will be cloned
		assertEquals(12, map.getInteger("hello"));
	}
	
	/**
	 * Represents a custom NBT tag.
	 * 
	 * @author Kristian
	 *
	 * @param <TValue> - the value of the tag.
	 */
	public static class NbtCustomTag<TValue> implements NbtBase<TValue> {
		private String name;
		private TValue value;
		private NbtType type;
		
		public NbtCustomTag(String name, TValue value) {
			if (value == null)
				throw new IllegalArgumentException("Cannot create a custom tag from NULL.");
			this.value = value;
			this.name = name;
			this.type = NbtType.getTypeFromClass(value.getClass());
			
		}

		@Override
		public NbtType getType() {
			return type;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}

		@Override
		public TValue getValue() {
			return value;
		}

		@Override
		public void setValue(TValue newValue) {
			this.value = newValue;
		}

		@Override
		public NbtBase<TValue> deepClone() {
			return new NbtCustomTag<TValue>(name, value);
		}	
	}
}
