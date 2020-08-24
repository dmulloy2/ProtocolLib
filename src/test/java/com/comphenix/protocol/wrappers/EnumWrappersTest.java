package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R2.EnumChatVisibility;
import net.minecraft.server.v1_16_R2.EnumDifficulty;
import net.minecraft.server.v1_16_R2.EnumGamemode;
import net.minecraft.server.v1_16_R2.EnumProtocol;
import net.minecraft.server.v1_16_R2.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_16_R2.PacketPlayInUseEntity.EnumEntityUseAction;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EnumWrappersTest {
	private static class EnumClass {
		public EnumProtocol protocol;
		public EnumClientCommand command;
		public EnumChatVisibility visibility;
		public EnumDifficulty difficulty;
		public EnumEntityUseAction action;
		public EnumGamemode mode;
	}
	
	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}
	
	@Test
	public void testEnum() {
		EnumClass obj = new EnumClass();
		obj.protocol = EnumProtocol.LOGIN;
		obj.command = EnumClientCommand.PERFORM_RESPAWN;
		obj.visibility = EnumChatVisibility.FULL;
		obj.difficulty = EnumDifficulty.PEACEFUL;
		obj.action = EnumEntityUseAction.INTERACT;
		obj.mode = EnumGamemode.CREATIVE;
		
		assertEquals(obj.protocol, roundtrip(obj, "protocol", EnumWrappers.getProtocolConverter()) );
		assertEquals(obj.command, roundtrip(obj, "command", EnumWrappers.getClientCommandConverter()) );
		assertEquals(obj.visibility, roundtrip(obj, "visibility", EnumWrappers.getChatVisibilityConverter()) );
		assertEquals(obj.difficulty, roundtrip(obj, "difficulty", EnumWrappers.getDifficultyConverter()) );
		assertEquals(obj.action, roundtrip(obj, "action", EnumWrappers.getEntityUseActionConverter()) );
		assertEquals(obj.mode, roundtrip(obj, "mode", EnumWrappers.getGameModeConverter()) );
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T roundtrip(Object target, String fieldName, EquivalentConverter<T> converter) {
		FieldAccessor accessor = Accessors.getFieldAccessor(target.getClass(), fieldName, true);
		
		return (T) converter.getGeneric(
			converter.getSpecific(accessor.get(target))
		);
	}

	private static final Set<String> KNOWN_INVALID = Sets.newHashSet("Particle");

	@Test
	public void testValidity() {
		assertEquals(EnumWrappers.INVALID, KNOWN_INVALID);
	}
}
