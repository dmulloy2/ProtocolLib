package com.comphenix.protocol.utility;

import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * Represents a class for printing hexadecimal dumps.
 * 
 * @author Kristian
 */
public class HexDumper {
	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	// Default values
	private int positionLength = 6;
	private char[] positionSuffix = ": ".toCharArray();
	private char[] delimiter = " ".toCharArray();
	private int groupLength = 2;
	private int groupCount = 24;
	private char[] lineDelimiter = "\n".toCharArray();
	
	/**
	 * Retrieve a hex dumper tuned for lines of 80 characters:
	 * <table border="1">
	 * <tr>
	 *     <th>Property</th>
	 *     <th>Value</th>
	 * </tr>
	 * <tr>
	 *     <td>Position Length</td>
	 *     <td>6</td>
	 * </tr>
	 * <tr>
	 *     <td>Position Suffix</td>
	 *     <td>": "</td>
	 * </tr>
	 * <tr>
	 *     <td>Delimiter</td>
	 *     <td>" "</td>
	 * </tr>
	 * <tr>
	 *     <td>Group Length</td>
	 *     <td>2</td>
	 * </tr>
	 * <tr>
	 *     <td>Group Count</td>
	 *     <td>24</td>
	 * </tr>
	 * <tr>
	 *     <td>Line Delimiter</td>
	 *     <td>"\n"</td>
	 * </tr>
	 * </table>
	 * @return The default dumper.
	 */
	public static HexDumper defaultDumper() {
		return new HexDumper();
	}
	
	/**
	 * Set the delimiter between each new line.
	 * @param lineDelimiter - the line delimiter.
	 * @return This instance, for chaining.
	 */
	public HexDumper lineDelimiter(String lineDelimiter) {
		this.lineDelimiter = Preconditions.checkNotNull(lineDelimiter, "lineDelimiter cannot be NULL").toCharArray();
		return this;
	}

	/**
	 * Set the number of hex characters in the position.
	 * @param positionLength - number of characters, from 0 to 8.
	 * @return This instance, for chaining.
	 */
	public HexDumper positionLength(int positionLength) {
		if (positionLength < 0)
			throw new IllegalArgumentException("positionLength cannot be less than zero.");
		if (positionLength > 8)
			throw new IllegalArgumentException("positionLength cannot be greater than eight.");
		this.positionLength = positionLength;
		return this;
	}

	/**
	 * Set a suffix to write after each position.
	 * @param positionSuffix - non-null string to write after the positions.
	 * @return This instance, for chaining.
	 */
	public HexDumper positionSuffix(String positionSuffix) {
		this.positionSuffix = Preconditions.checkNotNull(positionSuffix, "positionSuffix cannot be NULL").toCharArray();
		return this;
	}

	/**
	 * Set the delimiter to write in between each group of hexadecimal characters.
	 * @param delimiter - non-null string to write between each group.
	 * @return This instance, for chaining.
	 */
	public HexDumper delimiter(String delimiter) {
		this.delimiter = Preconditions.checkNotNull(delimiter, "delimiter cannot be NULL").toCharArray();
		return this;
	}

	/**
	 * Set the length of each group in hexadecimal characters.
	 * @param groupLength - the length of each group.
	 * @return This instance, for chaining.
	 */
	public HexDumper groupLength(int groupLength) {
		if (groupLength < 1)
			throw new IllegalArgumentException("groupLength cannot be less than one.");
		this.groupLength = groupLength;
		return this;
	}

	/**
	 * Set the number of groups in each line. This is limited by the supply of bytes in the byte array.
	 * <p>
	 * Use {@link Integer#MAX_VALUE} to effectively disable lines.
	 * @param groupLength - the length of each group.
	 * @return This instance, for chaining.
	 */
	public HexDumper groupCount(int groupCount) {
		if (groupCount < 1)
			throw new IllegalArgumentException("groupCount cannot be less than one.");
		this.groupCount = groupCount;
		return this;
	}

	/**
	 * Append the hex dump of the given data to the string builder, using the current formatting settings.
	 * @param appendable - appendable source.
	 * @param data - the data to dump.
	 * @param start - the starting index of the data.
	 * @param length - the number of bytes to dump.
	 * @throws IOException Any underlying IO exception.
	 */
	public void appendTo(Appendable appendable, byte[] data) throws IOException {
		appendTo(appendable, data, 0, data.length);
	}
	
	/**
	 * Append the hex dump of the given data to the string builder, using the current formatting settings.
	 * @param appendable - appendable source.
	 * @param data - the data to dump.
	 * @param start - the starting index of the data.
	 * @param length - the number of bytes to dump.
	 * @throws IOException Any underlying IO exception.
	 */
	public void appendTo(Appendable appendable, byte[] data, int start, int length) throws IOException {
		StringBuilder output = new StringBuilder();
		appendTo(output, data, start, length);
		appendable.append(output.toString());
	}
	
	/**
	 * Append the hex dump of the given data to the string builder, using the current formatting settings.
	 * @param builder - the builder.
	 * @param data - the data to dump.
	 * @param start - the starting index of the data.
	 * @param length - the number of bytes to dump.
	 */
	public void appendTo(StringBuilder builder, byte[] data) {
		appendTo(builder, data, 0, data.length);
	}
	
	/**
	 * Append the hex dump of the given data to the string builder, using the current formatting settings.
	 * @param builder - the builder.
	 * @param data - the data to dump.
	 * @param start - the starting index of the data.
	 * @param length - the number of bytes to dump.
	 */
	public void appendTo(StringBuilder builder, byte[] data, int start, int length) {
	    // Positions
	    int dataIndex = start;
	    int dataEnd = start + length;
	    int groupCounter = 0;
	    int currentGroupLength = 0;

	    // Current niblet in the byte
	    int value = 0;
	    boolean highNiblet = true;
	    
	    while (dataIndex < dataEnd || !highNiblet) {
	    	// Prefix
	    	if (groupCounter == 0 && currentGroupLength == 0) {
	    		// Print the current dataIndex (print in reverse)
	    		for (int i = positionLength - 1; i >= 0; i--) {
	    			builder.append(HEX_DIGITS[(dataIndex >>> (4 * i)) & 0xF]);
	    		}
	    		builder.append(positionSuffix);
	    	}
	    	
	    	// Print niblet
	        if (highNiblet) {
	        	value = data[dataIndex++] & 0xFF;
	        	builder.append(HEX_DIGITS[value >>> 4]);
	        } else {
	        	builder.append(HEX_DIGITS[value & 0x0F]);
	        }
	        highNiblet = !highNiblet;
	        currentGroupLength++;
	        
	        // See if we're dealing with the last element
	        if (currentGroupLength >= groupLength) {
	    		currentGroupLength = 0;
	    		
	    		// See if we've reached the last element in the line
	    		if (++groupCounter >= groupCount) {
	    			builder.append(lineDelimiter);
	    			groupCounter = 0;
	    		} else {
		    		// Write delimiter
	    			builder.append(delimiter);
	    		}
	        }
	    }
	}
	
	/**
	 * Calculate the length of each line.
	 * @param byteCount - the maximum number of bytes
	 * @return The lenght of the final line.
	 */
	public int getLineLength(int byteCount) {
		int constant = positionLength + positionSuffix.length + lineDelimiter.length;
		int groups = Math.min((2 * byteCount) / groupLength, groupCount);

		// Total expected length of each line
		return constant + delimiter.length * (groups - 1) + groupLength * groups;
	}
}
