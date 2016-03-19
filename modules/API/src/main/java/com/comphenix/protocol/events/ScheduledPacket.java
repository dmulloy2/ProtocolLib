package com.comphenix.protocol.events;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketStream;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a packet that is scheduled for transmission at a later stage.
 * @author Kristian
 */
public class ScheduledPacket {
	protected PacketContainer packet;
	protected Player target;
	protected boolean filtered;
	
	/**
	 * Construct a new scheduled packet.
	 * <p>
	 * Note that the sender is infered from the packet type.
	 * @param packet - the packet.
	 * @param target - the target player.
	 * @param filtered - whether or not to 
	 */
	public ScheduledPacket(PacketContainer packet, Player target, boolean filtered) {
		setPacket(packet);
		setTarget(target);
		setFiltered(filtered);
	}
	
	/**
	 * Construct a new scheduled packet that will not be processed by any packet listeners (except MONITOR).
	 * @param packet - the packet.
	 * @param target - the target player.
	 * @return The scheduled packet.
	 */
	public static ScheduledPacket fromSilent(PacketContainer packet, Player target) {
		return new ScheduledPacket(packet, target, false);
	}

	/**
	 * Construct a new scheduled packet that will be processed by any packet listeners.
	 * @param packet - the packet.
	 * @param target - the target player.
	 * @return The scheduled packet.
	 */
	public static ScheduledPacket fromFiltered(PacketContainer packet, Player target) {
		return new ScheduledPacket(packet, target, true);
	}
	
	/**
	 * Retrieve the packet that will be sent or transmitted.
	 * @return The sent or received packet.
	 */
	public PacketContainer getPacket() {
		return packet;
	}
	
	/**
	 * Set the packet that will be sent or transmitted.
	 * @param packet - the new packet, cannot be NULL.
	 */
	public void setPacket(PacketContainer packet) {
		this.packet = Preconditions.checkNotNull(packet, "packet cannot be NULL");
	}
	
	/**
	 * Retrieve the target player.
	 * @return The target player.
	 */
	public Player getTarget() {
		return target;
	}
	
	/**
	 * Set the target player.
	 * @param target - the new target, cannot be NULL.
	 */
	public void setTarget(Player target) {
		this.target = Preconditions.checkNotNull(target, "target cannot be NULL");
	}
	
	/**
	 * Determine if this packet will be processed by any of the packet listeners.
	 * @return TRUE if it will, FALSE otherwise.
	 */
	public boolean isFiltered() {
		return filtered;
	}
	
	/**
	 * Set whether or not this packet will be processed by packet listeners (except MONITOR listeners).
	 * @param filtered - TRUE if it should be processed by listeners, FALSE otherwise.
	 */
	public void setFiltered(boolean filtered) {
		this.filtered = filtered;
	}
	
	/**
	 * Retrieve the sender of this packet.
	 * @return The sender.
	 */
	public Sender getSender() {
		return packet.getType().getSender();
	}
	
	/**
	 * Schedule the packet transmission or reception.
	 */
	public void schedule() {
		schedule(ProtocolLibrary.getProtocolManager());
	}
	
	/**
	 * Schedule the packet transmission or reception.
	 * @param stream - the packet stream.
	 */
	public void schedule(PacketStream stream) {
		Preconditions.checkNotNull(stream, "stream cannot be NULL");
		
		try {
			if (getSender() == Sender.CLIENT) {
				stream.recieveClientPacket(getTarget(), getPacket(), isFiltered());
			} else {
				stream.sendServerPacket(getTarget(), getPacket(), isFiltered());
			}
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Cannot send packet " + this + " to " + stream);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Cannot send packet " + this + " to " + stream);
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("packet", packet)
			.add("target", target)
			.add("filtered", filtered)
			.toString();
	}
}
