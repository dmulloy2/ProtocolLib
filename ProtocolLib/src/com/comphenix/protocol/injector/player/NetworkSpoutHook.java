package com.comphenix.protocol.injector.player;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import net.minecraft.server.Packet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.ListenerInvoker;

public class NetworkSpoutHook extends PlayerInjector {
	
	public NetworkSpoutHook(Logger logger, Player player, ListenerInvoker invoker) throws IllegalAccessException {
		super(logger, player, invoker);
	}
	
	@Override
	protected boolean hasListener(int packetID) {
		return false;
	}
	
	@Override
	public boolean canInject() {
		return getSpout() != null;
	}
	
	private Plugin getSpout() {
		// Spout must be loaded
		try {
			return Bukkit.getServer().getPluginManager().getPlugin("Spout");
		} catch (Throwable e) {
			return null;
		}
	}
	
	@Override
	public void injectManager() {
	
	}
	
	@Override
	public void sendServerPacket(Packet packet, boolean filtered) throws InvocationTargetException {

	}

	@Override
	public void cleanupAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void checkListener(PacketListener listener) {
		// We support everything Spout does
	}
}
