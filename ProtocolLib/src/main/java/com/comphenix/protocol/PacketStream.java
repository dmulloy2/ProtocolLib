package com.comphenix.protocol;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.events.PacketContainer;

/**
 * Represents a object capable of sending or receiving packets.
 * 
 * @author Kristian
 */
public interface PacketStream {
	
	/**
	 * Send a packet to the given player.
	 * @param reciever - the reciever.
	 * @param packet - packet to send.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	public void sendServerPacket(Player reciever, PacketContainer packet) 
			throws InvocationTargetException;

	/**
	 * Send a packet to the given player.
	 * @param reciever - the reciever.
	 * @param packet - packet to send.
	 * @param filters - whether or not to invoke any packet filters.
	 * @throws InvocationTargetException - if an error occured when sending the packet.
	 */
	public void sendServerPacket(Player reciever, PacketContainer packet, boolean filters)
			throws InvocationTargetException;

	/**
	 * Simulate recieving a certain packet from a given player.
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	public void recieveClientPacket(Player sender, PacketContainer packet) 
			throws IllegalAccessException, InvocationTargetException;

	/**
	 * Simulate recieving a certain packet from a given player.
	 * @param sender - the sender.
	 * @param packet - the packet that was sent.
	 * @param filters - whether or not to invoke any packet filters.
	 * @throws InvocationTargetException If the reflection machinery failed.
	 * @throws IllegalAccessException If the underlying method caused an error.
	 */
	public void recieveClientPacket(Player sender, PacketContainer packet, boolean filters)
			throws IllegalAccessException, InvocationTargetException;
}
