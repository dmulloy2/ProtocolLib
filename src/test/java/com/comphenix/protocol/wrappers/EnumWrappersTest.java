package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.core.EnumDirection;
import net.minecraft.network.EnumProtocol;
import net.minecraft.network.protocol.game.PacketPlayInClientCommand.EnumClientCommand;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumHand;
import net.minecraft.world.entity.player.EnumChatVisibility;
import net.minecraft.world.level.EnumGamemode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EnumWrappersTest {

	private static final Set<String> KNOWN_INVALID = Sets.newHashSet(
			"Particle", "WorldBorderAction", "CombatEventType", "TitleAction", "ChatType"
	);

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
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
		obj.direction = EnumDirection.f;

		assertEquals(obj.protocol, this.roundtrip(obj, "protocol", EnumWrappers.getProtocolConverter()));
		assertEquals(obj.command, this.roundtrip(obj, "command", EnumWrappers.getClientCommandConverter()));
		assertEquals(obj.visibility, this.roundtrip(obj, "visibility", EnumWrappers.getChatVisibilityConverter()));
		assertEquals(obj.difficulty, this.roundtrip(obj, "difficulty", EnumWrappers.getDifficultyConverter()));
		assertEquals(obj.hand, this.roundtrip(obj, "hand", EnumWrappers.getHandConverter()));
		// assertEquals(obj.action, roundtrip(obj, "action", EnumWrappers.getEntityUseActionConverter()) );
		assertEquals(obj.mode, this.roundtrip(obj, "mode", EnumWrappers.getGameModeConverter()));
		assertEquals(obj.direction, this.roundtrip(obj, "direction", EnumWrappers.getDirectionConverter()));
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T roundtrip(Object target, String fieldName, EquivalentConverter<T> converter) {
		FieldAccessor accessor = Accessors.getFieldAccessor(target.getClass(), fieldName, true);

		return (T) converter.getGeneric(
				converter.getSpecific(accessor.get(target))
		);
	}

	@Test
	public void testValidity() {
		assertEquals(EnumWrappers.INVALID, KNOWN_INVALID);
	}

	private static class EnumClass {

		public EnumProtocol protocol;
		public EnumClientCommand command;
		public EnumChatVisibility visibility;
		public EnumDifficulty difficulty;
		public EnumHand hand;
		// public EnumEntityUseAction action; // moved to PacketPlayInUseEntity but is private
		public EnumGamemode mode;
		public EnumDirection direction;
	}
}
