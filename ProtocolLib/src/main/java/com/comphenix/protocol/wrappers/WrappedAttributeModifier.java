package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;
import java.util.UUID;

import javax.annotation.Nonnull;

import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents a wrapper around a AttributeModifier.
 * <p>
 * This is used to compute the final attribute value.
 * 
 * @author Kristian
 */
public class WrappedAttributeModifier extends AbstractWrapper {
	 /**
	  * Represents the different modifier operations.
	  * <p>
	  * The final value is computed as follows:
	  * <ol>
	  * <li>Set X = base value.</li>
	  * <li>Execute all modifiers with {@link Operation#ADD_NUMBER}.
	  * <li>Set Y = X.</li>
	  * <li>Execute all modifiers with {@link Operation#MULTIPLY_PERCENTAGE}.</li>
	  * <li>Execute all modifiers with {@link Operation#ADD_PERCENTAGE}.</li>
	  * <li>Y is the final value.</li>
	  * </ol>
	  * @author Kristian
	  */
	 public enum Operation {
		/**
		 * Increment X by amount.
		 */
		ADD_NUMBER(0),
	
		/**
		 * Increment Y by X * amount.
		 */
		MULTIPLY_PERCENTAGE(1),
		
		/**
		 * Multiply Y by (1 + amount)
		 */
		ADD_PERCENTAGE(2);
		
		private int id;
		
		private Operation(int id) {
			this.id = id;
		}
		
		/**
		 * Retrieve the unique operation ID.
		 * @return Operation ID.
		 */
		public int getId() {
			return id;
		}
		
		/**
		 * Retrieve the associated operation from an ID.
		 * @param id - the ID.
		 * @return The operation.
		 */
		public static Operation fromId(int id) {
			// Linear scan is very fast for small N
			for (Operation op : values()) {
				if (op.getId() == id) {
					return op;
				}
			}
			throw new IllegalArgumentException("Corrupt operation ID " + id + " detected.");
		}
	 }
		 
	// Shared structure modifier
	private static StructureModifier<Object> BASE_MODIFIER;
	
	// The constructor we are interested in
	private static Constructor<?> ATTRIBUTE_MODIFIER_CONSTRUCTOR;
	
	// A modifier for the wrapped handler
	protected StructureModifier<Object> modifier;
	
	// Cached values
	private final UUID uuid;
	private final String name;
	private final Operation operation;
	private final double amount;
	
	/**
	 * Construct a new wrapped attribute modifier with no associated handle.
	 * <p>
	 * Note that the handle object is not initialized after this constructor.
	 * @param uuid - the UUID.
	 * @param name - the human readable name.
	 * @param amount - the amount.
	 * @param operation - the operation.
	 */
	protected WrappedAttributeModifier(UUID uuid, String name, double amount, Operation operation) {
		super(MinecraftReflection.getAttributeModifierClass());
		
		// Use the supplied values instead of reading from the NMS instance
		this.uuid = uuid;
		this.name = name;
		this.amount = amount;
		this.operation = operation;
	}
	
	/**
	 * Construct an attribute modifier wrapper around a given NMS instance.
	 * @param handle - the NMS instance.
	 */
	protected WrappedAttributeModifier(@Nonnull Object handle) {
		// Update handle and modifier
		super(MinecraftReflection.getAttributeModifierClass());
		setHandle(handle);
		initializeModifier(handle);
		
		// Load final values, caching them
		this.uuid = (UUID) modifier.withType(UUID.class).read(0);
	    this.name = (String) modifier.withType(String.class).read(0);
	    this.amount = (Double) modifier.withType(double.class).read(0);
	    this.operation = Operation.fromId((Integer) modifier.withType(int.class).read(0));
	}
	
	/**
	 * Construct an attribute modifier wrapper around a NMS instance.
	 * @param handle - the NMS instance.
	 * @param uuid - the UUID.
	 * @param name - the human readable name.
	 * @param amount - the amount.
	 * @param operation - the operation.
	 */
	protected WrappedAttributeModifier(@Nonnull Object handle, UUID uuid, String name, double amount, Operation operation) {
		this(uuid, name, amount, operation);
		
		// Initialize handle and modifier
		setHandle(handle);
		initializeModifier(handle);
	}
	
	/**
	 * Construct a new attribute modifier builder.
	 * <p>
	 * It will automatically be supplied with a random UUID.
	 * @return The new builder.
	 */
	public static Builder newBuilder() {
		return new Builder(null).uuid(UUID.randomUUID());
	}
	
	/**
	 * Construct a new attribute modifier builder with the given UUID.
	 * @param id - the new UUID.
	 * @return Thew new builder.
	 */
	public static Builder newBuilder(UUID id) {
		return new Builder(null).uuid(id);
	}
	
	/**
	 * Construct a new wrapped attribute modifier builder initialized to the values from a template.
	 * @param template - the attribute modifier template.
	 * @return The new builder.
	 */
	public static Builder newBuilder(@Nonnull WrappedAttributeModifier template) {
		return new Builder(Preconditions.checkNotNull(template, "template cannot be NULL."));
	}
	
	/**
	 * Construct an attribute modifier wrapper around a given NMS instance.
	 * @param handle - the NMS instance.
	 * @return The created attribute modifier.
	 * @throws IllegalArgumentException If the handle is not an AttributeModifier.
	 */
	public static WrappedAttributeModifier fromHandle(@Nonnull Object handle) {
		return new WrappedAttributeModifier(handle);
	}
		
	/**
	 * Initialize modifier from a given handle.
	 * @param handle - the handle.
	 * @return The given handle.
	 */
	private void initializeModifier(@Nonnull Object handle) {
		// Initialize modifier
		if (BASE_MODIFIER == null) {
			BASE_MODIFIER = new StructureModifier<Object>(MinecraftReflection.getAttributeModifierClass());
		}
		this.modifier = BASE_MODIFIER.withTarget(handle);
	}
	
	/**
	 * Retrieve the unique UUID that identifies the origin of this modifier.
	 * @return The unique UUID.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Retrieve a human readable name of this modifier.
	 * <p>
	 * Note that this will be "Unknown synced attribute modifier" on the client side.
	 * @return The attribute key.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the operation that is used to compute the final attribute value.
	 * @return The operation.
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * Retrieve the amount to modify in the operation.
	 * @return The amount.
	 */
	public double getAmount() {
		return amount;
	}
	
	/**
	 * Retrieve the underlying attribute modifier.
	 * @return The underlying modifier.
	 */
	public Object getHandle() {
		return handle;
	}
	
	/**
	 * Set whether or not the modifier is pending synchronization with the client.
	 * <p>
	 * This value will be disregarded for {@link #equals(Object)}.
	 * @param pending - TRUE if is is, FALSE otherwise.
	 */
	public void setPendingSynchronization(boolean pending) {
		modifier.withType(boolean.class).write(0, pending);
	}
	
	/**
	 * Whether or not the modifier is pending synchronization with the client.
	 * @return TRUE if it is, FALSE otherwise.
	 */
	public boolean isPendingSynchronization() {
		return (Boolean) modifier.withType(boolean.class).read(0);
	}

	/**
	 * Determine if a given modifier is equal to the current modifier.
	 * <p>
	 * Two modifiers are considered equal if they use the same UUID.
	 * @param obj - the object to check against.
	 * @return TRUE if the given object is the same, FALSE otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof WrappedAttributeModifier) {
			WrappedAttributeModifier other = (WrappedAttributeModifier) obj;
			
			// Ensure they are equal
			return Objects.equal(uuid, other.getUUID());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return uuid != null ? uuid.hashCode() : 0;
	}
	
	@Override
	public String toString() {
		return "[amount=" + amount + ", operation=" + operation + ", name='" + name + "', id=" + uuid + ", serialize=" + isPendingSynchronization() + "]";
	}

	/**
	 * Represents a builder of attribute modifiers.
	 * <p>
	 * Use {@link WrappedAttributeModifier#newBuilder()} to construct an instance of the builder.
	 * @author Kristian
	 */
	public static class Builder {
		private Operation operation = Operation.ADD_NUMBER;
		private String name = "Unknown";
		private double amount;
		private UUID uuid;

		private Builder(WrappedAttributeModifier template) {
			if (template != null) {
				operation = template.getOperation();
				name = template.getName();
				amount = template.getAmount();
				uuid = template.getUUID();
			}
		}

		/**
		 * Set the unique UUID that identifies the origin of this modifier.
		 * <p>
		 * This parameter is automatically supplied with a random UUID, or the
		 * UUID from an attribute modifier to clone.
		 * 
		 * @param uuid - the uuid to supply to the new object.
		 * @return This builder, for chaining.
		 */
		public Builder uuid(@Nonnull UUID uuid) {
			this.uuid = Preconditions.checkNotNull(uuid, "uuid cannot be NULL.");
			return this;
		}

		/**
		 * Set the operation that is used to compute the final attribute value.
		 * 
		 * @param operation - the operation to supply to the new object.
		 * @return This builder, for chaining.
		 */
		public Builder operation(@Nonnull Operation operation) {
			this.operation = Preconditions.checkNotNull(operation, "operation cannot be NULL.");
			return this;
		}

		/**
		 * Set a human readable name of this modifier.
		 * @param name - the name of the modifier.
		 * @return This builder, for chaining.
		 */
		public Builder name(@Nonnull String name) {
			this.name = Preconditions.checkNotNull(name, "name cannot be NULL.");
			return this;
		}

		/**
		 * Set the amount to modify in the operation.
		 * 
		 * @param amount - the amount to supply to the new object.
		 * @return This builder, for chaining.
		 */
		public Builder amount(double amount) {
			this.amount = WrappedAttribute.checkDouble(amount);
			return this;
		}

		/**
		 * Construct a new attribute modifier and its wrapper using the supplied values in this builder.
		 * @return The new attribute modifier.
		 * @throws NullPointerException If UUID has not been set.
		 * @throws RuntimeException If we are unable to construct the underlying attribute modifier.
		 */
		public WrappedAttributeModifier build() {
			Preconditions.checkNotNull(uuid, "uuid cannot be NULL.");

			// Retrieve the correct constructor
			if (ATTRIBUTE_MODIFIER_CONSTRUCTOR == null) {
				ATTRIBUTE_MODIFIER_CONSTRUCTOR = FuzzyReflection.fromClass(
						MinecraftReflection.getAttributeModifierClass(), true).getConstructor(
						FuzzyMethodContract.newBuilder().parameterCount(4).
							parameterDerivedOf(UUID.class, 0).
							parameterExactType(String.class, 1).
							parameterExactType(double.class, 2).
							parameterExactType(int.class, 3).build());

				// Just in case
				ATTRIBUTE_MODIFIER_CONSTRUCTOR.setAccessible(true);
			}

			// Construct it
			try {
				// No need to read these values with a modifier
				return new WrappedAttributeModifier(
					ATTRIBUTE_MODIFIER_CONSTRUCTOR.newInstance(
						uuid, name, amount, operation.getId()),
					uuid, name, amount, operation
				);
			} catch (Exception e) {
				throw new RuntimeException("Cannot construct AttributeModifier.", e);
			}
		}
	}
}
