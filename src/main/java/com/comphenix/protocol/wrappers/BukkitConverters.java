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

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Either.Left;
import com.comphenix.protocol.wrappers.Either.Right;
import com.comphenix.protocol.wrappers.WrappedProfilePublicKey.WrappedProfileKeyData;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.injector.BukkitUnwrapper;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.injector.PacketConstructor.Unwrapper;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers.Dimension;
import com.comphenix.protocol.wrappers.EnumWrappers.FauxEnumConverter;
import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import static com.comphenix.protocol.utility.MinecraftReflection.getCraftBukkitClass;
import static com.comphenix.protocol.utility.MinecraftReflection.getMinecraftClass;
import static com.comphenix.protocol.wrappers.Converters.handle;
import static com.comphenix.protocol.wrappers.Converters.ignoreNull;

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

	private static final Map<Class<?>, Supplier<List<Object>>> LIST_SUPPLIERS = new ConcurrentHashMap<>();

	private static <T> Object getGenericList(Class<?> listClass, List<T> specific, EquivalentConverter<T> itemConverter) {
		List<Object> newList;
		Supplier<List<Object>> supplier = LIST_SUPPLIERS.get(listClass);
		if (supplier == null) {
			try {
				Constructor<?> ctor = listClass.getConstructor();
				newList = (List<Object>) ctor.newInstance();
				supplier = () -> {
					try {
						return (List<Object>) ctor.newInstance();
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException(ex);
					}
				};
			} catch (ReflectiveOperationException ex) {
				// ex.printStackTrace();
				supplier = ArrayList::new;
				newList = new ArrayList<>();
			}

			LIST_SUPPLIERS.put(listClass, supplier);
		} else {
			newList = supplier.get();
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

	private static <T> List<T> getSpecificList(Object generic, EquivalentConverter<T> itemConverter) {
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

	public static <T> EquivalentConverter<List<T>> getListConverter(final Class<?> listClass, final EquivalentConverter<T> itemConverter) {
		return ignoreNull(new EquivalentConverter<List<T>>() {
			@Override
			public List<T> getSpecific(Object generic) {
				return getSpecificList(generic, itemConverter);
			}

			@Override
			public Object getGeneric(List<T> specific) {
				return getGenericList(listClass, specific, itemConverter);
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
				return getSpecificList(generic, itemConverter);
			}

			@Override
			public Object getGeneric(List<T> specific) {
				return getGenericList(specific.getClass(), specific, itemConverter);
			}

			@Override
			public Class<List<T>> getSpecificType() {
				// Damn you Java
				Class<?> dummy = List.class;
				return (Class<List<T>>) dummy;
			}
		});
	}

	@SuppressWarnings("rawtypes")
	public static <A, B> EquivalentConverter<Pair<A, B>> getPairConverter(final EquivalentConverter<A> firstConverter,
																		  final EquivalentConverter<B> secondConverter) {
		return ignoreNull(new EquivalentConverter<Pair<A, B>>() {
			@Override
			public Object getGeneric(Pair<A, B> specific) {
				Object first = firstConverter.getGeneric(specific.getFirst());
				Object second = secondConverter.getGeneric(specific.getSecond());

				return new com.mojang.datafixers.util.Pair(first, second);
			}

			@Override
			public Pair<A, B> getSpecific(Object generic) {
				com.mojang.datafixers.util.Pair mjPair = (com.mojang.datafixers.util.Pair) generic;

				A first = firstConverter.getSpecific(mjPair.getFirst());
				B second = secondConverter.getSpecific(mjPair.getSecond());

				return new Pair(first, second);
			}

			@Override
			public Class<Pair<A, B>> getSpecificType() {
				Class<?> dummy = Pair.class;
				return (Class<Pair<A, B>>) dummy;
			}
		});
	}


	/**
	 * @param leftConverter convert the left value if available
	 * @param rightConverter convert the right value if available
	 * @return converter for Mojang either class
	 * @param <A> converted left type
	 * @param <B> converted right type
	 */
    public static <A, B> EquivalentConverter<Either<A, B>> getEitherConverter(EquivalentConverter<A> leftConverter,
                                                                              EquivalentConverter<B> rightConverter) {
        return ignoreNull(new EquivalentConverter<Either<A, B>>() {
            @Override
            public Object getGeneric(Either<A, B> specific) {
                return specific.map(
                    left -> com.mojang.datafixers.util.Either.left(leftConverter.getGeneric(left)),
                    right -> com.mojang.datafixers.util.Either.right(rightConverter.getGeneric(right))
                );
            }

            @Override
            public Either<A, B> getSpecific(Object generic) {
                com.mojang.datafixers.util.Either<A, B> mjEither = (com.mojang.datafixers.util.Either<A, B>) generic;

                return mjEither.map(
                    left -> new Left<>(leftConverter.getSpecific(left)),
                    right -> new Right<>(rightConverter.getSpecific(right))
                );
            }

            @Override
            public Class<Either<A, B>> getSpecificType() {
                Class<?> dummy = Either.class;
                return (Class<Either<A, B>>) dummy;
            }
        });
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
		return ignoreNull(handle(WrappedGameProfile::getHandle, WrappedGameProfile::fromHandle, WrappedGameProfile.class));
	}
	
	/**
	 * Retrieve a converter for wrapped chat components.
	 * @return Wrapped chat component.
	 */
	public static EquivalentConverter<WrappedChatComponent> getWrappedChatComponentConverter() {
		return ignoreNull(handle(WrappedChatComponent::getHandle, WrappedChatComponent::fromHandle, WrappedChatComponent.class));
	}
	
	/**
	 * Retrieve a converter for wrapped block data.
	 * @return Wrapped block data.
	 */
	public static EquivalentConverter<WrappedBlockData> getWrappedBlockDataConverter() {
		return ignoreNull(handle(WrappedBlockData::getHandle, WrappedBlockData::fromHandle, WrappedBlockData.class));
	}

	/**
	 * Retrieve a converter for wrapped block entity type.
	 * @return Wrapped block entity type.
	 */
	public static EquivalentConverter<WrappedRegistrable> getWrappedRegistrable(
		@NotNull final Class<?> registrableClass
	) {
		return ignoreNull(
			handle(
				WrappedRegistrable::getHandle,
				handle -> WrappedRegistrable.fromHandle(registrableClass, handle),
				WrappedRegistrable.class
			)
		);
	}

	/**
	 * Retrieve a converter for wrapped attribute snapshots.
	 * @return Wrapped attribute snapshot converter.
	 */
	public static EquivalentConverter<WrappedAttribute> getWrappedAttributeConverter() {
		return ignoreNull(handle(WrappedAttribute::getHandle, WrappedAttribute::fromHandle, WrappedAttribute.class));
	}

	public static EquivalentConverter<WrappedProfilePublicKey> getWrappedProfilePublicKeyConverter() {
		return ignoreNull(handle(WrappedProfilePublicKey::getHandle, WrappedProfilePublicKey::new, WrappedProfilePublicKey.class));
	}

	public static EquivalentConverter<WrappedProfileKeyData> getWrappedPublicKeyDataConverter() {
		return ignoreNull(handle(WrappedProfileKeyData::getHandle, WrappedProfileKeyData::new, WrappedProfileKeyData.class));
	}

	public static EquivalentConverter<WrappedRemoteChatSessionData> getWrappedRemoteChatSessionDataConverter() {
		return ignoreNull(handle(WrappedRemoteChatSessionData::getHandle, WrappedRemoteChatSessionData::new, WrappedRemoteChatSessionData.class));
	}

	/**
	 * @return converter for cryptographic signature data that are used in login and chat packets
	 */
    public static EquivalentConverter<WrappedSaltedSignature> getWrappedSignatureConverter() {
        return ignoreNull(handle(WrappedSaltedSignature::getHandle, WrappedSaltedSignature::new, WrappedSaltedSignature.class));
    }

	/**
	 * @return converter for an encoded cryptographic message signature
	 */
    public static EquivalentConverter<WrappedMessageSignature> getWrappedMessageSignatureConverter() {
        return ignoreNull(handle(WrappedMessageSignature::getHandle, WrappedMessageSignature::new, WrappedMessageSignature.class));
    }

	public static EquivalentConverter<WrappedLevelChunkData.ChunkData> getWrappedChunkDataConverter() {
		return ignoreNull(handle(WrappedLevelChunkData.ChunkData::getHandle, WrappedLevelChunkData.ChunkData::new, WrappedLevelChunkData.ChunkData.class));
	}

	public static EquivalentConverter<WrappedLevelChunkData.LightData> getWrappedLightDataConverter() {
		return ignoreNull(handle(WrappedLevelChunkData.LightData::getHandle, WrappedLevelChunkData.LightData::new, WrappedLevelChunkData.LightData.class));
	}

	public static EquivalentConverter<PacketContainer> getPacketContainerConverter() {
		return ignoreNull(handle(PacketContainer::getHandle, PacketContainer::fromPacket, PacketContainer.class));
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
				if (MinecraftReflection.is(MinecraftReflection.getDataWatcherItemClass(), generic))
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
	 * Retrieve a converter for data values in 1.19.3+.
	 * @return A data value converter.
	 */
	public static EquivalentConverter<WrappedDataValue> getDataValueConverter() {
		return ignoreNull(new EquivalentConverter<WrappedDataValue>() {
			@Override
			public Object getGeneric(WrappedDataValue specific) {
				return specific.getHandle();
			}

			@Override
			public WrappedDataValue getSpecific(Object generic) {
				return new WrappedDataValue(generic);
			}

			@Override
			public Class<WrappedDataValue> getSpecificType() {
				return WrappedDataValue.class;
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
								getMethodByReturnTypeAndParameters("getType", worldType, new Class<?>[]{String.class});
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
									getMethodByReturnTypeAndParameters("name", String.class, new Class<?>[]{});
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
					if (id != null && id >= 0 && manager != null) {
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

	private static MethodAccessor getEntityTypeName;
	private static MethodAccessor entityTypeFromName;

	public static EquivalentConverter<EntityType> getEntityTypeConverter() {
		return ignoreNull(new EquivalentConverter<EntityType>() {
			@Override
			public Object getGeneric(EntityType specific) {
				if (entityTypeFromName == null) {
					Class<?> entityTypesClass = MinecraftReflection.getEntityTypes();
					entityTypeFromName = Accessors.getMethodAccessor(
							FuzzyReflection
									.fromClass(entityTypesClass, false)
									.getMethod(FuzzyMethodContract
											           .newBuilder()
											           .returnDerivedOf(Optional.class)
											           .parameterExactArray(new Class<?>[]{ String.class })
											           .build()));
				}

				Optional<?> opt = (Optional<?>) entityTypeFromName.invoke(null, specific.getName());
				return opt.orElse(null);
			}

			@Override
			public EntityType getSpecific(Object generic) {
				if (getEntityTypeName == null) {
					Class<?> entityTypesClass = MinecraftReflection.getEntityTypes();
					getEntityTypeName = Accessors.getMethodAccessor(
							FuzzyReflection
									.fromClass(entityTypesClass, false)
									.getMethod(FuzzyMethodContract
											           .newBuilder()
											           .returnTypeExact(MinecraftReflection.getMinecraftKeyClass())
											           .parameterExactArray(new Class<?>[]{ entityTypesClass })
											           .build()));
				}

				MinecraftKey key = MinecraftKey.fromHandle(getEntityTypeName.invoke(null, generic));
				return EntityType.fromName(key.getKey());
			}

			@Override
			public Class<EntityType> getSpecificType() {
				return EntityType.class;
			}
		});
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
		return ignoreNull(handle(WrappedServerPing::getHandle, WrappedServerPing::fromHandle, WrappedServerPing.class));
	}
	
	/**
	 * Retrieve the converter for a statistic.
	 * @return Statistic converter.
	 */
	public static EquivalentConverter<WrappedStatistic> getWrappedStatisticConverter() {
		return ignoreNull(handle(WrappedStatistic::getHandle, WrappedStatistic::fromHandle, WrappedStatistic.class));
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
					mobEffectModifier = new StructureModifier<>(MinecraftReflection.getMobEffectClass());
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
					vec3dModifier = new StructureModifier<>(MinecraftReflection.getVec3DClass());
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

	static MethodAccessor getSound = null;
	static MethodAccessor getSoundEffect = null;
	static FieldAccessor soundKey = null;

	static MethodAccessor getSoundEffectByKey = null;
	static MethodAccessor getSoundEffectBySound = null;
	static MethodAccessor getSoundByEffect = null;

	static Map<String, Sound> soundIndex = null;

	public static EquivalentConverter<Sound> getSoundConverter() {
		// Try to create sound converter for new versions greater 1.16.4
		if (MinecraftVersion.NETHER_UPDATE_4.atOrAbove()) {
			if (getSoundEffectByKey == null || getSoundEffectBySound == null || getSoundByEffect == null) {
				Class<?> craftSound = MinecraftReflection.getCraftSoundClass();
				FuzzyReflection fuzzy = FuzzyReflection.fromClass(craftSound, true);

				getSoundEffectByKey = Accessors.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters(
						"getSoundEffect",
						MinecraftReflection.getSoundEffectClass(),
						String.class
				));

				getSoundEffectBySound = Accessors.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters(
						"getSoundEffect",
						MinecraftReflection.getSoundEffectClass(),
						Sound.class
				));

				getSoundByEffect = Accessors.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters(
						"getBukkit",
						Sound.class,
						MinecraftReflection.getSoundEffectClass()
				));
			}

			return ignoreNull(new EquivalentConverter<Sound>() {

				@Override
				public Class<Sound> getSpecificType() {
					return Sound.class;
				}

				@Override
				public Object getGeneric(Sound specific) {
					return getSoundEffectBySound.invoke(null, specific);
				}

				@Override
				public Sound getSpecific(Object generic) {
					try {
						return (Sound) getSoundByEffect.invoke(null, generic);
					} catch (IllegalStateException ex) {
						if (ex.getCause() instanceof NullPointerException) {
							// "null" sounds cause NPEs inside getSoundByEffect
							return null;
						}
						throw ex;
					}
				}
			});
		}

		// Fall back to sound converter from legacy versions before 1.16.4
		if (getSound == null || getSoundEffect == null) {
			Class<?> craftSound = MinecraftReflection.getCraftSoundClass();
			FuzzyReflection fuzzy = FuzzyReflection.fromClass(craftSound, true);
			getSound = Accessors.getMethodAccessor(
					fuzzy.getMethodByReturnTypeAndParameters("getSound", String.class, Sound.class));
			getSoundEffect = Accessors.getMethodAccessor(fuzzy.getMethodByReturnTypeAndParameters("getSoundEffect",
					MinecraftReflection.getSoundEffectClass(), String.class));
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

	public static EquivalentConverter<WrappedParticle> getParticleConverter() {
		return ignoreNull(handle(WrappedParticle::getHandle, WrappedParticle::fromHandle, WrappedParticle.class));
	}

	public static EquivalentConverter<Advancement> getAdvancementConverter() {
		return ignoreNull(new EquivalentConverter<Advancement>() {
			@Override
			public Advancement getSpecific(Object generic) {
				try {
					return (Advancement) getCraftBukkitClass("advancement.CraftAdvancement")
							.getConstructor(getMinecraftClass("advancements.Advancement", "Advancement"))
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
	public static Map<Class<?>, EquivalentConverter<Object>> getConvertersForGeneric() {
		if (genericConverters == null) {
			ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder = ImmutableMap.builder();
			addConverter(builder, MinecraftReflection::getDataWatcherClass, BukkitConverters::getDataWatcherConverter);
			addConverter(builder, MinecraftReflection::getItemStackClass, BukkitConverters::getItemStackConverter);
			addConverter(builder, MinecraftReflection::getNBTBaseClass, BukkitConverters::getNbtConverter);
			addConverter(builder, MinecraftReflection::getNBTCompoundClass, BukkitConverters::getNbtConverter);
			addConverter(builder, MinecraftReflection::getDataWatcherItemClass, BukkitConverters::getWatchableObjectConverter);
			addConverter(builder, MinecraftReflection::getMobEffectClass, BukkitConverters::getPotionEffectConverter);
			addConverter(builder, MinecraftReflection::getNmsWorldClass, BukkitConverters::getWorldConverter);
			addConverter(builder, MinecraftReflection::getWorldTypeClass, BukkitConverters::getWorldTypeConverter);
			addConverter(builder, MinecraftReflection::getAttributeSnapshotClass, BukkitConverters::getWrappedAttributeConverter);
			addConverter(builder, MinecraftReflection::getBlockClass, BukkitConverters::getBlockConverter);
			addConverter(builder, MinecraftReflection::getGameProfileClass, BukkitConverters::getWrappedGameProfileConverter);
			addConverter(builder, MinecraftReflection::getServerPingClass, BukkitConverters::getWrappedServerPingConverter);
			addConverter(builder, MinecraftReflection::getStatisticClass, BukkitConverters::getWrappedStatisticConverter);
			addConverter(builder, MinecraftReflection::getIBlockDataClass, BukkitConverters::getWrappedBlockDataConverter);

			for (Entry<Class<?>, EquivalentConverter<?>> entry : EnumWrappers.getFromNativeMap().entrySet()) {
				addConverter(builder, entry::getKey, entry::getValue);
			}

			genericConverters = builder.build();
		}

		return genericConverters;
	}

	private static void addConverter(ImmutableMap.Builder<Class<?>, EquivalentConverter<Object>> builder,
	                                 Supplier<Class<?>> getClass, Supplier<EquivalentConverter> getConverter) {
		try {
			Class<?> clazz = getClass.get();
			if (clazz != null) {
				EquivalentConverter converter = getConverter.get();
				if (converter != null) {
					builder.put(clazz, converter);
				}
			}
		} catch (Exception ex) {
			ProtocolLogger.debug("Exception registering converter", ex);
		}
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
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(clazz, false);
					getMobEffect = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
							.parameterExactArray(int.class)
							.returnTypeExact(clazz)
							.requireModifier(Modifier.STATIC)
							.build()));
				}

				int id = specific.getId();
				return getMobEffect.invoke(null, id);
			}

			@Override
			public PotionEffectType getSpecific(Object generic) {
				Class<?> clazz = MinecraftReflection.getMobEffectListClass();
				if (getMobEffectId == null) {
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(clazz, false);
					getMobEffectId = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract.newBuilder()
							.parameterExactArray(clazz)
							.returnTypeExact(int.class)
							.requireModifier(Modifier.STATIC)
							.build()));
				}

				int id = (int) getMobEffectId.invoke(null, generic);
				return PotionEffectType.getById(id);
			}
		});
	}

	private static Class<?> dimensionManager;
	private static FauxEnumConverter<Dimension> dimensionConverter;
	private static FauxEnumConverter<DimensionImpl> dimensionImplConverter;

	private static MethodAccessor dimensionFromId = null;
	private static MethodAccessor idFromDimension = null;

	private static FieldAccessor worldKeyField = null;
	private static MethodAccessor getServer = null;
	private static MethodAccessor getWorldServer = null;
	private static MethodAccessor getWorld = null;

	public static EquivalentConverter<World> getWorldKeyConverter() {
		return ignoreNull(new EquivalentConverter<World>() {
			@Override
			public Object getGeneric(World specific) {
				Object nmsWorld = getWorldConverter().getGeneric(specific);

				if (worldKeyField == null) {
					Class<?> worldClass = MinecraftReflection.getNmsWorldClass();
					Class<?> resourceKeyClass = MinecraftReflection.getResourceKey();

					FuzzyReflection fuzzy = FuzzyReflection.fromClass(nmsWorld.getClass(), true);
					worldKeyField = Accessors.getFieldAccessor(fuzzy.getParameterizedField(resourceKeyClass, worldClass));
				}

				return worldKeyField.get(nmsWorld);
			}

			@Override
			public World getSpecific(Object generic) {
				if (getServer == null) {
					getServer = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getServer");
				}

				Object server = getServer.invoke(Bukkit.getServer());
				if (getWorldServer == null) {
					FuzzyReflection fuzzy = FuzzyReflection.fromClass(server.getClass(), false);
					getWorldServer = Accessors.getMethodAccessor(fuzzy.getMethod(FuzzyMethodContract
							.newBuilder()
							.parameterExactArray(generic.getClass())
							.returnTypeExact(MinecraftReflection.getWorldServerClass())
							.build()));
				}

				Object worldServer = getWorldServer.invoke(server, generic);
				if (getWorld == null) {
					getWorld = Accessors.getMethodAccessor(worldServer.getClass(), "getWorld");
				}

				return (World) getWorld.invoke(worldServer);
			}

			@Override
			public Class<World> getSpecificType() {
				return World.class;
			}
		});
	}

	enum DimensionImpl {
		OVERWORLD_IMPL(0),
		THE_NETHER_IMPL(-1),
		THE_END_IMPL(1);

		int id;
		DimensionImpl(int id) {
			this.id = id;
		}

		static DimensionImpl fromId(int id) {
			switch (id) {
				case 0: return OVERWORLD_IMPL;
				case -1: return THE_NETHER_IMPL;
				case 1: return THE_END_IMPL;
				default: throw new IllegalArgumentException("Invalid dimension " + id);
			}
		}
	}

	private static FieldAccessor dimensionKey;
	private static MethodAccessor worldHandleAccessor;
	private static MethodAccessor worldHandleDimensionManagerAccessor;

	public static EquivalentConverter<World> getDimensionConverter() {
		return ignoreNull(new EquivalentConverter<World>() {
			@Override
			public Object getGeneric(World specific) {
				return getWorldHandleDimensionManagerAccessor().invoke(getWorldHandleAccessor().invoke(specific));
			}

			@Override
			public World getSpecific(Object generic) {
				for (World world : Bukkit.getWorlds()) {
					if (getGeneric(world) == generic) {
						return world;
					}
				}
				throw new IllegalArgumentException();
			}

			@Override
			public Class<World> getSpecificType() {
				return World.class;
			}
		});
	}

	private static MethodAccessor getWorldHandleAccessor() {
		if (worldHandleAccessor == null) {
			Method handleMethod = FuzzyReflection.fromClass(MinecraftReflection.getCraftWorldClass())
					.getMethod(FuzzyMethodContract.newBuilder()
							.nameExact("getHandle") // i guess this will never change
							.returnTypeExact(MinecraftReflection.getWorldServerClass())
							.build());
			worldHandleAccessor = Accessors.getMethodAccessor(handleMethod);
		}
		return worldHandleAccessor;
	}

	private static MethodAccessor getWorldHandleDimensionManagerAccessor() {
		if (worldHandleDimensionManagerAccessor == null) {
			Method dimensionGetter = FuzzyReflection.fromClass(MinecraftReflection.getWorldServerClass())
					.getMethod(FuzzyMethodContract.newBuilder()
							.returnTypeExact(MinecraftReflection.getDimensionManager())
							.build());
			worldHandleDimensionManagerAccessor = Accessors.getMethodAccessor(dimensionGetter);
		}
		return worldHandleDimensionManagerAccessor;
	}

	public static EquivalentConverter<Integer> getDimensionIDConverter() {
		return ignoreNull(new EquivalentConverter<Integer>() {
			@Override
			public Object getGeneric(Integer specific) {
				if (dimensionManager == null) {
					dimensionManager = MinecraftReflection.getDimensionManager();
				}

				if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
					World world = null;
					if (specific == 0) {
						world = Bukkit.getWorlds().get(0);
					} else if (specific == -1) {
						for (World world1 : Bukkit.getWorlds()) {
							if (world1.getEnvironment() == World.Environment.NETHER) {
								world = world1;
								break;
							}
						}
					} else if (specific == 1) {
						for (World world1 : Bukkit.getWorlds()) {
							if (world1.getEnvironment() == World.Environment.THE_END) {
								world = world1;
								break;
							}
						}
					}

					if (world != null) {
						try {
							return getWorldHandleDimensionManagerAccessor().invoke(getWorldHandleAccessor().invoke(world));
						} catch (Exception ignored) {
							// method not available, fall through
						}
					}

					throw new IllegalArgumentException();
				}
				if (MinecraftVersion.NETHER_UPDATE_2.atOrAbove()) {
					if (dimensionImplConverter == null) {
						dimensionImplConverter = new FauxEnumConverter<>(DimensionImpl.class, dimensionManager);
					}

					DimensionImpl dimension = DimensionImpl.fromId(specific);
					return dimensionImplConverter.getGeneric(dimension);
				} else if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
					if (dimensionConverter == null) {
						dimensionConverter = new FauxEnumConverter<>(Dimension.class, dimensionManager);
					}

					Dimension dimension = Dimension.fromId(specific);
					return dimensionConverter.getGeneric(dimension);
				} else {
					if (dimensionFromId == null) {
						FuzzyReflection reflection = FuzzyReflection.fromClass(dimensionManager, false);
						FuzzyMethodContract contract = FuzzyMethodContract
								.newBuilder()
								.requireModifier(Modifier.STATIC)
								.parameterExactType(int.class)
								.returnTypeExact(dimensionManager)
								.build();
						dimensionFromId = Accessors.getMethodAccessor(reflection.getMethod(contract));
					}

					return dimensionFromId.invoke(null, specific);
				}
			}

			@Override
			public Integer getSpecific(Object generic) {
				if (dimensionManager == null) {
					dimensionManager = MinecraftReflection.getDimensionManager();
				}

				if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
					if (dimensionKey == null) {
						FuzzyReflection fuzzy = FuzzyReflection.fromClass(dimensionManager, false);
						dimensionKey = Accessors.getFieldAccessor(fuzzy.getField(FuzzyFieldContract
								.newBuilder()
								.typeExact(MinecraftReflection.getMinecraftKeyClass())
								.banModifier(Modifier.STATIC)
								.build()));
					}

					MinecraftKey key = MinecraftKey.fromHandle(dimensionKey.get(generic));
					switch (key.getKey()) {
						case "overworld":
							return Dimension.OVERWORLD.getId();
						case "the_nether":
							return Dimension.THE_NETHER.getId();
						case "the_end":
							return Dimension.THE_END.getId();
						default:
							throw new IllegalArgumentException("id not supported for extra dimensions");
					}
				}
				if (MinecraftVersion.NETHER_UPDATE_2.atOrAbove()) {
					if (dimensionImplConverter == null) {
						dimensionImplConverter = new FauxEnumConverter<>(DimensionImpl.class, dimensionManager);
					}

					DimensionImpl dimension = dimensionImplConverter.getSpecific(generic);
					return dimension.id;
				} else if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
					if (dimensionConverter == null) {
						dimensionConverter = new FauxEnumConverter<>(Dimension.class, dimensionManager);
					}

					Dimension dimension = dimensionConverter.getSpecific(generic);
					return dimension.getId();
				} else {
					if (idFromDimension == null) {
						FuzzyReflection reflection = FuzzyReflection.fromClass(dimensionManager, false);
						FuzzyMethodContract contract = FuzzyMethodContract
								.newBuilder()
								.banModifier(Modifier.STATIC)
								.returnTypeExact(int.class)
								.parameterCount(0)
								.build();
						idFromDimension = Accessors.getMethodAccessor(reflection.getMethod(contract));
					}

					return (Integer) idFromDimension.invoke(generic);
				}
			}

			@Override
			public Class<Integer> getSpecificType() {
				return Integer.class;
			}
		});
	}
	
	private static ConstructorAccessor merchantRecipeListConstructor = null;
	private static MethodAccessor bukkitMerchantRecipeToCraft = null;
	private static MethodAccessor craftMerchantRecipeToNMS = null;
	private static MethodAccessor nmsMerchantRecipeToBukkit = null;
	
	/**
	 * Creates a converter from a MerchantRecipeList (which is just an ArrayList of MerchantRecipe wrapper)
	 * to a {@link List} of {@link MerchantRecipe}. Primarily for the packet OPEN_WINDOW_MERCHANT which is present
	 * in 1.13+.
	 *
	 * @return The MerchantRecipeList converter.
	 */
	public static EquivalentConverter<List<MerchantRecipe>> getMerchantRecipeListConverter() {
		return ignoreNull(new EquivalentConverter<List<MerchantRecipe>>() {
			
			@Override
			public Object getGeneric(List<MerchantRecipe> specific) {
				if (merchantRecipeListConstructor == null) {
					Class<?> merchantRecipeListClass = MinecraftReflection.getMerchantRecipeList();
					merchantRecipeListConstructor = Accessors.getConstructorAccessor(merchantRecipeListClass);
					Class<?> craftMerchantRecipeClass = MinecraftReflection.getCraftBukkitClass("inventory.CraftMerchantRecipe");
					FuzzyReflection reflection = FuzzyReflection.fromClass(craftMerchantRecipeClass, false);
					bukkitMerchantRecipeToCraft = Accessors.getMethodAccessor(reflection.getMethodByName("fromBukkit"));
					craftMerchantRecipeToNMS = Accessors.getMethodAccessor(reflection.getMethodByName("toMinecraft"));
				}
				return specific.stream().map(recipe -> craftMerchantRecipeToNMS.invoke(bukkitMerchantRecipeToCraft.invoke(null, recipe)))
						.collect(() -> (List<Object>)merchantRecipeListConstructor.invoke(), List::add, List::addAll);
			}
			
			@Override
			public List<MerchantRecipe> getSpecific(Object generic) {
				if (nmsMerchantRecipeToBukkit == null) {
					Class<?> merchantRecipeClass = MinecraftReflection.getMinecraftClass(
							"world.item.trading.MerchantRecipe", "world.item.trading.MerchantOffer","MerchantRecipe"
					);
					FuzzyReflection reflection = FuzzyReflection.fromClass(merchantRecipeClass, false);
					nmsMerchantRecipeToBukkit = Accessors.getMethodAccessor(reflection.getMethodByName("asBukkit"));
				}
				return ((List<Object>)generic).stream().map(o -> (MerchantRecipe)nmsMerchantRecipeToBukkit.invoke(o)).collect(Collectors.toList());
			}
			
			@Override
			public Class<List<MerchantRecipe>> getSpecificType() {
				// Damn you Java
				Class<?> dummy = List.class;
				return (Class<List<MerchantRecipe>>) dummy;
			}
			
		});
	}

	private static MethodAccessor sectionPositionCreate;
	private static Class<?> sectionPositionClass;

	public static EquivalentConverter<BlockPosition> getSectionPositionConverter() {
		return ignoreNull(new EquivalentConverter<BlockPosition>() {
			@Override
			public Object getGeneric(BlockPosition specific) {
				if (sectionPositionClass == null) {
					sectionPositionClass = MinecraftReflection.getSectionPosition();
				}

				if (sectionPositionCreate == null) {
					sectionPositionCreate = Accessors.getMethodAccessor(
							FuzzyReflection.fromClass(sectionPositionClass).getMethod(FuzzyMethodContract
									.newBuilder()
									.requireModifier(Modifier.STATIC)
									.returnTypeExact(sectionPositionClass)
									.parameterExactArray(int.class, int.class, int.class)
									.build())
					);
				}

				return sectionPositionCreate.invoke(null, specific.x, specific.y, specific.z);
			}

			@Override
			public BlockPosition getSpecific(Object generic) {
				StructureModifier<Integer> modifier = new StructureModifier<>(generic.getClass()).withTarget(generic).withType(int.class);
				return new BlockPosition(modifier.readSafely(0), modifier.readSafely(1), modifier.readSafely(2));
			}

			@Override
			public Class<BlockPosition> getSpecificType() {
				return BlockPosition.class;
			}
		});
	}

	private static Field gameStateMapField;
	private static Field gameStateIdField;

	public static EquivalentConverter<Integer> getGameStateConverter() {
		return new EquivalentConverter<Integer>() {
			@Override
			public Object getGeneric(Integer specific) {
				if (specific == null) {
					specific = 0;
				}

				if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
					if (gameStateMapField == null) {
						Class<?> stateClass = MinecraftReflection.getGameStateClass();
						gameStateMapField = FuzzyReflection
								.fromClass(stateClass, true)
								.getField(FuzzyFieldContract
										.newBuilder()
										.typeDerivedOf(Map.class)
										.build());
						gameStateMapField.setAccessible(true);
					}

					try {
						Map<Integer, Object> map = (Map<Integer, Object>) gameStateMapField.get(null);
						return map.get(specific);
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException(ex);
					}
				} else {
					return specific;
				}
			}

			@Override
			public Integer getSpecific(Object generic) {
				if (generic == null) {
					return 0;
				}

				if (MinecraftVersion.NETHER_UPDATE.atOrAbove()) {
					if (gameStateIdField == null) {
						Class<?> stateClass = MinecraftReflection.getGameStateClass();
						gameStateIdField = FuzzyReflection
								.fromClass(stateClass, true)
								.getField(FuzzyFieldContract
										.newBuilder()
										.typeExact(int.class)
										.build());
						gameStateIdField.setAccessible(true);
					}

					try {
						return (Integer) gameStateIdField.get(generic);
					} catch (ReflectiveOperationException ex) {
						throw new RuntimeException(ex);
					}
				} else {
					return (Integer) generic;
				}
			}

			@Override
			public Class<Integer> getSpecificType() {
				return Integer.class;
			}
		};
	}
}
