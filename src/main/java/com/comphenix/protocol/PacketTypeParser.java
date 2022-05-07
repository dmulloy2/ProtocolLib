package com.comphenix.protocol;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.ConnectionSide;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

class PacketTypeParser {
	public static final Range<Integer> DEFAULT_MAX_RANGE = Range.closed(0, 255);
	
	private Sender side = null;
	private Protocol protocol = null;
	
	public Set<PacketType> parseTypes(Deque<String> arguments, Range<Integer> defaultRange) {
		final Set<PacketType> result = new HashSet<>();
		side = null;
		protocol = null;
		
		// Find these first
		while (side == null) {
			String arg = arguments.poll();
			
			// Attempt to parse a side or protocol first
			if (side == null) {
				ConnectionSide connection = parseSide(arg);
				
				if (connection != null) {
					side = connection.getSender();
					continue;
				}
			}
			if (protocol == null) {
				if ((protocol = parseProtocol(arg)) != null) {
					continue;
				}
			}
			throw new IllegalArgumentException("Specify connection side (CLIENT or SERVER).");
		}
		
		// Then we move on to parsing IDs (named packet types soon to come)
		List<Range<Integer>> ranges = RangeParser.getRanges(arguments, DEFAULT_MAX_RANGE);

		// And finally, parse packet names if we have a protocol
		if (protocol != null) {
			for (Iterator<String> it = arguments.iterator(); it.hasNext(); ) {
				String name = it.next().toUpperCase(Locale.ENGLISH);
				Collection<PacketType> names = PacketType.fromName(name);
				
				for (PacketType type : names) {
					if (type.getProtocol() == protocol && type.getSender() == side) {
						result.add(type);
						it.remove();
					}
				}
			}
		}
		
		// Supply a default integer range
		if (ranges.isEmpty() && result.isEmpty()) {
			ranges = Collections.singletonList(defaultRange);
		}
		
		for (Range<Integer> range : ranges) {
			for (Integer id : ContiguousSet.create(range, DiscreteDomain.integers())) {
				// Deprecated packets
				if (protocol == null) {
					if (PacketType.hasLegacy(id)) {
						result.add(PacketType.findLegacy(id, side));
					}
				} else {
					if (PacketType.hasCurrent(protocol, side, id)) {
						result.add(PacketType.findCurrent(protocol, side, id));
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * Retrieve the last parsed protocol.
	 * @return Last protocol.
	 */
	public Protocol getLastProtocol() {
		return protocol;
	}
	
	/**
	 * Retrieve the last sender.
	 * @return Last sender.
	 */
	public Sender getLastSide() {
		return side;
	}
	
	/**
	 * Parse a connection sides from a string.
	 * @param text - the possible connection side.
	 * @return The connection side, or NULL if not found.
	 */
	public ConnectionSide parseSide(String text) {
		if (text == null)
			return null;
		String candidate = text.toLowerCase();
		
		// Parse the side gracefully
		if ("client".startsWith(candidate))
			return ConnectionSide.CLIENT_SIDE;
		else if ("server".startsWith(candidate))
			return ConnectionSide.SERVER_SIDE;
		else
			return null;
	}
	
	/**
	 * Parse a protocol from a string.
	 * @param text - the possible protocol.
	 * @return The protocol, or NULL if not found.
	 */
	public Protocol parseProtocol(String text) {
		if (text == null) return null;

		switch (text.toLowerCase()) {
			case "handshake":
			case "handshaking":
				return Protocol.HANDSHAKING;
			case "login":
				return Protocol.LOGIN;
			case "play":
			case "game":
				return Protocol.PLAY;
			case "status":
				return Protocol.STATUS;
			default: return null;
		}
	}
}
