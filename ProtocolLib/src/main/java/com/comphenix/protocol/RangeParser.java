/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * Used to parse ranges in CommandPacket.
 * 
 * @author Kristian
 */
class RangeParser {
	/**
	 * Parse a range from a given text.
	 * @param text - the text.
	 * @param legalRange - range of legal values.
	 * @return The parsed ranges.
	 */
	public static List<Range<Integer>> getRanges(String text, Range<Integer> legalRange) {
		return getRanges(new ArrayDeque<String>(Arrays.asList(text)), legalRange);
	}
	
	/**
	 * Parse ranges from an array of elements.
	 * @param args - array of elements.
	 * @param offset - beginning offset.
	 * @param lastIndex - the last index of the array to read.
	 * @param legalRange - range of legal values.
	 * @return The parsed ranges.
	 */
	public static List<Range<Integer>> getRanges(Deque<String> input, Range<Integer> legalRange) {
		List<String> tokens = tokenizeInput(input);
		List<Range<Integer>> ranges = new ArrayList<Range<Integer>>();
		
		for (int i = 0; i < tokens.size(); i++) {
			Range<Integer> range;
			String current = tokens.get(i);
			String next = i + 1 < tokens.size() ? tokens.get(i + 1) : null;
			
			// Yoda equality is done for null-safety
			if ("-".equals(current)) {
				throw new IllegalArgumentException("A hyphen must appear between two numbers.");
			} else if ("-".equals(next)) {
				if (i + 2 >= tokens.size())
					throw new IllegalArgumentException("Cannot form a range without a upper limit.");

				// This is a proper range
				range = Ranges.closed(Integer.parseInt(current), Integer.parseInt(tokens.get(i + 2)));
				ranges.add(range);
				
				// Skip the two next tokens
				i += 2;
				
			} else {
				// Just a single number
				range = Ranges.singleton(Integer.parseInt(current));
				ranges.add(range);
			}
			
			// Validate ranges
			if (!legalRange.encloses(range)) {
				throw new IllegalArgumentException(range + " is not in the range " + range.toString());
			}
		}
		
		return simplify(ranges, legalRange.upperEndpoint());
	}
	
	/**
	 * Simplify a list of ranges by assuming a maximum value.
	 * @param ranges - the list of ranges to simplify.
	 * @param maximum - the maximum value (minimum value is always 0).
	 * @return A simplified list of ranges.
	 */
	private static List<Range<Integer>> simplify(List<Range<Integer>> ranges, int maximum) {
		List<Range<Integer>> result = new ArrayList<Range<Integer>>();
		boolean[] set = new boolean[maximum + 1];
		int start = -1;
		
		// Set every ID
		for (Range<Integer> range : ranges) {
			for (int id : range.asSet(DiscreteDomains.integers())) {
				set[id] = true;
			}
		}
		
		// Generate ranges from this set
		for (int i = 0; i <= set.length; i++) {
			if (i < set.length && set[i]) {
				if (start < 0) {
					start = i;
				}
			} else {
				if (start >= 0) {
					result.add(Ranges.closed(start, i - 1));
					start = -1;
				}
			}
		}
		
		return result;
	}
	
	private static List<String> tokenizeInput(Deque<String> input) {
		List<String> tokens = new ArrayList<String>();
		
		// Tokenize the input
		while (!input.isEmpty()) {
			StringBuilder number = new StringBuilder();
			String text = input.peek();
			
			for (int j = 0; j < text.length(); j++) {
				char current = text.charAt(j);
				
				if (Character.isDigit(current)) {
					number.append(current);
				} else if (Character.isWhitespace(current)) {
					// That's ok
				} else if (current == '-') {
					// Add the number token first
					if (number.length() > 0) {
						tokens.add(number.toString());
						number.setLength(0);
					}
					
					tokens.add(Character.toString(current));
				} else {
					// We're no longer dealing with integers - quit
					return tokens;
				}
			}
			
			// Add the number token, if it hasn't already
			if (number.length() > 0)
				tokens.add(number.toString());
			input.poll();
		}
		
		return tokens;
	}
}
