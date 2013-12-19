package com.comphenix.protocol;

import java.util.Deque;
import java.util.List;
import java.util.Set;

import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.events.ConnectionSide;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;
import com.google.common.collect.Sets;

class PacketTypeParser {
	public final static Range<Integer> DEFAULT_MAX_RANGE = Ranges.closed(0, 255);
	
	public Set<PacketType> parseTypes(Deque<String> arguments, Range<Integer> defaultRange) {
		Sender side = null;
		Protocol protocol = null;
		Set<PacketType> result = Sets.newHashSet();

		// Find these first
		while (protocol == null || side == null) {
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
			throw new IllegalArgumentException("No side and protocol specified.");
		}
		
		// Then we move on to parsing IDs (named packet types soon to come)
		List<Range<Integer>> ranges = RangeParser.getRanges(arguments, DEFAULT_MAX_RANGE);
		
		// Supply a default integer range
		if (ranges.size() == 0) {
			ranges = Lists.newArrayList();
			ranges.add(defaultRange);
		}
		
		for (Range<Integer> range : ranges) {
			for (Integer id : range.asSet(DiscreteDomains.integers())) {
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
		if (text == null)
			return null;
		String candidate = text.toLowerCase();
		
		if ("handshake".equals(candidate) || "handshaking".equals(candidate))
			return Protocol.HANDSHAKING;
		else if ("login".equals(candidate))
			return Protocol.LOGIN;
		else if ("play".equals(candidate) || "game".equals(candidate))
			return Protocol.PLAY;
		else if ("status".equals(candidate))
			return Protocol.STATUS;
		else
			return null;
	}	
}
