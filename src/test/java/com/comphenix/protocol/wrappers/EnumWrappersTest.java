package com.comphenix.protocol.wrappers;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;

import org.junit.BeforeClass;
import org.junit.Test;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

import java.util.Set;

import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.level.EnumGamemode;

public class EnumWrappersTest {
	private static class EnumClass {
		public EnumProtocol protocol;
		public EnumClientCommand command;
		public EnumChatVisibility visibility;
		public EnumDifficulty difficulty;
		public EnumHand hand;
		// public EnumEntityUseAction action; // moved to PacketPlayInUseEntity but is private
		public EnumGamemode mode;
	}

	@BeforeClass
	public static void initializeBukkit() {
		BukkitInitialization.initializePackage();
	}

	@Test
	public void testEnum() {
		EnumClass obj = new EnumClass();
		obj.protocol = EnumProtocol.a;
		obj.command = EnumClientCommand.b;
		obj.visibility = EnumChatVisibility.c;
		obj.difficulty = EnumDifficulty.d;
		obj.hand = EnumHand.b;
		// obj.action = EnumEntityUseAction.INTERACT;
		obj.mode = EnumGamemode.e;

		assertEquals(obj.protocol, roundtrip(obj, "protocol", EnumWrappers.getProtocolConverter()) );
		assertEquals(obj.command, roundtrip(obj, "command", EnumWrappers.getClientCommandConverter()) );
		assertEquals(obj.visibility, roundtrip(obj, "visibility", EnumWrappers.getChatVisibilityConverter()) );
		assertEquals(obj.difficulty, roundtrip(obj, "difficulty", EnumWrappers.getDifficultyConverter()) );
		assertEquals(obj.hand, roundtrip(obj, "hand", EnumWrappers.getHandConverter()) );
		// assertEquals(obj.action, roundtrip(obj, "action", EnumWrappers.getEntityUseActionConverter()) );
		assertEquals(obj.mode, roundtrip(obj, "mode", EnumWrappers.getGameModeConverter()) );
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T roundtrip(Object target, String fieldName, EquivalentConverter<T> converter) {
		FieldAccessor accessor = Accessors.getFieldAccessor(target.getClass(), fieldName, true);

		return (T) converter.getGeneric(
			converter.getSpecific(accessor.get(target))
		);
	}

	private static final Set<String> KNOWN_INVALID = Sets.newHashSet(
			"Particle", "WorldBorderAction", "CombatEventType", "TitleAction"
	);

	@Test
	public void testValidity() {
		assertEquals(EnumWrappers.INVALID, KNOWN_INVALID);
	}
}
