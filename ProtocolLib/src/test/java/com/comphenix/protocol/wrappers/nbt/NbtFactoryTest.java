package com.comphenix.protocol.wrappers.nbt;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.utility.MinecraftReflection;

public class NbtFactoryTest {
	@BeforeClass
	public static void initializeBukkit() {
		// Initialize reflection
		MinecraftReflection.setMinecraftPackage("net.minecraft.server.v1_4_6", "org.bukkit.craftbukkit.v1_4_6");
	}
	
	@Test
	public void testFromStream() {
		NbtCompound compound = NbtCompound.fromName("tag");
		
		compound.put("name", "Test Testerson");
		compound.put("age", 42);
		
		compound.put(NbtFactory.ofList("nicknames", "a", "b", "c"));

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput test = new DataOutputStream(buffer);
		compound.write(test);

		ByteArrayInputStream source = new ByteArrayInputStream(buffer.toByteArray());
		DataInput input = new DataInputStream(source);
		
		NbtCompound cloned = (NbtCompound) NbtFactory.fromStream(input);
		
		assertEquals(compound.getString("name"), cloned.getString("name"));
		assertEquals(compound.getInteger("age"), cloned.getInteger("age"));
		assertEquals(compound.getList("nicknames"), cloned.getList("nicknames"));
	}
}
