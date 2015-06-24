package com.comphenix.tinyprotocol;

import io.netty.channel.Channel;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.tinyprotocol.Reflection.ConstructorInvoker;
import com.comphenix.tinyprotocol.Reflection.FieldAccessor;

/**
 * Represents an example plugin utilizing TinyProtocol
 */
public class ExamplePlugin extends JavaPlugin {
	// Chat packets
	private FieldAccessor<String> CHAT_MESSAGE = Reflection.getField("{nms}.PacketPlayInChat", String.class, 0);

	// Explosion packet
	private Class<?> particleClass = Reflection.getClass("{nms}.PacketPlayOutWorldParticles");
	private FieldAccessor<String> particleName = Reflection.getField(particleClass, String.class, 0);
	private FieldAccessor<Float> particleX = Reflection.getField(particleClass, float.class, 0);
	private FieldAccessor<Float> particleY = Reflection.getField(particleClass, float.class, 1);
	private FieldAccessor<Float> particleZ = Reflection.getField(particleClass, float.class, 2);
	private FieldAccessor<Integer> particleCount = Reflection.getField(particleClass, int.class, 0);

	// Server info packet
	private Class<?> serverInfoClass = Reflection.getClass("{nms}.PacketStatusOutServerInfo");
	private Class<Object> serverPingClass = Reflection.getUntypedClass("{nms}.ServerPing");
	private Class<Object> playerSampleClass = Reflection.getUntypedClass("{nms}.ServerPingPlayerSample");
	private FieldAccessor<Object> serverPing = Reflection.getField(serverInfoClass, serverPingClass, 0);
	private FieldAccessor<Object> playerSample = Reflection.getField(serverPingClass, playerSampleClass, 0);
	private ConstructorInvoker playerSampleInvoker = Reflection.getConstructor(playerSampleClass, int.class, int.class);

	private TinyProtocol protocol;

	@Override
	public void onEnable() {
		protocol = new TinyProtocol(this) {

			@Override
			public Object onPacketInAsync(Player sender, Channel channel, Object packet) {
				// Cancel chat packets
				if (CHAT_MESSAGE.hasField(packet)) {
					if (CHAT_MESSAGE.get(packet).contains("dirty")) {
						sendExplosion(sender);
						return null;
					}
				}

				if (particleName.hasField(packet)) {
					System.out.println("Sending particle field:" + packet);
				}

				return super.onPacketInAsync(sender, channel, packet);
			}

			@Override
			public Object onPacketOutAsync(Player reciever, Channel channel, Object packet) {
				if (serverInfoClass.isInstance(packet)) {
					Object ping = serverPing.get(packet);
					playerSample.set(ping, playerSampleInvoker.invoke(1000, 0));

					// Which is equivalent to:
					// serverPing.get(packet).setPlayerSample(new ServerPingPlayerSample(1000, 0));
					return packet;
				}

				return super.onPacketOutAsync(reciever, channel, packet);
			}

		};
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			// Toggle injection
			if (protocol.hasInjected(player)) {
				protocol.uninjectPlayer(player);
				sender.sendMessage(ChatColor.YELLOW + "Player " + player + " has been uninjected.");
			} else {
				protocol.injectPlayer(player);
				sender.sendMessage(ChatColor.DARK_GREEN + "Player " + player + " has been injected.");
			}

			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "Can only be invoked by a player.");
		}

		return false;
	}

	private void sendExplosion(Player player) {
		try {
			// Only visible for the client
			Object explosionPacket = particleClass.newInstance();
			Location loc = player.getLocation();
			particleName.set(explosionPacket, "hugeexplosion");
			particleX.set(explosionPacket, (float) loc.getX());
			particleY.set(explosionPacket, (float) loc.getY());
			particleZ.set(explosionPacket, (float) loc.getZ());
			particleCount.set(explosionPacket, 1);

			// Send the packet to the player
			protocol.sendPacket(player, explosionPacket);
		} catch (Exception e) {
			throw new RuntimeException("Cannot send packet.", e);
		}
	}
}