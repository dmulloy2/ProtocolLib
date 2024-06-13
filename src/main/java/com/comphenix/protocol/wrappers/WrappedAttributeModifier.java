package com.comphenix.protocol.wrappers;

import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;

import com.google.common.base.Preconditions;

/**
 * Represents a wrapper around a AttributeModifier.
 * <p>
 * This is used to compute the final attribute value.
 * 
 * @author Kristian
 */
public class WrappedAttributeModifier extends AbstractWrapper {
    private static final boolean OPERATION_ENUM = MinecraftVersion.VILLAGE_UPDATE.atOrAbove();
    static final boolean USES_RESOURCE_LOCATION = MinecraftVersion.v1_21_0.atOrAbove();

    private static final Class<?> OPERATION_CLASS;
    private static final EquivalentConverter<Operation> OPERATION_CONVERTER;

    private static class IndexedEnumConverter<T extends Enum<T>> implements EquivalentConverter<T> {
        private final Class<T> specificClass;
        private final Class<?> genericClass;

        private IndexedEnumConverter(Class<T> specificClass, Class<?> genericClass) {
            this.specificClass = specificClass;
            this.genericClass = genericClass;
        }

        @Override
        public Object getGeneric(T specific) {
            int ordinal = specific.ordinal();
            for (Object elem : genericClass.getEnumConstants()) {
                if (((Enum<?>) elem).ordinal() == ordinal) {
                    return elem;
                }
            }

            return null;
        }

        @Override
        public T getSpecific(Object generic) {
            int ordinal = ((Enum<?>) generic).ordinal();
            for (T elem : specificClass.getEnumConstants()) {
                if (elem.ordinal() == ordinal) {
                    return elem;
                }
            }

            return null;
        }

        @Override
        public Class<T> getSpecificType() {
            return specificClass;
        }
    }

    static {
        OPERATION_CLASS = OPERATION_ENUM ? MinecraftReflection.getMinecraftClass(
                "world.entity.ai.attributes.AttributeModifier$Operation", "AttributeModifier$Operation"
        ) : null;
        OPERATION_CONVERTER = OPERATION_ENUM ? new IndexedEnumConverter<>(Operation.class, OPERATION_CLASS) : null;
    }

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
        
        private final int id;
        
        Operation(int id) {
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

    // The constructor we are interested in
    private static ConstructorAccessor ATTRIBUTE_MODIFIER_CONSTRUCTOR;
    
    // A modifier for the wrapped handler
    protected InternalStructure modifier;
    
    // Cached values
    @Nullable
    private final MinecraftKey key;

    @Nullable
    private final UUID uuid;

    private final Supplier<String> name;
    private final Operation operation;
    private final double amount;

    /**
     * Construct an attribute modifier wrapper around a given NMS instance.
     * @param handle - the NMS instance.
     */
    @SuppressWarnings("unchecked")
    protected WrappedAttributeModifier(@Nonnull Object handle) {
        // Update handle and modifier
        super(MinecraftReflection.getAttributeModifierClass());
        setHandle(handle);
        initializeModifier(handle);

        if (USES_RESOURCE_LOCATION) {
            this.key = modifier.getMinecraftKeys().read(0);
            this.uuid = null;
            this.name = () -> null;
        } else {
            this.key = null;
            this.uuid = modifier.getUUIDs().read(0);

            Optional<String> name = modifier.getStrings().optionRead(0);
            if (name.isPresent()) {
                this.name = name::get;
            } else {
                this.name = (Supplier<String>) modifier.getModifier().withType(Supplier.class).read(0);
            }
        }

        this.amount = modifier.getDoubles().read(0);

        if (OPERATION_ENUM) {
            this.operation = modifier.getModifier().withType(OPERATION_CLASS, OPERATION_CONVERTER).readSafely(0);
        } else {
            this.operation = Operation.fromId(modifier.getIntegers().readSafely(0));
        }
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
        super(MinecraftReflection.getAttributeModifierClass());

        this.uuid = uuid;
        this.name = () -> name;
        this.amount = amount;
        this.operation = operation;
        this.key = null;
        
        // Initialize handle and modifier
        setHandle(handle);
        initializeModifier(handle);
    }

    protected WrappedAttributeModifier(Object handle, MinecraftKey key, double amount, Operation operation) {
        super(MinecraftReflection.getAttributeModifierClass());

        this.uuid = null;
        this.key = key;
        this.amount = amount;
        this.name = key::toString;
        this.operation = operation;

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
     */
    private void initializeModifier(@Nonnull Object handle) {
        this.modifier = InternalStructure.getConverter().getSpecific(handle);
    }

    public MinecraftKey getKey() {
        return key;
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
        return name.get();
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
     * Set whether the modifier is pending synchronization with the client.
     * <p>
     * This value will be disregarded for {@link #equals(Object)}.
     * @param pending - TRUE if it is pending, FALSE otherwise.
     */
    public void setPendingSynchronization(boolean pending) {
        modifier.getBooleans().write(0, pending);
    }
    
    /**
     * Whether the modifier is pending synchronization with the client.
     * @return TRUE if it is, FALSE otherwise.
     */
    public boolean isPendingSynchronization() {
        return modifier.getBooleans().optionRead(0).orElse(false);
    }

    /**
     * Determine if a given modifier is equal to the current modifier.
     * <p>
     * Two modifiers are considered equal if they use the same UUID.
     * @param obj - the object to check against.
     * @return TRUE if the given object is the same, FALSE otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;

        if (obj instanceof WrappedAttributeModifier) {
            WrappedAttributeModifier other = (WrappedAttributeModifier) obj;
            if (USES_RESOURCE_LOCATION) {
                return this.key.equals(other.key);
            } else {
                return this.uuid.equals(other.uuid);
            }

        }

        return false;
    }
    
    @Override
    public int hashCode() {
        if (USES_RESOURCE_LOCATION) {
            return key != null ? key.hashCode() : 0;
        } else {
            return uuid != null ? uuid.hashCode() : 0;
        }
    }
    
    @Override
    public String toString() {
        return "[key=" + key + ", amount=" + amount + ", operation=" + operation + "]";
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
        private MinecraftKey key;

        private Builder(WrappedAttributeModifier template) {
            if (template != null) {
                this.operation = template.getOperation();
                this.name = template.getName();
                this.amount = template.getAmount();
                this.uuid = template.getUUID();
                this.key = template.getKey();
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
        @Deprecated
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
        @Deprecated
        public Builder name(@Nonnull String name) {
            this.name = Preconditions.checkNotNull(name, "name cannot be NULL.");
            return this;
        }

        public Builder key(@Nonnull MinecraftKey key) {
            this.key = Preconditions.checkNotNull(key, "key cannot be NULL.");
            return this;
        }

        public Builder key(String prefix, String value) {
            return key(new MinecraftKey(prefix, value));
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
                ATTRIBUTE_MODIFIER_CONSTRUCTOR = getConstructor();
            }

            // Construct it
            try {
                if (USES_RESOURCE_LOCATION) {
                    if (key == null) {
                        UUID uuid = this.uuid != null ? this.uuid : UUID.randomUUID();
                        key = new MinecraftKey("protocollib", uuid.toString());
                    }

                    Object handle = ATTRIBUTE_MODIFIER_CONSTRUCTOR.invoke(
                        MinecraftKey.getConverter().getGeneric(key),
                        amount,
                        OPERATION_CONVERTER.getGeneric(operation)
                    );

                    return new WrappedAttributeModifier(handle, key, amount, operation);
                } else {
                    Object handle = ATTRIBUTE_MODIFIER_CONSTRUCTOR.invoke(
                        uuid, name, amount, getOperationParam(operation));

                    return new WrappedAttributeModifier(
                        handle,
                        uuid, name, amount, operation
                    );
                }
            } catch (Exception e) {
                throw new RuntimeException("Cannot construct AttributeModifier.", e);
            }
        }
    }

    private static Object getOperationParam(Operation operation) {
        return OPERATION_ENUM ? OPERATION_CONVERTER.getGeneric(operation) : operation.getId();
    }

    private static ConstructorAccessor getConstructor() {
        FuzzyMethodContract.Builder builder = FuzzyMethodContract.newBuilder();

        if (USES_RESOURCE_LOCATION) {
            builder.parameterCount(3)
                .parameterDerivedOf(MinecraftReflection.getMinecraftKeyClass())
                .parameterExactType(double.class)
                .parameterExactType(OPERATION_CLASS);
        } else {
            builder.parameterCount(4)
                .parameterDerivedOf(UUID.class, 0)
                .parameterExactType(String.class, 1)
                .parameterExactType(double.class, 2);

            if (OPERATION_ENUM) {
                builder.parameterExactType(OPERATION_CLASS, 3);
            } else {
                builder.parameterExactType(int.class, 3);
            }
        }

        Constructor<?> ret = FuzzyReflection
            .fromClass(MinecraftReflection.getAttributeModifierClass(), true)
            .getConstructor(builder.build());
        return Accessors.getConstructorAccessor(ret);
    }
}
