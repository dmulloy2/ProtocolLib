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
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import static com.comphenix.protocol.utility.MinecraftReflection.*;
import static com.comphenix.protocol.wrappers.Converters.*;

/**
 * Contains several useful equivalent converters for normal Bukkit types.
 * 
 * @author Kristian
 */
@SuppressWarnings("unchecked")
public class BukkitConverters {
	// Check whether or not certain classes exists
	private static boolean hasWorldType = false;
	private static boolean hasAttributeSnapshot = false;
	
	// The static maps
	private static Map<Class<?>, EquivalentConverter<Object>> genericConverters;
	private static List<Unwrapper> unwrappers;
	
	// Used to access the world type
	private static Method worldTypeName;
	private static Method worldTypeGetType;

	// Used for potion effect conversion
	private static volatile Constructor<?> mobEffectConstructor;
	private static volatile StructureModifier<Object> mobEffectModifier;
	
	// Used for fetching the CraftWorld associated with a WorldServer
	private static FieldAccessor craftWorldField;
	
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
		
		// Fetch CraftWorld field
		try {
			craftWorldField = Accessors.getFieldAccessor(
				MinecraftReflection.getNmsWorldClass(),
				MinecraftReflection.getCraftWorldClass(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Represents a typical equivalence converter.
	 * 
	 * @author Kristian
	 * @param <TType> - type that can be converted.
	 * @deprecated Replaced by {@link Converters#ignoreNull(EquivalentConverter)}
	 */
	@Deprecated
	public static abstract class IgnoreNullConverter<TType> implements EquivalentConverter<TType> {
		@Override
		public final Object getGeneric(TType specific) {
			if (specific != null)
				return getGenericValue(specific);
			else
				return null;
		}
		
		/**
		 * Retrieve a copy of the actual generic value.
		 * @param specific - the specific type-
		 * @return A copy of the specific type.
		 */
		public abstract Object getGenericValue(TType specific);
		
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
		public abstract TType getSpecificValue(Object generic);
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (obj instanceof EquivalentConverter) {
				EquivalentConverter<?> that = (EquivalentConverter<?>) obj;
				return Objects.equal(this.getSpecificType(), that.getSpecificType());
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.getSpecificType());
		}
	}
	
	/**
	 * Represents a converter that is only valid in a given world.
	 * 
	 * @author Kristian
	 * @param <TType> - instance types it converts.
	 */
	private static abstract class WorldSpecificConverter<TType> implements EquivalentConverter<TType> {
		protected World world;

		/**
		 * Initialize a new world-specificn converter.
		 * @param world - the given world.
		 */
		WorldSpecificConverter(World world) {
			super();
			this.world = world;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;

			// Add another constraint
			if (obj instanceof WorldSpecificConverter && super.equals(obj)) {
				WorldSpecificConverter<?> that = (WorldSpecificConverter<?>) obj;
				return Objects.equal(this.world, that.world);
			}

			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.getSpecificType(), this.world);
		}
	}

	public static <K, V> EquivalentConverter<Map<K, V>> getMapConverter(EquivalentConverter<K> keyConverter,
	                                                                        EquivalentConverter<V> valConverter) {
		return new EquivalentConverter<Map<K, V>>() {
			@Override
			public Map<K, V> getSpecific(Object generic) {
				Map<Object, Object> genericMap = (Map<Object, Object>) generic;
				Map<K, V> newMap;

				try {
					newMap = (Map<K, V>) genericMap.getClass().newInstance();
				} catch (ReflectiveOperationException ex) {
					newMap = new HashMap<>();
				}

				for (Map.Entry<Object, Object> entry : genericMap.entrySet()) {
					newMap.put(keyConverter.getSpecific(entry.getKey()), valConverter.getSpecific(entry.getValue()));
				}

				return newMap;
			}

			@Override
			public Object getGeneric(Map<K, V> specific) {
				Map<Object, Object> newMap;

				try {
					newMap = specific.getClass().newInstance();
				} catch (ReflectiveOperationException ex) {
					newMap = new HashMap<>();
				}

				for (Map.Entry<K, V> entry : specific.entrySet()) {
					newMap.put(keyConverter.getGeneric(entry.getKey()), valConverter.getGeneric(entry.getValue()));
				}

				return newMap;
			}

			@Override
			public Class<Map<K, V>> getSpecificType() {
				return null;
			}
		};
	}

	/**
	 * Retrieve an equivalent converter for a list of generic items.
	 * @param <T> Type
	 * @param itemConverter - an equivalent converter for the generic type.
	 * @return An equivalent converter.
	 */
	public static <T> EquivalentConverter<List<T>> getListConverter(final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return ignoreNull(new EquivalentConverter<List<T>>() {
			@Override
			public List<T> getSpecific(Object generic) {
				if (generic instanceof Collection) {
					List<T> items = new ArrayList<>();

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

			@Override
			public Object getGeneric(List<T> specific) {
				List<Object> newList;

				try {
					newList = (List<Object>) specific.getClass().newInstance();
				} catch (ReflectiveOperationException ex) {
					newList = new ArrayList<>();
				}

				// Convert each object
				for (T position : specific) {
					if (position != null) {
						Object converted = itemConverter.getGeneric(position);
						if (converted != null) {
							newList.add(converted);
						}
					} else {
						newList.add(null);
					}
				}

				return newList;
			}

			@Override
			public Class<List<T>> getSpecificType() {
				// Damn you Java
				Class<?> dummy = List.class;
				return (Class<List<T>>) dummy;
			}
		});
	}

	/**
	 * @deprecated While this solution is not as abhorrent as I had imagined, I still highly recommend switching to the
	 * new conversion API.
	 */
	@Deprecated
	public static <T> EquivalentConverter<Set<T>> getSetConverter(final Class<?> genericType,
	                                                              final EquivalentConverter<T> itemConverter) {
		if (itemConverter instanceof EnumWrappers.EnumConverter) {
			((EnumWrappers.EnumConverter) itemConverter).setGenericType(genericType);
		}

		return getSetConverter(itemConverter);
	}

	/**
	 * Retrieve an equivalent converter for a set of generic items.
	 * @param <T> Element type
	 * @param itemConverter - an equivalent converter for the generic type.
	 * @return An equivalent converter.
	 */
	@SuppressWarnings("unchecked")
	public static <T> EquivalentConverter<Set<T>> getSetConverter(final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return ignoreNull(new EquivalentConverter<Set<T>>() {

			@Override
			public Set<T> getSpecific(Object generic) {
				if (generic instanceof Collection) {
					Set<T> items = new HashSet<>();

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

			@Override
			public Object getGeneric(Set<T> specific) {
				Set<Object> newList;

				try {
					newList = (Set<Object>) specific.getClass().newInstance();
				} catch (ReflectiveOperationException ex) {
					newList = new HashSet<>();
				}

				// Convert each object
				for (T position : specific) {
					if (position != null) {
						Object converted = itemConverter.getGeneric(position);
						if (converted != null) {
							newList.add(converted);
						}
					} else {
						newList.add(null);
					}
				}

				return newList;
			}

			@Override
			public Class<Set<T>> getSpecificType() {
				// Damn you Java
				Class<?> dummy = Set.class;
				return (Class<Set<T>>) dummy;
			}
		});
	}

	/**
	 * Retrieve an equivalent converter for an array of generic items.
	 * @param <T> Type
	 * <p>
	 * The array is wrapped in a list.
	 * @param genericItemType - the generic item type.
	 * @param itemConverter - an equivalent converter for the generic type.
	 * @return An equivalent converter.
	 */
	public static <T> EquivalentConverter<Iterable<? extends T>> getArrayConverter(final Class<?> genericItemType,
	                                                                               final EquivalentConverter<T> itemConverter) {
		// Convert to and from the wrapper
		return ignoreNull(new EquivalentConverter<Iterable<? extends T>>() {
			@Override
			public List<T> getSpecific(Object generic) {
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
			public Object getGeneric(Iterable<? extends T> specific) {
				List<T> list = Lists.newArrayList(specific);
				Object[] output = (Object[]) Array.newInstance(genericItemType, list.size());

				// Convert each object
				for (int i = 0; i < output.length; i++) {
					Object converted = itemConverter.getGeneric(list.get(i));
					output[i] = converted;
				}
				return output;
			}

			@Override
			public Class<Iterable<? extends T>> getSpecificType() {
				// Damn you Java
				Class<?> dummy = Iterable.class;
				return (Class<Iterable<? extends T>>) dummy;
			}
		});
	}
	
	/**
	 * Retrieve a converter for wrapped game profiles.
	 * @return Wrapped game profile converter.
	 */
	public static EquivalentConverter<WrappedGameProfile> getWrappedGameProfileConverter() {
		return ignoreNull(handle(WrappedGameProfile::getHandle, WrappedGameProfile::fromHandle));
	}
	
	/**
	 * Retrieve a converter for wrapped chat components.
	 * @return Wrapped chat component.
	 */
	public static EquivalentConverter<WrappedChatComponent> getWrappedChatComponentConverter() {
		return ignoreNull(handle(WrappedChatComponent::getHandle, WrappedChatComponent::fromHandle));
	}
	
	/**
	 * Retrieve a converter for wrapped block data.
	 * @return Wrapped block data.
	 */
	public static EquivalentConverter<WrappedBlockData> getWrappedBlockDataConverter() {
		return ignoreNull(handle(WrappedBlockData::getHandle, WrappedBlockData::fromHandle));
	}
	
	/**
	 * Retrieve a converter for wrapped attribute snapshots.
	 * @return Wrapped attribute snapshot converter.
	 */
	public static EquivalentConverter<WrappedAttribute> getWrappedAttributeConverter() {
		return ignoreNull(handle(WrappedAttribute::getHandle, WrappedAttribute::fromHandle));
	}
	
	/**
	 * Retrieve a converter for watchable objects and the respective wrapper.
	 * @return A watchable object converter.
	 */
	public static EquivalentConverter<WrappedWatchableObject> getWatchableObjectConverter() {
		return ignoreNull(new EquivalentConverter<WrappedWatchableObject>() {
			@Override
			public Object getGeneric(WrappedWatchableObject specific) {
				return specific.getHandle();
			}

			@Override
			public WrappedWatchableObject getSpecific(Object generic) {
				if (MinecraftReflection.isWatchableObject(generic))
					return new WrappedWatchableObject(generic);
				else if (generic instanceof WrappedWatchableObject)
					return (WrappedWatchableObject) generic;
				else
					throw new IllegalArgumentException("Unrecognized type " + generic.getClass());
			}

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
		return ignoreNull(new EquivalentConverter<WrappedDataWatcher>() {
			@Override
			public Object getGeneric(WrappedDataWatcher specific) {
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

		final Class<?> worldType = MinecraftReflection.getWorldTypeClass();

		return ignoreNull(new EquivalentConverter<WorldType>() {
			@Override
			public Object getGeneric(WorldType specific) {
				try {
					// Deduce getType method by parameters alone
					if (worldTypeGetType == null) {
						worldTypeGetType = FuzzyReflection.fromClass(worldType).
								getMethodByParameters("getType", worldType, new Class<?>[]{String.class});
					}

					// Convert to the Bukkit world type
					return worldTypeGetType.invoke(this, specific.getName());

				} catch (Exception e) {
					throw new FieldAccessException("Cannot find the WorldType.getType() method.", e);
				}
			}

			@Override
			public WorldType getSpecific(Object generic) {
				try {
					if (worldTypeName == null) {
						try {
							worldTypeName = worldType.getMethod("name");
						} catch (Exception e) {
							// Assume the first method is the one
							worldTypeName = FuzzyReflection.fromClass(worldType).
									getMethodByParameters("name", String.class, new Class<?>[]{});
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
		});
	}
	
	/**
	 * Retrieve an equivalent converter for net.minecraft.server NBT classes and their wrappers.
	 * @return An equivalent converter for NBT.
	 */
	public static EquivalentConverter<NbtBase<?>> getNbtConverter() {
		return ignoreNull(new EquivalentConverter<NbtBase<?>>() {
			@Override
			public Object getGeneric(NbtBase<?> specific) {
				return NbtFactory.fromBase(specific).getHandle();
			}

			@Override
			public NbtBase<?> getSpecific(Object generic) {
				return NbtFactory.fromNMS(generic, null);
			}

			@Override
			@SuppressWarnings("unchecked")
			public Class<NbtBase<?>> getSpecificType() {
				// Damn you Java AGAIN
				Class<?> dummy = NbtBase.class;
				return (Class<NbtBase<?>>) dummy;
			}
		});
	}
	
	/**
	 * Retrieve a converter for NMS entities and Bukkit entities.
	 * @param world - the current world.
	 * @return A converter between the underlying NMS entity and Bukkit's wrapper.
	 */
	public static EquivalentConverter<Entity> getEntityConverter(World world) {
		final WeakReference<ProtocolManager> managerRef = new WeakReference<>(ProtocolLibrary.getProtocolManager());

		return new WorldSpecificConverter<Entity>(world) {
			@Override
			public Object getGeneric(Entity specific) {
				// Simple enough
				return specific.getEntityId();
			}
			
			@Override
			public Entity getSpecific(Object generic) {
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
		return new EquivalentConverter<ItemStack>() {
			@Override
			public ItemStack getSpecific(Object generic) {
				return MinecraftReflection.getBukkitItemStack(generic);
			}

			@Override
			public Object getGeneric(ItemStack specific) {
				return MinecraftReflection.getMinecraftItemStack(specific);
			}

			@Override
			public Class<ItemStack> getSpecificType() {
				return ItemStack.class;
			}
		};
	}

	/**
	 * Retrieve the converter for the ServerPing packet in {@link PacketType.Status.Server#SERVER_INFO}.
	 * @return Server ping converter.
	 */
	public static EquivalentConverter<WrappedServerPing> getWrappedServerPingConverter() {
		return ignoreNull(handle(WrappedServerPing::getHandle, WrappedServerPing::fromHandle));
	}
	
	/**
	 * Retrieve the converter for a statistic.
	 * @return Statistic converter.
	 */
	public static EquivalentConverter<WrappedStatistic> getWrappedStatisticConverter() {
		return ignoreNull(handle(WrappedStatistic::getHandle, WrappedStatistic::fromHandle));
	}

	private static MethodAccessor BLOCK_FROM_MATERIAL;
	private static MethodAccessor MATERIAL_FROM_BLOCK;

	/**
	 * Retrieve a converter for block instances.
	 * @return A converter for block instances.
	 */
	public static EquivalentConverter<Material> getBlockConverter() {
		if (BLOCK_FROM_MATERIAL == null || MATERIAL_FROM_BLOCK == null) {
			Class<?> magicNumbers = MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers");
			Class<?> block = MinecraftReflection.getBlockClass();

			FuzzyReflection fuzzy = FuzzyReflection.fromClass(magicNumbers);
			FuzzyMethodContract.Builder builder = FuzzyMethodContract
					.newBuilder()
					.requireModifier(Modifier.STATIC)
					.returnTypeExact(Material.class)
					.parameterExactArray(block);
			MATERIAL_FROM_BLOCK = Accessors.getMethodAccessor(fuzzy.getMethod(builder.build()));

			builder = FuzzyMethodContract
					.newBuilder()
					.requireModifier(Modifier.STATIC)
					.returnTypeExact(block)
					.parameterExactArray(Material.class);
			BLOCK_FROM_MATERIAL = Accessors.getMethodAccessor(fuzzy.getMethod(builder.build()));
		}

		return ignoreNull(new EquivalentConverter<Material>() {
			@Override
			public Object getGeneric(Material specific) {
				return BLOCK_FROM_MATERIAL.invoke(null, specific);
			}

			@Override
			public Material getSpecific(Object generic) {
				return (Material) MATERIAL_FROM_BLOCK.invoke(null, generic);
			}

			@Override
			public Class<Material> getSpecificType() {
				return Material.class;
			}
		});
	}

	/**
	 * Retrieve the converter used to convert between a NMS World and a Bukkit world.
	 * @return The world converter.
	 */
	public static EquivalentConverter<World> getWorldConverter() {
		return ignoreNull(new EquivalentConverter<World>() {
			@Override
			public Object getGeneric(World specific) {
				return BukkitUnwrapper.getInstance().unwrapItem(specific);
			}

			@Override
			public World getSpecific(Object generic) {
				return (World) craftWorldField.get(generic);
			}

			@Override
			public Class<World> getSpecificType() {
				return World.class;
			}
		});
	}
	
	/**
	 * Retrieve the converter used to convert between a PotionEffect and the equivalent NMS Mobeffect.
	 * @return The potion effect converter.
	 */
	public static EquivalentConverter<PotionEffect> getPotionEffectConverter() {
		return ignoreNull(new EquivalentConverter<PotionEffect>() {
			@Override
			public Object getGeneric(PotionEffect specific) {
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
			public PotionEffect getSpecific(Object generic) {
				if (mobEffectModifier == null) {
					mobEffectModifier = new StructureModifier<>(MinecraftReflection.getMobEffectClass(), false);
				}
				StructureModifier<Integer> ints = mobEffectModifier.withTarget(generic).withType(int.class);
				StructureModifier<Boolean> bools = mobEffectModifier.withTarget(generic).withType(boolean.class);

				return new PotionEffect(
						PotionEffectType.getById(ints.read(0)), 	/* effectId */
						ints.read(1),  							/* duration */
						ints.read(2), 								/* amplification */
						bools.read(1)								/* ambient */
				);
			}

			@Override
			public Class<PotionEffect> getSpecificType() {
				return PotionEffect.class;
			}
		});
	}

	private static Constructor<?> vec3dConstructor;
	private static StructureModifier<Object> vec3dModifier;

	/**
	 * Retrieve the converter used to convert between a Vector and the equivalent NMS Vec3d.
	 * @return The Vector converter.
	 */
	public static EquivalentConverter<Vector> getVectorConverter() {
		return ignoreNull(new EquivalentConverter<Vector>() {

			@Override
			public Class<Vector> getSpecificType() {
				return Vector.class;
			}

			@Override
			public Object getGeneric(Vector specific) {
				if (vec3dConstructor == null) {
					try {
						vec3dConstructor = MinecraftReflection.getVec3DClass().getConstructor(
								double.class, double.class, double.class);
					} catch (Throwable ex) {
						throw new RuntimeException("Could not find Vec3d constructor (double, double, double)");
					}
				}

				try {
					return vec3dConstructor.newInstance(specific.getX(), specific.getY(), specific.getZ());
				} catch (Throwable ex) {
					throw new RuntimeException("Could not construct Vec3d.", ex);
				}
			}

			@Override
			public Vector getSpecific(Object generic) {
				if (vec3dModifier == null) {
					vec3dModifier = new StructureModifier<>(MinecraftReflection.getVec3DClass(), false);
				}

				StructureModifier<Double> doubles = vec3dModifier.withTarget(generic).withType(double.class);
				return new Vector(
						doubles.read(0), /* x */
						doubles.read(1), /* y */
						doubles.read(2)  /* z */
				);
			}

		});
	}

	private static MethodAccessor getSound = null;
	private static MethodAccessor getSoundEffect = null;
	private static FieldAccessor soundKey = null;

	private static Map<String, Sound> soundIndex = null;

	public static EquivalentConverter<Sound> getSoundConverter() {
		if (getSound == null || getSoundEffect == null) {
			Class<?> craftSound = MinecraftReflection.getCraftSoundClass();
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(craftSound, true);
			getSound = Accessors.getMethodAccessor(
					fuzzy.getMethodByParameters("getSound", String.class, new Class<?>[]{Sound.class}));
			getSoundEffect = Accessors.getMethodAccessor(fuzzy.getMethodByParameters("getSoundEffect",
					MinecraftReflection.getSoundEffectClass(), new Class<?>[]{String.class}));
		}

		return ignoreNull(new EquivalentConverter<Sound>() {

			@Override
			public Class<Sound> getSpecificType() {
				return Sound.class;
			}

			@Override
			public Object getGeneric(Sound specific) {
				// Getting the SoundEffect is easy, Bukkit provides us the methods
				String key = (String) getSound.invoke(null, specific);
				return getSoundEffect.invoke(null, key);
			}

			@Override
			public Sound getSpecific(Object generic) {
				// Getting the Sound is a bit more complicated...
				if (soundKey == null) {
					Class<?> soundEffect = generic.getClass();
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(soundEffect, true);
					soundKey = Accessors.getFieldAccessor(
							fuzzy.getFieldByType("key", MinecraftReflection.getMinecraftKeyClass()));
				}

				MinecraftKey minecraftKey = MinecraftKey.fromHandle(soundKey.get(generic));
				String key = minecraftKey.getKey();

				// Use our index if it already exists
				if (soundIndex != null) {
					return soundIndex.get(key);
				}

				// If it doesn't, try to guess the enum name
				try {
					return Sound.valueOf(minecraftKey.getEnumFormat());
				} catch (IllegalArgumentException ignored) {
				}

				// Worst case we index all the sounds and use it later
				soundIndex = new ConcurrentHashMap<>();
				for (Sound sound : Sound.values()) {
					String index = (String) getSound.invoke(null, sound);
					soundIndex.put(index, sound);
				}

				return soundIndex.get(key);
			}
		});
	}

	public static EquivalentConverter<Advancement> getAdvancementConverter() {
		return ignoreNull(new EquivalentConverter<Advancement>() {
			@Override
			public Advancement getSpecific(Object generic) {
				try {
					return (Advancement) getCraftBukkitClass("advancement.CraftAdvancement")
							.getConstructor(getMinecraftClass("Advancement"))
							.newInstance(generic);
				} catch (ReflectiveOperationException ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public Object getGeneric(Advancement specific) {
				return BukkitUnwrapper.getInstance().unwrapItem(specific);
			}

			@Override
			public Class<Advancement> getSpecificType() {
				return Advancement.class;
			}
		});
	}

	/**
	 * Retrieve an equivalent unwrapper for the converter.
	 * @param nativeType - the native NMS type the converter produces.
	 * @param converter - the converter.
	 * @return The equivalent unwrapper.
	 */
	public static Unwrapper asUnwrapper(final Class<?> nativeType, final EquivalentConverter<Object> converter) {
		return wrappedObject -> {
			Class<?> type = PacketConstructor.getClass(wrappedObject);

			// Ensure the type is correct before we test
			if (converter.getSpecificType().isAssignableFrom(type)) {
				if (wrappedObject instanceof Class)
					return nativeType;
				else
					return converter.getGeneric(wrappedObject);
			}
			return null;
		};
	}

	/**
	 * Retrieve every converter that is associated with a generic class.
	 * @return Every converter with a unique generic class.
	 */
	@SuppressWarnings("rawtypes")
	// TODO this list needs to be updated
	public static Map<Class<?>, EquivalentConverter<Object>> getConvertersForGeneric() {
		if (genericConverters == null) {
			// Generics doesn't work, as usual
			ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder =
				   ImmutableMap.<Class<?>, EquivalentConverter<Object>>builder().
				put(MinecraftReflection.getDataWatcherClass(), (EquivalentConverter) getDataWatcherConverter()).
				put(MinecraftReflection.getItemStackClass(), (EquivalentConverter) getItemStackConverter()).
				put(MinecraftReflection.getNBTBaseClass(), (EquivalentConverter) getNbtConverter()).
				put(MinecraftReflection.getNBTCompoundClass(), (EquivalentConverter) getNbtConverter()).
				put(MinecraftReflection.getDataWatcherItemClass(), (EquivalentConverter) getWatchableObjectConverter()).
				put(MinecraftReflection.getMobEffectClass(), (EquivalentConverter) getPotionEffectConverter()).
				put(MinecraftReflection.getNmsWorldClass(), (EquivalentConverter) getWorldConverter());
				
			if (hasWorldType)
				builder.put(MinecraftReflection.getWorldTypeClass(), (EquivalentConverter) getWorldTypeConverter());
			if (hasAttributeSnapshot)
				builder.put(MinecraftReflection.getAttributeSnapshotClass(), (EquivalentConverter) getWrappedAttributeConverter());
			
			// Types added in 1.7.2
			if (MinecraftReflection.isUsingNetty()) {
				builder.put(MinecraftReflection.getBlockClass(), (EquivalentConverter) getBlockConverter());
				builder.put(MinecraftReflection.getGameProfileClass(), (EquivalentConverter) getWrappedGameProfileConverter());
				builder.put(MinecraftReflection.getIChatBaseComponentClass(), (EquivalentConverter) getWrappedChatComponentConverter());
				builder.put(MinecraftReflection.getServerPingClass(), (EquivalentConverter) getWrappedServerPingConverter());
				builder.put(MinecraftReflection.getStatisticClass(), (EquivalentConverter) getWrappedStatisticConverter());
				
				for (Entry<Class<?>, EquivalentConverter<?>> entry : EnumWrappers.getFromNativeMap().entrySet()) {
					builder.put((Class) entry.getKey(), (EquivalentConverter) entry.getValue());
				}
			}
			genericConverters = builder.build();
		}
		return genericConverters;
	}
	
	/**
	 * Retrieve every NMS to/from Bukkit converter as unwrappers.
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

	private static MethodAccessor getMobEffectId = null;
	private static MethodAccessor getMobEffect = null;

	public static EquivalentConverter<PotionEffectType> getEffectTypeConverter() {
		return ignoreNull(new EquivalentConverter<PotionEffectType>() {

			@Override
			public Class<PotionEffectType> getSpecificType() {
				return PotionEffectType.class;
			}

			@Override
			public Object getGeneric(PotionEffectType specific) {
				Class<?> clazz = MinecraftReflection.getMobEffectListClass();
				if (getMobEffect == null) {
					getMobEffect = Accessors.getMethodAccessor(clazz, "fromId", int.class);
				}

				int id = specific.getId();
				return getMobEffect.invoke(null, id);
			}

			@Override
			public PotionEffectType getSpecific(Object generic) {
				Class<?> clazz = MinecraftReflection.getMobEffectListClass();
				if (getMobEffectId == null) {
					getMobEffectId = Accessors.getMethodAccessor(clazz, "getId", clazz);
				}

				int id = (int) getMobEffectId.invoke(null, generic);
				return PotionEffectType.getById(id);
			}
		});
	}
}
