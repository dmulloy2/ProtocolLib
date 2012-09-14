package com.comphenix.protocol.events;

/**
 * Represents a packet event priority, similar to the Bukkit EventPriority.
 * 
 * @author Kristian
 */
public enum ListenerPriority {
	/**
	 * Event call is of very low importance and should be ran first, to allow
	 * other plugins to further customise the outcome.
	 */
	LOWEST(0),
	/**
	 * Event call is of low importance.
	 */
	LOW(1),
	/**
	 * Event call is neither important or unimportant, and may be ran normally.
	 */
	NORMAL(2),
	/**
	 * Event call is of high importance.
	 */
	HIGH(3),
	/**
	 * Event call is critical and must have the final say in what happens to the
	 * event.
	 */
	HIGHEST(4),
	/**
	 * Event is listened to purely for monitoring the outcome of an event.
	 * <p/>
	 * No modifications to the event should be made under this priority.
	 */
	MONITOR(5);

	private final int slot;

	private ListenerPriority(int slot) {
		this.slot = slot;
	}

	/**
	 * A low slot represents a low priority.
	 * @return Integer representation of this priorty.
	 */
	public int getSlot() {
		return slot;
	}
}
