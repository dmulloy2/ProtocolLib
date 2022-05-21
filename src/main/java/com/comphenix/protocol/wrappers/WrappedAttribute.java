package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.collection.CachedSet;
import com.comphenix.protocol.wrappers.collection.ConvertedSet;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;


/**
 * Represents a single attribute sent in packet 44.
 * @author Kristian
 */
public class WrappedAttribute extends AbstractWrapper {
	public static boolean KEY_WRAPPED = MinecraftVersion.NETHER_UPDATE.atOrAbove();

	// Shared structure modifier
	private static StructureModifier<Object> ATTRIBUTE_MODIFIER;
	
	// The one constructor
	private static Constructor<?> ATTRIBUTE_CONSTRUCTOR;

	private static final Map<String, String> REMAP;

	static {
		Map<String, String> remap = new HashMap<>();
		remap.put("generic.maxHealth", "generic.max_health");
		remap.put("generic.followRange", "generic.follow_range");
		remap.put("generic.knockbackResistance", "generic.knockback_resistance");
		remap.put("generic.movementSpeed", "generic.movement_speed");
		remap.put("generic.attackDamage", "generic.attack_damage");
		remap.put("generic.attackSpeed", "generic.attack_speed");
		remap.put("generic.armorToughness", "generic.armor_toughness");
		remap.put("generic.attackKnockback", "generic.attack_knockback");
		remap.put("horse.jumpStrength", "horse.jump_strength");
		remap.put("zombie.spawnReinforcements", "zombie.spawn_reinforcements");
		REMAP = ImmutableMap.copyOf(remap);
	}
	
	/**
	 * Reference to the underlying attribute snapshot.
	 */
	protected Object handle;
	protected StructureModifier<Object> modifier;
	
	// Cached computed value
	private double computedValue = Double.NaN;
	
	// Cached modifiers list
	private Set<WrappedAttributeModifier> attributeModifiers;
	
	/**
	 * Construct a wrapper around a specific NMS instance.
	 * @param handle - the NMS instance.
	 */
	private WrappedAttribute(@Nonnull Object handle) {
		super(MinecraftReflection.getAttributeSnapshotClass());
		setHandle(handle);
		
		// Initialize modifier
		if (ATTRIBUTE_MODIFIER == null) {
			ATTRIBUTE_MODIFIER = new StructureModifier<>(MinecraftReflection.getAttributeSnapshotClass());
		}
		this.modifier = ATTRIBUTE_MODIFIER.withTarget(handle);
	}
	
	
	/**
	 * Construct a new wrapped attribute around a specific NMS instance.
	 * @param handle - handle to a NMS AttributeSnapshot.
	 * @return The attribute wrapper.
	 * @throws IllegalArgumentException If the handle is not a AttributeSnapshot.
	 */
	public static WrappedAttribute fromHandle(@Nonnull Object handle) {
		return new WrappedAttribute(handle);
	}
	
	/**
	 * Construct a new wrapped attribute builder.
	 * @return The new builder.
	 */
	public static Builder newBuilder() {
		return new Builder(null);
	}

	/**
	 * Construct a new wrapped attribute builder initialized to the values from a template.
	 * @param template - the attribute template.
	 * @return The new builder.
	 */
	public static Builder newBuilder(@Nonnull WrappedAttribute template) {
		return new Builder(Preconditions.checkNotNull(template, "template cannot be NULL."));
	}

	public static class WrappedAttributeBase {
		public double defaultValue;
		public boolean unknown;
		public String key;
	}

	private static final Class<?> ATTRIBUTE_BASE_CLASS = MinecraftReflection.getNullableNMS(
			"world.entity.ai.attributes.AttributeBase", "AttributeBase"
	);

	private static final AutoWrapper<WrappedAttributeBase> ATTRIBUTE_BASE = AutoWrapper.wrap(
			WrappedAttributeBase.class, ATTRIBUTE_BASE_CLASS
	);

	/**
	 * Retrieve the unique attribute key that identifies its function.
	 * <p>
	 * Example: "generic.maxHealth"
	 * @return The attribute key.
	 */
	public String getAttributeKey() {
		if (KEY_WRAPPED) {
			WrappedAttributeBase base = modifier.withType(ATTRIBUTE_BASE_CLASS, ATTRIBUTE_BASE).read(0);
			return base.key.replace("attribute.name.", ""); // TODO not entirely sure why this happens
		} else {
			return (String) modifier.withType(String.class).read(0);
		}
	}

	/**
	 * Retrieve the attribute base instance. New in 1.16.
	 *
	 * @return The attribute base
	 */
	public WrappedAttributeBase getBase() {
		return modifier.withType(WrappedAttributeBase.class, ATTRIBUTE_BASE).readSafely(0);
	}
	
	/**
	 * Retrieve the base value of this attribute, before any of the modifiers have been taken into account.
	 * @return The base value.
	 */
	public double getBaseValue() {
		return (Double) modifier.withType(double.class).read(0);
	}
	
	/**
	 * Retrieve the final computed value.
	 * @return The final value.
	 */
	public double getFinalValue() {
		if (Double.isNaN(computedValue)) {
			computedValue = computeValue();
		}
		return computedValue;
	}
	
	/**
	 * Retrieve the parent update attributes packet.
	 * @return The parent packet.
	 * @deprecated Removed in 1.17
	 */
	@Nullable
	public PacketContainer getParentPacket() {
		if (MinecraftVersion.CAVES_CLIFFS_1.atOrAbove()) {
			return null;
		}

		return new PacketContainer(
			PacketType.Play.Server.UPDATE_ATTRIBUTES,
			modifier.withType(MinecraftReflection.getPacketClass()).read(0)
		);
	}
	
	/**
	 * Determine if the attribute has a given attribute modifier, identified by UUID.
	 * @param id - the id to check for.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	public boolean hasModifier(UUID id) {
		return getModifiers().contains(WrappedAttributeModifier.newBuilder(id).build());
	}
	
	/**
	 * Retrieve an attribute modifier by UUID.
	 * @param id - the id to look for.
	 * @return The single attribute modifier with the given ID.
	 */
	public WrappedAttributeModifier getModifierByUUID(UUID id) {
		if (hasModifier(id)) {
			for (WrappedAttributeModifier modifier : getModifiers()) {
				if (Objects.equal(modifier.getUUID(), id)) {
					return modifier;
				}
			}
		}
		return null;
	}
	
	/**
	 * Retrieve an immutable set of all the attribute modifiers that will compute the final value of this attribute.
	 * @return Every attribute modifier.
	 */
	public Set<WrappedAttributeModifier> getModifiers() {
		if (attributeModifiers == null) {
			@SuppressWarnings("unchecked")
			Collection<Object> collection = (Collection<Object>) modifier.withType(Collection.class).read(0);

			// Convert to an equivalent wrapper
			ConvertedSet<Object, WrappedAttributeModifier> converted =
			  new ConvertedSet<Object, WrappedAttributeModifier>(getSetSafely(collection)) {
				@Override
				protected Object toInner(WrappedAttributeModifier outer) {
					return outer.getHandle();
				}
				
				@Override
				protected WrappedAttributeModifier toOuter(Object inner) {
					return WrappedAttributeModifier.fromHandle(inner);
				}
			};
			
			attributeModifiers = new CachedSet<>(converted);
		}
		return Collections.unmodifiableSet(attributeModifiers);
	}
	
	/**
	 * Construct an attribute with the same key and name, but a different list of modifiers.
	 * @param modifiers - attribute modifiers.
	 * @return The new attribute.
	 */
	public WrappedAttribute withModifiers(Collection<WrappedAttributeModifier> modifiers) {
		return newBuilder(this).modifiers(modifiers).build();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof WrappedAttribute) {
			WrappedAttribute other = (WrappedAttribute) obj;
			
			if (getBaseValue() == other.getBaseValue() &&
				   Objects.equal(getAttributeKey(), other.getAttributeKey())) {
				return other.getModifiers().containsAll(getModifiers());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (attributeModifiers == null)
			getModifiers();
		return Objects.hashCode(getAttributeKey(), getBaseValue(), attributeModifiers);
	}
	
	/**
	 * Compute the final value from the current attribute modifers.
	 * @return The final value.
	 */
	private double computeValue() {
		Collection<WrappedAttributeModifier> modifiers = getModifiers();
		double x = getBaseValue();
		double y = 0;
		
		// Compute each phase
		for (int phase = 0; phase < 3; phase++) {
			for (WrappedAttributeModifier modifier : modifiers) {
				if (modifier.getOperation().getId() == phase) {
					switch (phase) {
						case 0: // Adding phase
							x += modifier.getAmount();
							break;
						case 1: // Multiply percentage
							y += x * modifier.getAmount();
							break;
						case 2:
							y *= 1 + modifier.getAmount();
							break;
						default :
							throw new IllegalStateException("Unknown phase: " + phase);
					}
				}
			}
			
			// The additive phase is finished
			if (phase == 0) {
				y = x;
			}
		}
		return y;
	}
	
	@Override
	public String toString() {
		return "WrappedAttribute[key=" + getAttributeKey() + ", base=" + getBaseValue() + ", final=" + getFinalValue() + ", modifiers=" + getModifiers() + "]";
	}
	
	/**
	 * If the collection is a set, retrieve it - otherwise, create a new set with the same elements.
	 * @param collection - the collection.
	 * @return A set with the same elements.
	 */
	private static <U> Set<U> getSetSafely(Collection<U> collection) {
		return collection instanceof Set ? (Set<U>) collection : Sets.newHashSet(collection);
	}
	
	/**
	 * Ensure that the given double is not infinite nor NaN.
	 * @param value - the value to check.
	 */
	static double checkDouble(double value) {
		if (Double.isInfinite(value))
			throw new IllegalArgumentException("value cannot be infinite.");
		if (Double.isNaN(value))
			throw new IllegalArgumentException("value cannot be NaN.");
		return value;
	}
	
	/**
	 * Represents a builder for wrapped attributes.
	 * <p>
	 * Use {@link WrappedAttribute#newBuilder()} to construct it.
	 * @author Kristian
	 */
	public static class Builder {
		private double baseValue = Double.NaN;
		private String attributeKey;
		private PacketContainer packet;
		private Collection<WrappedAttributeModifier> modifiers = Collections.emptyList();
		
		private Builder(WrappedAttribute template) {
			if (template != null) {
				baseValue = template.getBaseValue();
				attributeKey = template.getAttributeKey();
				packet = template.getParentPacket();
				modifiers = template.getModifiers();
			}
		}
		
		/**
		 * Change the base value of the attribute.
		 * <p>
		 * The modifiers will automatically supply a value if this is unset.
		 * @param baseValue - the base value value.
		 * @return This builder, for chaining.
		 */
		public Builder baseValue(double baseValue) {
			this.baseValue = checkDouble(baseValue);
			return this;
		}
		
		/**
		 * Set the unique attribute key that identifies its function.
		 * <p>
		 * This is required.
		 * @param attributeKey - the unique attribute key.
		 * @return This builder, for chaining.
		 */
		public Builder attributeKey(String attributeKey) {
			this.attributeKey = Preconditions.checkNotNull(attributeKey, "attributeKey cannot be NULL.");
			return this;
		}
		
		/**
		 * Set the modifers that will be supplied to the client, and used to compute the final value.
		 * @param modifiers - the attribute modifiers.
		 * @return This builder, for chaining.
		 */
		public Builder modifiers(Collection<WrappedAttributeModifier> modifiers) {
			this.modifiers = Preconditions.checkNotNull(modifiers, "modifiers cannot be NULL - use an empty list instead.");
			return this;
		}
		
		/**
		 * Set the parent update attributes packet (44).
		 * @param packet - the parent packet.
		 * @return This builder, for chaining.
		 */
		public Builder packet(PacketContainer packet) {
			if (Preconditions.checkNotNull(packet, "packet cannot be NULL").getType() != PacketType.Play.Server.UPDATE_ATTRIBUTES) {
				throw new IllegalArgumentException("Packet must be UPDATE_ATTRIBUTES (44)");
			}
			this.packet = packet;
			return this;
		}
		
		/**
		 * Retrieve the unwrapped modifiers.
		 * @return Unwrapped modifiers.
		 */
		private Set<Object> getUnwrappedModifiers() {
			Set<Object> output = Sets.newHashSet();
			
			for (WrappedAttributeModifier modifier : modifiers) {
				output.add(modifier.getHandle());
			}
			return output;
		}
		
		/**
		 * Build a new wrapped attribute with the values of this builder.
		 * @return The wrapped attribute.
		 * @throws RuntimeException If anything went wrong with the reflection.
		 */
		public WrappedAttribute build() {
			Preconditions.checkNotNull(attributeKey, "attributeKey cannot be NULL.");
			
			// Remember to set the base value
			if (Double.isNaN(baseValue)) {
				throw new IllegalStateException("Base value has not been set.");
			}

			boolean isStatic = MinecraftVersion.CAVES_CLIFFS_1.atOrAbove();

			if (ATTRIBUTE_CONSTRUCTOR == null) {
				FuzzyReflection ref = FuzzyReflection.fromClass(MinecraftReflection.getAttributeSnapshotClass(), true);
				FuzzyMethodContract.Builder contract = FuzzyMethodContract.newBuilder().parameterCount(isStatic ? 3 : 4);
				if (!isStatic) {
					contract.parameterDerivedOf(MinecraftReflection.getPacketClass(), 0);
				}
				contract.parameterExactType(double.class).parameterDerivedOf(Collection.class);
				ATTRIBUTE_CONSTRUCTOR = ref.getConstructor(contract.build());

				// Just in case
				ATTRIBUTE_CONSTRUCTOR.setAccessible(true);
			}

			Object attributeKey;
			if (KEY_WRAPPED) {
				WrappedRegistry registry = WrappedRegistry.getAttributeRegistry();
				String strKey = REMAP.getOrDefault(this.attributeKey, this.attributeKey);
				attributeKey = registry.get(strKey);

				if (attributeKey == null) {
					throw new IllegalArgumentException("Invalid attribute name: " + this.attributeKey);
				}
			} else {
				attributeKey = this.attributeKey;
			}

			try {
				Object handle;
				if (isStatic) {
					handle = ATTRIBUTE_CONSTRUCTOR.newInstance(attributeKey, baseValue, getUnwrappedModifiers());
				} else {
					handle = ATTRIBUTE_CONSTRUCTOR.newInstance(packet.getHandle(), attributeKey, baseValue, getUnwrappedModifiers());
				}

				return new WrappedAttribute(handle);
			} catch (Exception e) {
				throw new RuntimeException("Cannot construct AttributeSnapshot.", e);
			}
		}
	}
}
