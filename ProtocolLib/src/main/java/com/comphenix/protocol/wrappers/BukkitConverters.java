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
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.reflect.instances.DefaultInstances;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

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
	private static List<Unwrapper> unwrappers;
	
	// Used to access the world type
	private static Method worldTypeName;
	private static Method worldTypeGetType;
	
	// Used to get block instances
	private static MethodAccessor GET_BLOCK;
	private static MethodAccessor GET_BLOCK_ID;
	
	// Used for potion effect conversion
	private static volatile Constructor<?> mobEffectConstructor;
	private static volatile StructureModifier<Object> mobEffectModifier;
	
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
	 * Retrieve an equivalent converter for an array of generic items.
	 * <p>
	 * The array is wrapped in a list.
	 * @param genericItemType - the generic item type.
	 * @param itemConverter - an equivalent converter for the generic type.
	 * @return An equivalent converter.
	 */
	public static <T> EquivalentConverter<Iterable<? extends T>> getArrayConverter(
	  final Class<?> genericItemType, final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return new IgnoreNullConverter<Iterable<? extends T>>() {
				@Override
				protected List<T> getSpecificValue(Object generic) {
					if (generic instanceof Object[]) {
						ImmutableList.Builder<T> builder = ImmutableList.builder();

						// Copy everything to a new list
						for (Object item : (Object[]) generic) {
							T result = itemConverter.getSpecific(item);
							builder.add(result);
						}
						return builder.build();
					}
					
					// Not valid
					return null;
				}

				@Override
				protected Object getGenericValue(Class<?> genericType, Iterable<? extends T> specific) {
					List<T> list = Lists.newArrayList(specific);
					Object[] output = (Object[]) Array.newInstance(genericItemType, list.size());
					
					// Convert each object
					for (int i = 0; i < output.length; i++) {
						Object converted = itemConverter.getGeneric(genericItemType, list.get(i));
						output[i] = converted;
					}
					return output;
				}

				@SuppressWarnings("unchecked")
				@Override
				public Class<Iterable<? extends T>> getSpecificType() {
					// Damn you Java
					Class<?> dummy = Iterable.class;
					return (Class<Iterable<? extends T>>) dummy;
				}
			};
	}
	
	/**
	 * Retrieve a converter for wrapped attribute snapshots.
	 * @return Wrapped attribute snapshot converter.
	 */
	public static EquivalentConverter<WrappedGameProfile> getWrappedGameProfileConverter() {
		return new IgnoreNullConverter<WrappedGameProfile>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedGameProfile specific) {
				return specific.getHandle();
			}
			
			@Override
			protected WrappedGameProfile getSpecificValue(Object generic) {
				return WrappedGameProfile.fromHandle(generic);
			}
			
			@Override
			public Class<WrappedGameProfile> getSpecificType() {
				return WrappedGameProfile.class;
			}
		};
	}
	
	/**
	 * Retrieve a converter for wrapped chat components.
	 * @return Wrapped chat componetns.
	 */
	public static EquivalentConverter<WrappedChatComponent> getWrappedChatComponentConverter() {
		return new IgnoreNullConverter<WrappedChatComponent>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedChatComponent specific) {
				return specific.getHandle();
			}
			
			@Override
			protected WrappedChatComponent getSpecificValue(Object generic) {
				return WrappedChatComponent.fromHandle(generic);
			}
			
			@Override
			public Class<WrappedChatComponent> getSpecificType() {
				return WrappedChatComponent.class;
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
		
		final Class<?> worldType = MinecraftReflection.getWorldTypeClass();
		
		return new IgnoreNullConverter<WorldType>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WorldType specific) {
				try {
					// Deduce getType method by parameters alone
					if (worldTypeGetType == null) {
						worldTypeGetType = FuzzyReflection.fromClass(worldType).
								getMethodByParameters("getType", worldType, new Class<?>[] { String.class });
					}
					
					// Convert to the Bukkit world type
					return worldTypeGetType.invoke(this, specific.getName());
					
				} catch (Exception e) {
					throw new FieldAccessException("Cannot find the WorldType.getType() method.", e);
				}	
			}

			@Override
			protected WorldType getSpecificValue(Object generic) {
				try {
					if (worldTypeName == null) {
						try {
							worldTypeName = worldType.getMethod("name");
						} catch (Exception e) {
							// Assume the first method is the one
							worldTypeName = FuzzyReflection.fromClass(worldType).
								getMethodByParameters("name", String.class, new Class<?>[] {});
						}
					}
					
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
				return NbtFactory.fromNMS(generic, null);
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
	 * Retrieve the converter for the ServerPing packet in {@link PacketType.Status.Server#OUT_SERVER_INFO}.
	 * @return Server ping converter.
	 */
	public static EquivalentConverter<WrappedServerPing> getWrappedServerPingConverter() {
		return new IgnoreNullConverter<WrappedServerPing>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, WrappedServerPing specific) {
				return specific.getHandle();
			}
			
			@Override
			protected WrappedServerPing getSpecificValue(Object generic) {
				return WrappedServerPing.fromHandle(generic);
			}
			
			@Override
			public Class<WrappedServerPing> getSpecificType() {
				return WrappedServerPing.class;
			}
		};
	}
	
	/**
	 * Retrieve a converter for block instances.
	 * @return A converter for block instances.
	 */
	public static EquivalentConverter<Material> getBlockConverter() {
		// Initialize if we have't already
		if (GET_BLOCK == null || GET_BLOCK_ID == null) {
			Class<?> block = MinecraftReflection.getBlockClass();
			
			FuzzyMethodContract getIdContract = FuzzyMethodContract.newBuilder().
					parameterExactArray(block).
					requireModifier(Modifier.STATIC).
					build();
			FuzzyMethodContract getBlockContract = FuzzyMethodContract.newBuilder().
					parameterExactArray(int.class).
					requireModifier(Modifier.STATIC).
					build();
			GET_BLOCK = Accessors.getMethodAccessor(FuzzyReflection.fromClass(block).getMethod(getBlockContract));
			GET_BLOCK_ID = Accessors.getMethodAccessor(FuzzyReflection.fromClass(block).getMethod(getIdContract));
		}
		
		return new IgnoreNullConverter<Material>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, Material specific) {
				return GET_BLOCK.invoke(null, specific.getId());
			}
			
			@Override
			protected Material getSpecificValue(Object generic) {
				return Material.getMaterial((Integer) GET_BLOCK_ID.invoke(null, generic));
			}
			
			@Override
			public Class<Material> getSpecificType() {
				return Material.class;
			}
		};
	}
	
	/**
	 * Retrieve the converter used to convert between a PotionEffect and the equivalent NMS Mobeffect.
	 * @return The potion effect converter.
	 */
	public static EquivalentConverter<PotionEffect> getPotionEffectConverter() {		
		return new IgnoreNullConverter<PotionEffect>() {
			@Override
			protected Object getGenericValue(Class<?> genericType, PotionEffect specific) {
				// Locate the constructor
				if (mobEffectConstructor == null) {
					try {
						mobEffectConstructor = MinecraftReflection.getMobEffectClass().
								getConstructor(int.class, int.class, int.class, boolean.class);
					} catch (Exception e) {
						throw new RuntimeException("Cannot find mob effect constructor (int, int, int, boolean).", e);
					}
				}
				
				// Create the generic value
				try {
					return mobEffectConstructor.newInstance(
						specific.getType().getId(), specific.getDuration(), 
						specific.getAmplifier(), specific.isAmbient());
				} catch (Exception e) {
					throw new RuntimeException("Cannot construct MobEffect.", e);
				}
			}
			
			@Override
			protected PotionEffect getSpecificValue(Object generic) {
				if (mobEffectModifier == null) {
					mobEffectModifier = new StructureModifier<Object>(MinecraftReflection.getMobEffectClass(), false);
				}
				StructureModifier<Integer> ints = mobEffectModifier.withTarget(generic).withType(int.class);
				StructureModifier<Boolean> bools = mobEffectModifier.withTarget(generic).withType(boolean.class);
				
				return new PotionEffect(
					PotionEffectType.getById(ints.read(0)), 	/* effectId */
					ints.read(1),  								/* duration */
					ints.read(2), 								/* amplification */
					bools.read(1)								/* ambient */
				);
			}
			
			@Override
			public Class<PotionEffect> getSpecificType() {
				return PotionEffect.class;
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
	 * Retrieve an equivalent unwrapper for the converter.
	 * @param nativeType - the native NMS type the converter produces.
	 * @param converter - the converter.
	 * @return The equivalent unwrapper.
	 */
	public static Unwrapper asUnwrapper(final Class<?> nativeType, final EquivalentConverter<Object> converter) {
		return new Unwrapper() {
			@SuppressWarnings("rawtypes")
			@Override
			public Object unwrapItem(Object wrappedObject) {
				Class<?> type = PacketConstructor.getClass(wrappedObject);
				
				// Ensure the type is correct before we test
				if (converter.getSpecificType().isAssignableFrom(type)) {
					if (wrappedObject instanceof Class)
						return nativeType;
					else
						return converter.getGeneric((Class) nativeType, wrappedObject);
				}
				return null;
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
				put(WrappedWatchableObject.class, (EquivalentConverter) getWatchableObjectConverter()).
				put(PotionEffect.class, (EquivalentConverter) getPotionEffectConverter());
			
			// Types added in 1.7.2
			if (MinecraftReflection.isUsingNetty()) {
				builder.put(WrappedGameProfile.class, (EquivalentConverter) getWrappedGameProfileConverter());
				builder.put(WrappedChatComponent.class, (EquivalentConverter) getWrappedChatComponentConverter());	
				builder.put(WrappedServerPing.class, (EquivalentConverter) getWrappedServerPingConverter());
				
				for (Entry<Class<?>, EquivalentConverter<?>> entry : EnumWrappers.getFromWrapperMap().entrySet()) {
					builder.put((Class) entry.getKey(), (EquivalentConverter) entry.getValue());
				}
			}
			
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
				put(MinecraftReflection.getWatchableObjectClass(), (EquivalentConverter) getWatchableObjectConverter()).
				put(MinecraftReflection.getMobEffectClass(), (EquivalentConverter) getPotionEffectConverter());
				
			if (hasWorldType)
				builder.put(MinecraftReflection.getWorldTypeClass(), (EquivalentConverter) getWorldTypeConverter());
			if (hasAttributeSnapshot)
				builder.put(MinecraftReflection.getAttributeSnapshotClass(), (EquivalentConverter) getWrappedAttributeConverter());
			
			// Types added in 1.7.2
			if (MinecraftReflection.isUsingNetty()) {
				builder.put(MinecraftReflection.getGameProfileClass(), (EquivalentConverter) getWrappedGameProfileConverter());
				builder.put(MinecraftReflection.getIChatBaseComponentClass(), (EquivalentConverter) getWrappedChatComponentConverter());
				builder.put(MinecraftReflection.getServerPingClass(), (EquivalentConverter) getWrappedServerPingConverter());
				
				for (Entry<Class<?>, EquivalentConverter<?>> entry : EnumWrappers.getFromNativeMap().entrySet()) {
					builder.put((Class) entry.getKey(), (EquivalentConverter) entry.getValue());
				}
			}
			genericConverters = builder.build();
		}
		return genericConverters;
	}
	
	/**
	 * Retrieve every NMS <-> Bukkit converter as unwrappers.
	 * @return Every unwrapper.
	 */
	public static List<Unwrapper> getUnwrappers() {
		if (unwrappers == null) {
			ImmutableList.Builder<Unwrapper> builder = ImmutableList.builder();
			
			for (Map.Entry<Class<?>, EquivalentConverter<Object>> entry : getConvertersForGeneric().entrySet()) {
				builder.add(asUnwrapper(entry.getKey(), entry.getValue()));
			}
			unwrappers = builder.build();
		}
		return unwrappers;
	}
}
