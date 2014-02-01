package com.comphenix.tinyprotocol;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.tinyprotocol.Reflection.FieldAccessor;

public class ExamplePlugin extends JavaPlugin {
	// Chat packets
	private FieldAccessor<String> CHAT_MESSAGE = Reflection.getField(
			"{nms}.PacketPlayInChat", String.class, 0);
	
	// Explosion packet
	private Class<?> particleClass = Reflection.getClass("{nms}.PacketPlayOutWorldParticles");
	private FieldAccessor<String> particleName = Reflection.getField(particleClass, String.class, 0);
	private FieldAccessor<Float> particleX = Reflection.getField(particleClass, float.class, 0);
	private FieldAccessor<Float> particleY = Reflection.getField(particleClass, float.class, 1);
	private FieldAccessor<Float> particleZ = Reflection.getField(particleClass, float.class, 2);
	private FieldAccessor<Integer> particleCount = Reflection.getField(particleClass, int.class, 0);
	
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
