package com.comphenix.tinyprotocol;

import net.minecraft.server.v1_7_R1.PacketPlayInChat;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.base.Function;

public class ExamplePlugin extends JavaPlugin {
	private Function<Object, String> CHAT_MESSAGE = TinyProtocol.getFieldAccessor(
			PacketPlayInChat.class, String.class, 0);

	@Override
	public void onEnable() {
		new TinyProtocol(this) {
			@Override
			public Object onPacketInAsync(Player sender, Object packet) {
				// Cancel chat packets
				if (packet instanceof PacketPlayInChat) {
					if (CHAT_MESSAGE.apply(packet).contains("dirty"))
						return null;
				}
				return super.onPacketInAsync(sender, packet);
			}
		};
	}
}
