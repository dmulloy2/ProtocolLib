/**
 * ProtocolLib - Bukkit server library that allows access to the Minecraft protocol. Copyright (C) 2015 dmulloy2
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author dmulloy2
 */
public class PlayerInfoDataTest {

	@BeforeAll
	public static void initializeBukkit() {
		BukkitInitialization.initializeAll();
	}

	@Test
	public void test() {
		WrappedGameProfile profile = new WrappedGameProfile(UUID.randomUUID(), "Name");
		WrappedChatComponent displayName = WrappedChatComponent.fromText("Name's Name");

		PlayerInfoData data = new PlayerInfoData(profile, 42, NativeGameMode.CREATIVE, displayName);
		Object generic = PlayerInfoData.getConverter().getGeneric(data);
		PlayerInfoData back = PlayerInfoData.getConverter().getSpecific(generic);

		assertEquals(data, back);
	}
}
