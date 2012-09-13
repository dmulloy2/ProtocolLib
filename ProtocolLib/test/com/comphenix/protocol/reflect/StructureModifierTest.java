package com.comphenix.protocol.reflect;

import static org.junit.Assert.*;

import net.minecraft.server.Packet103SetSlot;

import org.junit.Test;

import com.avaje.ebeaninternal.server.cluster.Packet;
import com.comphenix.protocol.reflect.StructureModifier;

public class StructureModifierTest {

	@Test
	public void test() throws IllegalAccessException {

		Packet103SetSlot move = new Packet103SetSlot();
		StructureModifier<Object> modifier = new StructureModifier<Object>(Packet103SetSlot.class, Packet.class);

		move.a = 1;
		int value = (Integer) modifier.withTarget(move).withType(int.class).read(0);
		
		assertEquals(1, value);
	}
}
