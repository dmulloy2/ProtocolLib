package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.Converters;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerClickPacket} (game phase, serverbound).
 *
 * <p>NMS field order: {@code containerId, stateId, slotNum, buttonNum, containerInput (global 4),
 * changedSlots (global 5), carriedItem (global 6)}
 */
public class WrappedServerboundContainerClickPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.WINDOW_CLICK;

    private static final Class<?> CONTAINER_INPUT_CLASS =
            MinecraftReflection.getMinecraftClass("world.inventory.ContainerInput");
    private static final Class<?> INT2OBJECT_MAP_CLASS = MinecraftReflection.getInt2ObjectMapClass();
    private static final Class<?> INT2OBJECT_OPEN_HASH_MAP_CLASS =
            MinecraftReflection.getFastUtilClass("ints.Int2ObjectOpenHashMap");
    private static final Class<?> HASHED_STACK_CLASS =
            MinecraftReflection.getMinecraftClass("network.HashedStack");

    private static final ConstructorAccessor INT2OBJECT_OPEN_HASH_MAP_CONSTRUCTOR =
            Accessors.getConstructorAccessor(INT2OBJECT_OPEN_HASH_MAP_CLASS);
    private static final FieldAccessor EMPTY_HASHED_STACK =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(HASHED_STACK_CLASS, "EMPTY", HASHED_STACK_CLASS));

    private static final EquivalentConverter<ContainerInput> CONTAINER_INPUT_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(ContainerInput specific) {
                    return containerInput(specific);
                }

                @Override
                public ContainerInput getSpecific(Object generic) {
                    return ContainerInput.valueOf(((Enum<?>) generic).name());
                }

                @Override
                public Class<ContainerInput> getSpecificType() {
                    return ContainerInput.class;
                }
            });

    private static final EquivalentConverter<HashedStack> HASHED_STACK_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(HashedStack specific) {
                    return specific.handle;
                }

                @Override
                public HashedStack getSpecific(Object generic) {
                    return HashedStack.fromHandle(generic);
                }

                @Override
                public Class<HashedStack> getSpecificType() {
                    return HashedStack.class;
                }
            });

    private static final EquivalentConverter<Map<Integer, HashedStack>> CHANGED_SLOTS_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(Map<Integer, HashedStack> specific) {
                    Object map = INT2OBJECT_OPEN_HASH_MAP_CONSTRUCTOR.invoke();
                    asMap(map).putAll(convertToNmsMap(specific));
                    return map;
                }

                @Override
                public Map<Integer, HashedStack> getSpecific(Object generic) {
                    Map<Integer, HashedStack> result = new LinkedHashMap<>();
                    asMap(generic).forEach((slot, item) ->
                            result.put((Integer) slot, HASHED_STACK_CONVERTER.getSpecific(item)));
                    return result;
                }

                @SuppressWarnings({"unchecked", "rawtypes"})
                @Override
                public Class<Map<Integer, HashedStack>> getSpecificType() {
                    return (Class) Map.class;
                }
            });

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class)
            .withParam(short.class)
            .withParam(byte.class)
            .withParam(CONTAINER_INPUT_CLASS, CONTAINER_INPUT_CONVERTER)
            .withParam(INT2OBJECT_MAP_CLASS, CHANGED_SLOTS_CONVERTER)
            .withParam(HASHED_STACK_CLASS, HASHED_STACK_CONVERTER);

    /**
     * Mirrors {@code ContainerInput} enum. Constants must match NMS names exactly.
     * Global field index 4.
     */
    public enum ContainerInput {
        PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL
    }

    public WrappedServerboundContainerClickPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundContainerClickPacket(int containerId, int stateId, short slotNum,
            byte buttonNum, ContainerInput containerInput) {
        this(containerId, stateId, slotNum, buttonNum, containerInput, Map.of(), HashedStack.empty());
    }

    public WrappedServerboundContainerClickPacket(int containerId, int stateId, short slotNum,
            byte buttonNum, ContainerInput containerInput, Map<Integer, HashedStack> changedSlots,
            HashedStack carriedItem) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(
                containerId,
                stateId,
                slotNum,
                buttonNum,
                containerInput,
                changedSlots,
                carriedItem)));
    }

    public WrappedServerboundContainerClickPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public int getStateId() {
        return handle.getIntegers().read(1);
    }

    public void setStateId(int stateId) {
        handle.getIntegers().write(1, stateId);
    }

    public short getSlotNum() {
        return handle.getShorts().read(0);
    }

    public void setSlotNum(short slotNum) {
        handle.getShorts().write(0, slotNum);
    }

    public byte getButtonNum() {
        return handle.getBytes().read(0);
    }

    public void setButtonNum(byte buttonNum) {
        handle.getBytes().write(0, buttonNum);
    }

    /** Returns the inventory action type. Global field index 4. */
    public ContainerInput getContainerInput() {
        return handle.getModifier().withType(CONTAINER_INPUT_CLASS, CONTAINER_INPUT_CONVERTER).read(0);
    }

    public void setContainerInput(ContainerInput containerInput) {
        handle.getModifier().withType(CONTAINER_INPUT_CLASS, CONTAINER_INPUT_CONVERTER).write(0, containerInput);
    }

    public Map<Integer, HashedStack> getChangedSlots() {
        return handle.getModifier().withType(INT2OBJECT_MAP_CLASS, CHANGED_SLOTS_CONVERTER).read(0);
    }

    public void setChangedSlots(Map<Integer, HashedStack> changedSlots) {
        handle.getModifier().withType(INT2OBJECT_MAP_CLASS, CHANGED_SLOTS_CONVERTER).write(0, changedSlots);
    }

    public HashedStack getCarriedItem() {
        return handle.getModifier().withType(HASHED_STACK_CLASS, HASHED_STACK_CONVERTER).read(0);
    }

    public void setCarriedItem(HashedStack carriedItem) {
        handle.getModifier().withType(HASHED_STACK_CLASS, HASHED_STACK_CONVERTER).write(0, carriedItem);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object containerInput(ContainerInput containerInput) {
        return Enum.valueOf((Class) CONTAINER_INPUT_CLASS, containerInput.name());
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Object> asMap(Object map) {
        return (Map<Object, Object>) map;
    }

    private static Map<Object, Object> convertToNmsMap(Map<Integer, HashedStack> changedSlots) {
        Map<Object, Object> result = new LinkedHashMap<>();
        changedSlots.forEach((slot, item) -> result.put(slot, HASHED_STACK_CONVERTER.getGeneric(item)));
        return result;
    }

    /** Packet-local handle wrapper for {@code HashedStack}. */
    public static final class HashedStack {
        private final Object handle;

        private HashedStack(Object handle) {
            this.handle = handle;
        }

        public static HashedStack empty() {
            return new HashedStack(EMPTY_HASHED_STACK.get(null));
        }

        public static HashedStack fromHandle(Object handle) {
            return new HashedStack(Objects.requireNonNull(handle, "handle"));
        }

        public Object getHandle() {
            return handle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof HashedStack that)) {
                return false;
            }
            return Objects.equals(handle, that.handle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(handle);
        }
    }
}
