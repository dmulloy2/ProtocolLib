package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;
import net.minecraft.server.v1_8_R3.EntityHuman.EnumChatVisibility;
import net.minecraft.server.v1_8_R3.EnumDifficulty;
import net.minecraft.server.v1_8_R3.EnumProtocol;
import net.minecraft.server.v1_8_R3.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity.EnumEntityUseAction;
import net.minecraft.server.v1_8_R3.WorldSettings.EnumGamemode;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

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
			accessor.getField().getType(), 
			converter.getSpecific(accessor.get(target))
		);
	}
}
