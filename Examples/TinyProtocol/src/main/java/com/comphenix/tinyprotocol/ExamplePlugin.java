package com.comphenix.tinyprotocol;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.tinyprotocol.TinyProtocol.FieldAccessor;

public class ExamplePlugin extends JavaPlugin {
	// Chat packets
	private FieldAccessor<String> CHAT_MESSAGE = TinyProtocol.getField(
			"PacketPlayInChat", String.class, 0);
	
	// Explosion packet
	private Class<?> particleClass = TinyProtocol.getMinecraftClass("PacketPlayOutWorldParticles");
	private FieldAccessor<String> particleName = TinyProtocol.getField(particleClass, String.class, 0);
	private FieldAccessor<Float> particleX = TinyProtocol.getField(particleClass, float.class, 0);
	private FieldAccessor<Float> particleY = TinyProtocol.getField(particleClass, float.class, 1);
	private FieldAccessor<Float> particleZ = TinyProtocol.getField(particleClass, float.class, 2);
	private FieldAccessor<Integer> particleCount = TinyProtocol.getField(particleClass, int.class, 0);
	
	private TinyProtocol protocol;
	
	@Override
	public void onEnable() {
		protocol = new TinyProtocol(this) {
			@Override
			public Object onPacketInAsync(Player sender, Object packet) {
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
				return super.onPacketInAsync(sender, packet);
			}
		};
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
