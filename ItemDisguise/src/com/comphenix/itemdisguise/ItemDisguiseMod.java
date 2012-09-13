package com.comphenix.itemdisguise;

import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class ItemDisguiseMod extends JavaPlugin {
	
	private ProtocolManager protocolManager;
	private Logger logger;
	
	private HideEnchantmentsListener enchantmentsListener;
	
	@Override
	public void onEnable() {
		
		logger = getLoggerSafely();
		protocolManager = ProtocolLibrary.getProtocolManager();
		
		enchantmentsListener = new HideEnchantmentsListener(getServer(), logger);
		enchantmentsListener.addListener(protocolManager, this);
	}

	@Override
	public void onDisable() {
		enchantmentsListener.removeListener(protocolManager, this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
		
			Player player = (Player) sender;
			
			if (label.equalsIgnoreCase("explosion")) {
				PacketContainer fakeExplosion = protocolManager.createPacket(0x3C);
				
				// Set the coordinates
				try {
					fakeExplosion.getSpecificModifier(double.class).
					    write(0, player.getLocation().getX()).
					    write(1, player.getLocation().getY()).
					    write(2, player.getLocation().getZ());
					fakeExplosion.getSpecificModifier(float.class).
						write(0, 3.0F);

					protocolManager.sendServerPacket(player, fakeExplosion);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	// Get the Bukkit logger first, before we try to create our own
	private Logger getLoggerSafely() {
		
		Logger log = null;
	
		try {
			log = getLogger();
		} catch (Throwable e) {
			// We'll handle it
		}
		
		if (log == null)
			log = Logger.getLogger("Minecraft");
		return log;
	}
}
