package com.comphenix.protocol.reflect.accessors;

import static org.junit.Assert.*;

import org.junit.Test;

public class AccessorsTest {
	// --- Some classes we can use for testing ---
	private static class Entity {
		private int id;
		
		public Entity(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
		
		@SuppressWarnings("unused")
		private void setId(int value) {
			this.id = value;
		}
	}
	
	private static class Player extends Entity {
		private String name;

		public Player(int id, String name) {
			super(id);
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}
 	// --- Test classes ---
	
	@Test
	public void testField() {
		Player player = new Player(123, "ABC");
		
		Accessors.getFieldAccessor(player.getClass(), "id", true).set(player, 0);
		Accessors.getFieldAccessor(player.getClass(), "name", true).set(player, "MODIFIED");
		assertEquals(0, player.getId());
		assertEquals("MODIFIED", player.getName());
	}
	
	@Test
	public void testMethod() {
		Player player = new Player(123, "ABC");

		Accessors.getMethodAccessor(player.getClass(), "setId", int.class).invoke(player, 0);
		assertEquals(0, player.getId());
	}
}
