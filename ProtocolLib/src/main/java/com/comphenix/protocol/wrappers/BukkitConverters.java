package com.comphenix.protocol.wrappers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.server.DataWatcher;
import net.minecraft.server.WatchableObject;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.instances.DefaultInstances;

/**
 * Contains several useful equivalent converters for normal Bukkit types.
 * 
 * @author Kristian
 */
public class BukkitConverters {
	// Check whether or not certain classes exists
	private static boolean hasWorldType = false;
	
	// The getEntity method
	private static Method getEntity;
	
	static {
		try {
			Class.forName("net.minecraft.server.WorldType");
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
				if (generic instanceof WatchableObject)
					return new WrappedWatchableObject((WatchableObject) generic);
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
				if (generic instanceof DataWatcher)
					return new WrappedDataWatcher((DataWatcher) generic);
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
			@Override
			public Object getGeneric(Class<?> genericType, WorldType specific) {
				return net.minecraft.server.WorldType.getType(specific.getName());
			}
			
			@Override
			public WorldType getSpecific(Object generic) {
				net.minecraft.server.WorldType type = (net.minecraft.server.WorldType) generic;
				return WorldType.getByName(type.name());
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
		final Object worldServer = ((CraftWorld) world).getHandle();
		final Class<?> nmsEntityClass = net.minecraft.server.Entity.class;

		if (getEntity == null)
			getEntity = FuzzyReflection.fromObject(worldServer).getMethodByParameters(
					"getEntity", nmsEntityClass, new Class[] { int.class });
		
		return getIgnoreNull(new EquivalentConverter<Entity>() {
			
			@Override
			public Object getGeneric(Class<?> genericType, Entity specific) {
				// Simple enough
				return specific.getEntityId();
			}
			
			@Override
			public Entity getSpecific(Object generic) {
				try {
					net.minecraft.server.Entity nmsEntity = (net.minecraft.server.Entity) 
							getEntity.invoke(worldServer, generic);
					Integer id = (Integer) generic;
					
					// Attempt to get the Bukkit entity
					if (nmsEntity != null) {
						return nmsEntity.getBukkitEntity();
					} else {
						Server server = Bukkit.getServer();
						
						// Maybe it's a player that has just logged in? Try a search
						if (server != null) {
							for (Player player : server.getOnlinePlayers()) {
								if (player.getEntityId() == id) {
									return player;
								}
							}
						}
						
						return null;
					}
					
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("Incorrect arguments detected.", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Cannot read field due to a security limitation.", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Error occured in Minecraft method.", e.getCause());
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
				return toStackNMS(specific);
			}
			
			@Override
			public ItemStack getSpecific(Object generic) {
				return new CraftItemStack((net.minecraft.server.ItemStack) generic);
			}
			
			@Override
			public Class<ItemStack> getSpecificType() {
				return ItemStack.class;
			}
		});
	}
	
	/**
	 * Convert an item stack to the NMS equivalent.
	 * @param stack - Bukkit stack to convert.
	 * @return A bukkit stack.
	 */
	private static net.minecraft.server.ItemStack toStackNMS(ItemStack stack) {
		// We must be prepared for an object that simply implements ItemStcak
		if (stack instanceof CraftItemStack) {
			return ((CraftItemStack) stack).getHandle();
		} else {
			return (new CraftItemStack(stack)).getHandle();
		}
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
