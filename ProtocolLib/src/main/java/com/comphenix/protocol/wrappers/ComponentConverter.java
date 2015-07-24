/**
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2015 dmulloy2
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package com.comphenix.protocol.wrappers;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

/**
 * Utility class for converting between the BungeeCord Chat API and ProtocolLib's wrapper
 * <p>
 * Note: The BungeeCord Chat API is not included in CraftBukkit.
 * @author dmulloy2
 */

public final class ComponentConverter {

	private ComponentConverter() {
	}

	/**
	 * Converts a {@link WrappedChatComponent} into an array of {@link BaseComponent}s
	 * @param wrapper ProtocolLib wrapper
	 * @return BaseComponent array
	 */
	public static BaseComponent[] fromWrapper(WrappedChatComponent wrapper) {
		return ComponentSerializer.parse(wrapper.getJson());
	}

	/**
	 * Converts an array of {@link BaseComponent}s into a ProtocolLib wrapper
	 * @param components BaseComponent array
	 * @return ProtocolLib wrapper
	 */
	public static WrappedChatComponent fromBaseComponent(BaseComponent... components) {
		return WrappedChatComponent.fromJson(ComponentSerializer.toString(components));
	}
}