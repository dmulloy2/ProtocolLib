package com.comphenix.protocol.wrappers.nbt.io;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.comphenix.protocol.wrappers.nbt.NbtVisitor;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

/**
 * Serialize and deserialize NBT information from a configuration section.
 * <p>
 * Note that data types may be internally preserved by modifying the serialized name. This may
 * be visible to the end-user.
 * 
 * @author Kristian
 */
public class NbtConfigurationSerializer {
	/**
	 * The default delimiter that is used to store the data type in YAML.
	 */
	public static final String TYPE_DELIMITER = "$";
	
	/**
	 * A standard YAML serializer.
	 */
	public static final NbtConfigurationSerializer DEFAULT = new NbtConfigurationSerializer();

	private String dataTypeDelimiter;

	/**
	 * Construct a serializer using {@link #TYPE_DELIMITER} as the default delimiter.
	 */
	public NbtConfigurationSerializer() {
		this.dataTypeDelimiter = TYPE_DELIMITER;
	}
	
	/**
	 * Construct a serializer using the given value as a delimiter.
	 * @param dataTypeDelimiter - the local data type delimiter.
	 */
	public NbtConfigurationSerializer(String dataTypeDelimiter) {
		this.dataTypeDelimiter = dataTypeDelimiter;
	}

	/**
	 * Retrieve the current data type delimiter.
	 * @return The current data type delimiter.
	 */
	public String getDataTypeDelimiter() {
		return dataTypeDelimiter;
	}

	/**
	 * Write the content of a NBT tag to a configuration section.
	 * @param value - the NBT tag to write.
	 * @param destination - the destination section.
	 */
	public <TType> void serialize(NbtBase<TType> value, final ConfigurationSection destination) {
		value.accept(new NbtVisitor() {
			private ConfigurationSection current = destination;
			
			// The current list we're working on
			private List<Object> currentList;
			
			// Store the index of a configuration section that works like a list
			private Map<ConfigurationSection, Integer> workingIndex = Maps.newHashMap();
			
			@Override
			public boolean visitEnter(NbtCompound compound) {
				current = current.createSection(compound.getName());
				return true;
			}
			
			@Override
			public boolean visitEnter(NbtList<?> list) {
				Integer listIndex = getNextIndex();
				String name = getEncodedName(list, listIndex);
				
				if (list.getElementType().isComposite()) {
					// Use a configuration section to store this list
					current = current.createSection(name);
					workingIndex.put(current, 0);
				} else {
					currentList = Lists.newArrayList();
					current.set(name, currentList);
				}
				return true;
			}
			
			@Override
			public boolean visitLeave(NbtCompound compound) {
				current = current.getParent();
				return true;
			}
			
			@Override
			public boolean visitLeave(NbtList<?> list) {
				// Write the list to the configuration section
				if (currentList != null) {
					// Save and reset the temporary list
					currentList = null;
				} else {
					// Go up a level
					workingIndex.remove(current);
					current = current.getParent();
				}
				return true;
			}
			
			@Override
			public boolean visit(NbtBase<?> node) {
				// Are we working on a list?
				if (currentList == null) {
					Integer listIndex = getNextIndex();
					String name = getEncodedName(node, listIndex);
					
					// Save member
					current.set(name, fromNodeValue(node));
					
				} else {
					currentList.add(fromNodeValue(node));
				}
				return true;
			}
			
			private Integer getNextIndex() {
				Integer listIndex = workingIndex.get(current);
				
				if (listIndex != null) 
					return workingIndex.put(current, listIndex + 1);
				else
					return null;
			}
			
			// We need to store the data type somehow
			private String getEncodedName(NbtBase<?> node, Integer index) {
				if (index != null)
					return index + dataTypeDelimiter + node.getType().getRawID();
				else
					return node.getName() + dataTypeDelimiter + node.getType().getRawID();
			}
			
			private String getEncodedName(NbtList<?> node, Integer index) {
				if (index != null)
					return index + dataTypeDelimiter + node.getElementType().getRawID();
				else
					return node.getName() + dataTypeDelimiter + node.getElementType().getRawID();
			}
		});
	}
	
	/**
	 * Read a NBT tag from a root configuration. 
	 * @param root - configuration that contains the NBT tag.
	 * @param nodeName - name of the NBT tag.
	 * @return The read NBT tag.
	 */
	@SuppressWarnings("unchecked")
	public <TType> NbtWrapper<TType> deserialize(ConfigurationSection root, String nodeName) {
		return (NbtWrapper<TType>) readNode(root, nodeName);
	}
	
	/**
	 * Read a NBT compound from a root configuration. 
	 * @param root - configuration that contains the NBT compound.
	 * @param nodeName - name of the NBT compound.
	 * @return The read NBT compound.
	 */
	public NbtCompound deserializeCompound(YamlConfiguration root, String nodeName) {
		return (NbtCompound) readNode(root, nodeName);
	}
	
	/**
	 * Read a NBT compound from a root configuration. 
	 * @param root - configuration that contains the NBT compound.
	 * @param nodeName - name of the NBT compound.
	 * @return The read NBT compound.
	 */
	@SuppressWarnings("unchecked")
	public <T> NbtList<T> deserializeList(YamlConfiguration root, String nodeName) {
		return (NbtList<T>) readNode(root, nodeName);
	}
	
	@SuppressWarnings("unchecked")
	private NbtWrapper<?> readNode(ConfigurationSection parent, String name) {
		String[] decoded = getDecodedName(name);
		Object node = parent.get(name);
		NbtType type = NbtType.TAG_END;
		
		// It's possible that the caller isn't aware of the encoded name itself
		if (node == null) {
			for (String key : parent.getKeys(false)) {
				decoded = getDecodedName(key);
				
				// Great
				if (decoded[0].equals(name)) {
					node = parent.get(decoded[0]);
					break;
				}
			}
			
			// Inform the caller of the problem
			if (node == null) {
				throw new IllegalArgumentException("Unable to find node " + name + " in " + parent);
			}
		}
		
		// Attempt to decode a NBT type
		if (decoded.length > 1) {
			type = NbtType.getTypeFromID(Integer.parseInt(decoded[1]));
		}
		
		// Is this a compound?
		if (node instanceof ConfigurationSection) {
			// Is this a list of a map?
			if (type != NbtType.TAG_END) {
				NbtList<Object> list = NbtFactory.ofList(decoded[0]);
				ConfigurationSection section = (ConfigurationSection) node;
				List<String> sorted = sortSet(section.getKeys(false));
				
				// Read everything in order
				for (String key : sorted) {
					NbtBase<Object> base = (NbtBase<Object>) readNode(section, key.toString());
					base.setName(NbtList.EMPTY_NAME);
					list.getValue().add(base);
				}
				return (NbtWrapper<?>) list;
			
			} else {
				NbtCompound compound = NbtFactory.ofCompound(decoded[0]);
				ConfigurationSection section = (ConfigurationSection) node;
				
				// As above
				for (String key : section.getKeys(false)) 
					compound.put(readNode(section, key));
				return (NbtWrapper<?>) compound;
			}
			
		} else {
			// We need to know
			if (type == NbtType.TAG_END) {
				throw new IllegalArgumentException("Cannot find encoded type of " + decoded[0] + " in " + name);
			}

			if (node instanceof List) {
				NbtList<Object> list = NbtFactory.ofList(decoded[0]);
				list.setElementType(type);
				
				for (Object value : (List<Object>) node) {
					list.addClosest(toNodeValue(value, type));
				}
				
				// Add the list
				return (NbtWrapper<?>) list;
			
			} else {
				// Normal node
				return NbtFactory.ofWrapper(type, decoded[0], toNodeValue(node, type));
			}
		}
	}
	
	private List<String> sortSet(Set<String> unsorted) {
		// Convert to integers
		List<String> sorted = new ArrayList<String>(unsorted);
		
		Collections.sort(sorted, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				// Parse the name
				int index1 = Integer.parseInt(getDecodedName(o1)[0]);
				int index2 = Integer.parseInt(getDecodedName(o2)[0]);
				return Ints.compare(index1, index2);
			}
		});
		return sorted;
	}
	
	// Ensure that int arrays are converted to byte arrays
	private Object fromNodeValue(NbtBase<?> base) {
		if (base.getType() == NbtType.TAG_INT_ARRAY)
			return toByteArray((int[]) base.getValue());
		else
			return base.getValue();
	}
	
	// Convert them back
	public Object toNodeValue(Object value, NbtType type) {
		if (type == NbtType.TAG_INT_ARRAY)
			return toIntegerArray((byte[]) value);
		else
			return value;
	}
	
	/**
	 * Convert an integer array to an equivalent byte array.
	 * @param data - the integer array with the data.
	 * @return An equivalent byte array.
	 */
	private static byte[] toByteArray(int[] data) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        
        intBuffer.put(data);
        return byteBuffer.array();
	}
	
	/**
	 * Convert a byte array to the equivalent integer array.
	 * <p>
	 * Note that the number of byte elements are only perserved if the byte size is a multiple of four.
	 * @param data - the byte array to convert.
	 * @return The equivalent integer array.
	 */
	private static int[] toIntegerArray(byte[] data) {
		IntBuffer source = ByteBuffer.wrap(data).asIntBuffer();
		IntBuffer copy = IntBuffer.allocate(source.capacity());
		
		copy.put(source);
		return copy.array();
	}
	
	private static String[] getDecodedName(String nodeName) {
		int delimiter = nodeName.lastIndexOf('$');
		
		if (delimiter > 0)
			return new String[] { nodeName.substring(0, delimiter), nodeName.substring(delimiter + 1) };
		else
			return new String[] { nodeName };
	}
}
