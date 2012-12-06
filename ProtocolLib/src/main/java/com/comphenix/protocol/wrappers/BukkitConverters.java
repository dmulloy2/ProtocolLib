package com.comphenix.protocol.wrappers;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;

/**
 * Contains several useful equivalent converters for normal Bukkit types.
 * 
 * @author Kristian
 */
public class BukkitConverters {
	// Check whether or not certain classes exists
	private static boolean hasWorldType = false;
	
	// Used to access the world type
	private static Method worldTypeName;
	private static Method worldTypeGetType;
	
	static {
		try {
			Class.forName(MinecraftReflection.getMinecraftPackage() + ".WorldType");
			hasWorldType = true;
		} catch (ClassNotFoundException e) {
		}
	}
	
	public static <T> EquivalentConverter<List<T>> getListConverter(final Class<?> genericItemType, final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return getIgnoreNull(new EquivalentConverter<List<T>>() {
				@SuppressWarnings("unchecked")
				@Override
				public List<T> getSpecific(Object generic) {
					if (generic instanceof Collection) {
						List<T> items = new ArrayList<T>();
					
						// Copy everything to a new list
						for (Object item : (Collection<Object>) generic) {
							T result = itemConverter.getSpecific(item);
							
							if (item != null)
								items.add(result);
						}
						return items;
					}
					
					// Not valid
					return null;
				}

				@SuppressWarnings("unchecked")
				@Override
				public Object getGeneric(Class<?> genericType, List<T> specific) {
					Collection<Object> newContainer = (Collection<Object>) DefaultInstances.DEFAULT.getDefault(genericType);
					
					// Convert each object
					for (T position : specific) {
						Object converted = itemConverter.getGeneric(genericItemType, position);
						
						if (position == null)
							newContainer.add(null);
						else if (converted != null)
							newContainer.add(converted);
					}
					return newContainer;
				}

				@SuppressWarnings("unchecked")
				@Override
				public Class<List<T>> getSpecificType() {
					// Damn you Java
					Class<?> dummy = List.class;
					return (Class<List<T>>) dummy;
				}
			}
		);
	}
	
	/**
	 * Retrieve a converter for watchable objects and the respective wrapper.
	 * @return A watchable object converter.
	 */
	public static EquivalentConverter<WrappedWatchableObject> getWatchableObjectConverter() {
		return getIgnoreNull(new EquivalentConverter<WrappedWatchableObject>() {
			@Override
			public Object getGeneric(Class<?> genericType, WrappedWatchableObject specific) {
				return specific.getHandle();
			}
			
			public WrappedWatchableObject getSpecific(Object generic) {
				if (MinecraftReflection.isWatchableObject(generic))
					return new WrappedWatchableObject(generic);
				else if (generic instanceof WrappedWatchableObject)
					return (WrappedWatchableObject) generic;
				else
					throw new IllegalArgumentException("Unrecognized type " + generic.getClass());
			};
			
			@Override
			public Class<WrappedWatchableObject> getSpecificType() {
				return WrappedWatchableObject.class;
			}
		});
	}
	
	/**
	 * Retrieve a converter for the NMS DataWatcher class and our wrapper.
	 * @return A DataWatcher converter.
	 */
	public static EquivalentConverter<WrappedDataWatcher> getDataWatcherConverter() {
		return getIgnoreNull(new EquivalentConverter<WrappedDataWatcher>() {
			@Override
			public Object getGeneric(Class<?> genericType, WrappedDataWatcher specific) {
				return specific.getHandle();
			}
			
			@Override
			public WrappedDataWatcher getSpecific(Object generic) {
				if (MinecraftReflection.isDataWatcher(generic))
					return new WrappedDataWatcher(generic);
				else if (generic instanceof WrappedDataWatcher)
					return (WrappedDataWatcher) generic;
				else
					throw new IllegalArgumentException("Unrecognized type " + generic.getClass());
			}
			
			@Override
			public Class<WrappedDataWatcher> getSpecificType() {
				return WrappedDataWatcher.class;
			}
		});
	}
	
	/**
	 * Retrieve a converter for Bukkit's world type enum and the NMS equivalent.
	 * @return A world type enum converter.
	 */
	public static EquivalentConverter<WorldType> getWorldTypeConverter() {
		// Check that we can actually use this converter
		if (!hasWorldType)
			return null;
		
		return getIgnoreNull(new EquivalentConverter<WorldType>() {
			@SuppressWarnings("unchecked")
			@Override
			public Object getGeneric(Class<?> genericType, WorldType specific) {
				try {
					if (worldTypeGetType == null)
						worldTypeGetType = MinecraftReflection.getWorldTypeClass().getMethod("getType", String.class);
					
					// Convert to the Bukkit world type
					return worldTypeGetType.invoke(this, specific.getName());
					
				} catch (Exception e) {
					throw new FieldAccessException("Cannot find the WorldType.getType() method.", e);
				}	
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public WorldType getSpecific(Object generic) {
				try {
					if (worldTypeName == null)
						worldTypeName = MinecraftReflection.getWorldTypeClass().getMethod("name");
					
					// Dynamically call the namne method
					String name = (String) worldTypeName.invoke(generic);
					return WorldType.getByName(name);
					
				} catch (Exception e) {
					throw new FieldAccessException("Cannot call the name method in WorldType.", e);
				}
			}
			
			@Override
			public Class<WorldType> getSpecificType() {
				return WorldType.class;
			}
		});
	}
	
	/**
	 * Retrieve a converter for NMS entities and Bukkit entities.
	 * @param world - the current world.
	 * @return A converter between the underlying NMS entity and Bukkit's wrapper.
	 */
	public static EquivalentConverter<Entity> getEntityConverter(World world) {
		final World container = world;
		final WeakReference<ProtocolManager> managerRef = 
				new WeakReference<ProtocolManager>(ProtocolLibrary.getProtocolManager());

		return getIgnoreNull(new EquivalentConverter<Entity>() {
			
			@Override
			public Object getGeneric(Class<?> genericType, Entity specific) {
				// Simple enough
				return specific.getEntityId();
			}
			
			@Override
			public Entity getSpecific(Object generic) {
				try {
					Integer id = (Integer) generic;
					ProtocolManager manager = managerRef.get();
					
					// Use the 
					if (id != null && manager != null) {
						return manager.getEntityFromID(container, id);
					} else {
						return null;
					}
					
				} catch (FieldAccessException e) {
					throw new RuntimeException("Cannot retrieve entity from ID.", e);
				}
			}
			
			@Override
			public Class<Entity> getSpecificType() {
				return Entity.class;
			}
		});
	}
	
	/**
	 * Retrieve the converter used to convert NMS ItemStacks to Bukkit's ItemStack.
	 * @return Item stack converter.
	 */
	public static EquivalentConverter<ItemStack> getItemStackConverter() {
		return getIgnoreNull(new EquivalentConverter<ItemStack>() {
			public Object getGeneric(Class<?> genericType, ItemStack specific) {
				return MinecraftReflection.getMinecraftItemStack(specific);
			}
			
			@Override
			public ItemStack getSpecific(Object generic) {
				return MinecraftReflection.getBukkitItemStack(generic);
			}
			
			@Override
			public Class<ItemStack> getSpecificType() {
				return ItemStack.class;
			}
		});
	}
	
	/**
	 * Wraps a given equivalent converter in NULL checks, ensuring that such values are ignored.
	 * @param delegate - the underlying equivalent converter.
	 * @return A equivalent converter that ignores NULL values.
	 */
	public static <TType> EquivalentConverter<TType> getIgnoreNull(final EquivalentConverter<TType> delegate) {
		// Automatically wrap all parameters to the delegate with a NULL check
		return new EquivalentConverter<TType>() {
			public Object getGeneric(Class<?> genericType, TType specific) {
				if (specific != null)
					return delegate.getGeneric(genericType, specific);
				else
					return null;
			}
			
			@Override
			public TType getSpecific(Object generic) {
				if (generic != null)
					return delegate.getSpecific(generic);
				else
					return null;
			}
			
			@Override
			public Class<TType> getSpecificType() {
				return delegate.getSpecificType();
			}
		};
	}
}
