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

package com.comphenix.protocol.wrappers;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * Contains several useful equivalent converters for normal Bukkit types.
 * 
 * @author Kristian
 */
public class BukkitConverters {
	// Check whether or not certain classes exists
	private static boolean hasWorldType = false;
	private static boolean hasAttributeSnapshot = false;
	
	// The static maps
	private static Map<Class<?>, EquivalentConverter<Object>> specificConverters;
	private static Map<Class<?>, EquivalentConverter<Object>> genericConverters;
	
	// Used to access the world type
	private static Method worldTypeName;
	private static Method worldTypeGetType;
	
	static {
		try {
			MinecraftReflection.getWorldTypeClass();
			hasWorldType = true;
		} catch (Exception e) {
		}
		
		try {
			MinecraftReflection.getAttributeSnapshotClass();
			hasAttributeSnapshot = true;
		} catch (Exception e) {
		}
	}
	
	/**
	 * Represents a typical equivalence converter.
	 * 
	 * @author Kristian
	 * @param <T> - type that can be converted.
	 */
	private static abstract class IgnoreNullConverter<TType> implements EquivalentConverter<TType> {
		public final Object getGeneric(Class<?> genericType, TType specific) {
			if (specific != null)
				return getGenericValue(genericType, specific);
			else
				return null;
		}
		
		/**
		 * Retrieve a copy of the actual generic value.
		 * @param genericType - generic type.
		 * @param specific - the specific type-
		 * @return A copy of the specific type.
		 */
		protected abstract Object getGenericValue(Class<?> genericType, TType specific);
		
		@Override
		public final TType getSpecific(Object generic) {
			if (generic != null)
				return getSpecificValue(generic);
			else
				return null;
		}
		
		/**
		 * Retrieve a copy of the specific type using an instance of the generic type.
		 * @param generic - generic type.
		 * @return A copy of the specific type.
		 */
		protected abstract TType getSpecificValue(Object generic);
		
		@Override
		public boolean equals(Object obj) {
			// Very short
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			
			// See if they're equivalent
			if (obj instanceof EquivalentConverter) {
				@SuppressWarnings("rawtypes")
				EquivalentConverter other = (EquivalentConverter) obj;
				return Objects.equal(this.getSpecificType(), other.getSpecificType());
			}
			return false;
		}
	}
	
	/**
	 * Represents a converter that is only valid in a given world.
	 * 
	 * @author Kristian
	 * @param <TType> - instance types it converts.
	 */
	private static abstract class WorldSpecificConverter<TType> extends IgnoreNullConverter<TType> {
		protected World world;

		/**
		 * Initialize a new world-specificn converter.
		 * @param world - the given world.
		 */
		public WorldSpecificConverter(World world) {
			super();
			this.world = world;
		}

		@Override
		public boolean equals(Object obj) {
			// More shortcuts
			if (obj == this)
				return true;
			if (obj == null)
				return false;
			
			// Add another constraint
			if (obj instanceof WorldSpecificConverter && super.equals(obj)) {
				@SuppressWarnings("rawtypes")
				WorldSpecificConverter other = (WorldSpecificConverter) obj;
				
				return Objects.equal(world, other.world);
			}
			return false;
		}
	}
	
	/**
	 * Retrieve an equivalent converter for a list of generic items.
	 * @param genericItemType - the generic item type.
	 * @param itemConverter - an equivalent converter for the generic type.
	 * @return An equivalent converter.
	 */
	public static <T> EquivalentConverter<List<T>> getListConverter(final Class<?> genericItemType, final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return new IgnoreNullConverter<List<T>>() {
				@SuppressWarnings("unchecked")
				@Override
				protected List<T> getSpecificValue(Object generic) {
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
				protected Object getGenericValue(Class<?> genericType, List<T> specific) {
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
			};
	}
	
	/**
	 * Retrieve a converter for wrapped attribute snapshots.
	 * @return Wrapped attribute snapshot converter.
	 */
	public static EquivalentConverter<WrappedAttribute> getWrappedAttributeConverter() {
		return new IgnoreNullConverter<WrappedAttribute>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedAttribute specific) {
				return specific.getHandle();
			}
			
			@Override
			protected WrappedAttribute getSpecificValue(Object generic) {
				return WrappedAttribute.fromHandle(generic);
			}
			
			@Override
			public Class<WrappedAttribute> getSpecificType() {
				return WrappedAttribute.class;
			}
		};
	}
	
	/**
	 * Retrieve a converter for watchable objects and the respective wrapper.
	 * @return A watchable object converter.
	 */
	public static EquivalentConverter<WrappedWatchableObject> getWatchableObjectConverter() {
		return new IgnoreNullConverter<WrappedWatchableObject>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedWatchableObject specific) {
				return specific.getHandle();
			}
			
			protected WrappedWatchableObject getSpecificValue(Object generic) {
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
		};
	}
	
	/**
	 * Retrieve a converter for the NMS DataWatcher class and our wrapper.
	 * @return A DataWatcher converter.
	 */
	public static EquivalentConverter<WrappedDataWatcher> getDataWatcherConverter() {
		return new IgnoreNullConverter<WrappedDataWatcher>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedDataWatcher specific) {
				return specific.getHandle();
			}
			
			@Override
			protected WrappedDataWatcher getSpecificValue(Object generic) {
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
		};
	}
	
	/**
	 * Retrieve a converter for Bukkit's world type enum and the NMS equivalent.
	 * @return A world type enum converter.
	 */
	public static EquivalentConverter<WorldType> getWorldTypeConverter() {
		// Check that we can actually use this converter
		if (!hasWorldType)
			return null;
		
		return new IgnoreNullConverter<WorldType>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WorldType specific) {
				try {
					if (worldTypeGetType == null)
						worldTypeGetType = MinecraftReflection.getWorldTypeClass().getMethod("getType", String.class);
					
					// Convert to the Bukkit world type
					return worldTypeGetType.invoke(this, specific.getName());
					
				} catch (Exception e) {
					throw new FieldAccessException("Cannot find the WorldType.getType() method.", e);
				}	
			}

			@Override
			protected WorldType getSpecificValue(Object generic) {
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
		};
	}
	
	/**
	 * Retrieve an equivalent converter for net.minecraft.server NBT classes and their wrappers.
	 * @return An equivalent converter for NBT.
	 */
	public static EquivalentConverter<NbtBase<?>> getNbtConverter() {
		return new IgnoreNullConverter<NbtBase<?>>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, NbtBase<?> specific) {
				return NbtFactory.fromBase(specific).getHandle();
			}
			
			@Override
			protected NbtBase<?> getSpecificValue(Object generic) {
				return NbtFactory.fromNMS(generic);
			}
			
			@Override
			@SuppressWarnings("unchecked")
			public Class<NbtBase<?>> getSpecificType() {
				// Damn you Java AGAIN
				Class<?> dummy = NbtBase.class;
				return (Class<NbtBase<?>>) dummy;
			}
		};
	}
	
	/**
	 * Retrieve a converter for NMS entities and Bukkit entities.
	 * @param world - the current world.
	 * @return A converter between the underlying NMS entity and Bukkit's wrapper.
	 */
	public static EquivalentConverter<Entity> getEntityConverter(World world) {
		final WeakReference<ProtocolManager> managerRef = 
				new WeakReference<ProtocolManager>(ProtocolLibrary.getProtocolManager());

		return new WorldSpecificConverter<Entity>(world) {
			@Override
			public Object getGenericValue(Class<?> genericType, Entity specific) {
				// Simple enough
				return specific.getEntityId();
			}
			
			@Override
			public Entity getSpecificValue(Object generic) {
				try {
					Integer id = (Integer) generic;
					ProtocolManager manager = managerRef.get();
					
					// Use the entity ID to get a reference to the entity
					if (id != null && manager != null) {
						return manager.getEntityFromID(world, id);
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
		};
	}
	
	/**
	 * Retrieve the converter used to convert NMS ItemStacks to Bukkit's ItemStack.
	 * @return Item stack converter.
	 */
	public static EquivalentConverter<ItemStack> getItemStackConverter() {
		return new IgnoreNullConverter<ItemStack>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, ItemStack specific) {
				return MinecraftReflection.getMinecraftItemStack(specific);
			}
			
			@Override
			protected ItemStack getSpecificValue(Object generic) {
				return MinecraftReflection.getBukkitItemStack(generic);
			}
			
			@Override
			public Class<ItemStack> getSpecificType() {
				return ItemStack.class;
			}
		};
	}
	
	/**
	 * Wraps a given equivalent converter in NULL checks, ensuring that such values are ignored.
	 * @param delegate - the underlying equivalent converter.
	 * @return A equivalent converter that ignores NULL values.
	 */
	public static <TType> EquivalentConverter<TType> getIgnoreNull(final EquivalentConverter<TType> delegate) {
		// Automatically wrap all parameters to the delegate with a NULL check
		return new IgnoreNullConverter<TType>() {
			@Override
			public Object getGenericValue(Class<?> genericType, TType specific) {
				return delegate.getGeneric(genericType, specific);
			}
			
			@Override
			public TType getSpecificValue(Object generic) {
				return delegate.getSpecific(generic);
			}
			
			@Override
			public Class<TType> getSpecificType() {
				return delegate.getSpecificType();
			}
		};
	}
	
	/**
	 * Retrieve every converter that is associated with a specific class.
	 * @return Every converter with a unique specific class.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Map<Class<?>, EquivalentConverter<Object>> getConvertersForSpecific() {
		if (specificConverters == null) {
			// Generics doesn't work, as usual
			ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder = 
				   ImmutableMap.<Class<?>, EquivalentConverter<Object>>builder().
				put(WrappedDataWatcher.class, (EquivalentConverter) getDataWatcherConverter()).
				put(ItemStack.class, (EquivalentConverter) getItemStackConverter()).
				put(NbtBase.class, (EquivalentConverter) getNbtConverter()).
				put(NbtCompound.class, (EquivalentConverter) getNbtConverter()).
				put(WrappedWatchableObject.class, (EquivalentConverter) getWatchableObjectConverter());
			
			if (hasWorldType) 
				builder.put(WorldType.class, (EquivalentConverter) getWorldTypeConverter());
			if (hasAttributeSnapshot)
				builder.put(WrappedAttribute.class, (EquivalentConverter) getWrappedAttributeConverter());
			specificConverters = builder.build();
		}
		return specificConverters;
	}
	
	/**
	 * Retrieve every converter that is associated with a generic class.
	 * @return Every converter with a unique generic class.
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static Map<Class<?>, EquivalentConverter<Object>> getConvertersForGeneric() {
		if (genericConverters == null) {
			// Generics doesn't work, as usual
			ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder =
				   ImmutableMap.<Class<?>, EquivalentConverter<Object>>builder().
				put(MinecraftReflection.getDataWatcherClass(), (EquivalentConverter) getDataWatcherConverter()).
				put(MinecraftReflection.getItemStackClass(), (EquivalentConverter) getItemStackConverter()).
				put(MinecraftReflection.getNBTBaseClass(), (EquivalentConverter) getNbtConverter()).
				put(MinecraftReflection.getNBTCompoundClass(), (EquivalentConverter) getNbtConverter()).
				put(MinecraftReflection.getWatchableObjectClass(), (EquivalentConverter) getWatchableObjectConverter());
			
			if (hasWorldType)
				builder.put(MinecraftReflection.getWorldTypeClass(), (EquivalentConverter) getWorldTypeConverter());
			if (hasAttributeSnapshot)
				builder.put(MinecraftReflection.getAttributeSnapshotClass(), (EquivalentConverter) getWrappedAttributeConverter());
			genericConverters = builder.build();
		}
		return genericConverters;
	}
}
