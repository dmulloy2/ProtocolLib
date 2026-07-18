package com.comphenix.protocol.wrappers;

import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

import org.bukkit.util.Vector;

/**
 * Represents an entity used action used in the UseEntity packet sent by the client.
 * @author derklaro
 */
public class WrappedEnumEntityUseAction extends AbstractWrapper implements ClonableWrapper {

    public static final EquivalentConverter<WrappedEnumEntityUseAction> CONVERTER = Converters.handle(AbstractWrapper::getHandle,
            WrappedEnumEntityUseAction::fromHandle, WrappedEnumEntityUseAction.class);

    private static final Class<?> PACKET_CLASS = PacketType.Play.Client.USE_ENTITY.getPacketClass();
    private static final Class<?>[] DECLARED_CLASSES = PACKET_CLASS.getDeclaredClasses();

    private static final Class<?> HANDLE_TYPE = MinecraftReflection.getEnumEntityUseActionClass();
    private static final MethodAccessor ACTION_USE = getActionUseAccessor();

    private static final ConstructorAccessor PACKET_CONSTRUCTOR = MinecraftVersion.v26_1.atOrAbove()
            ? Accessors.getConstructorAccessor(PACKET_CLASS, int.class, EnumWrappers.getHandClass(),
                    MinecraftReflection.getVec3DClass(), boolean.class)
            : null;
    private static final ConstructorAccessor INTERACT = MinecraftVersion.v26_1.atOrAbove()
            ? null
            : useAction(EnumWrappers.getHandClass());
    private static final ConstructorAccessor INTERACT_AT = MinecraftVersion.v26_1.atOrAbove()
            ? null
            : useAction(EnumWrappers.getHandClass(), MinecraftReflection.getVec3DClass());

    private static final Object ATTACK = initializeAttack();
    private static final WrappedEnumEntityUseAction ATTACK_WRAPPER = ATTACK != null ? new WrappedEnumEntityUseAction(ATTACK) : null;

    private final EntityUseAction action;
    // these fields are only available for interact & interact_at
    private FieldAccessor handAccessor;
    private FieldAccessor positionAccessor;

    /**
     * Construct a new wrapper for the entity use action class in the UseEntity packet.
     * @param handle - the NMS handle.
     */
    private WrappedEnumEntityUseAction(Object handle) {
        super(HANDLE_TYPE);
        setHandle(handle);

        action = resolveAction();
    }

    private static MethodAccessor getActionUseAccessor() {
        return MinecraftReflection.getEntityUseActionEnumMethodAccessor();
    }

    private static Object initializeAttack() {
        if (MinecraftVersion.v26_1.atOrAbove()) {
            return null;
        }

        return Accessors.getFieldAccessor(FuzzyReflection.fromClass(PACKET_CLASS, true)
                .getField(FuzzyFieldContract.newBuilder()
                        .requireModifier(Modifier.STATIC)
                        .typeExact(MinecraftReflection.getEnumEntityUseActionClass())
                        .build())
        ).get(null);
    }

    private EntityUseAction resolveAction() {
        if (ACTION_USE != null) {
            return EnumWrappers.getEntityUseActionConverter().getSpecific(ACTION_USE.invoke(handle));
        }

        return EntityUseAction.INTERACT_AT;
    }

    /**
     * Finds a constructor of a declared class in the UseEntity class. Used to find the action class implementations.
     * @param parameterTypes - the types the constructor of the class must have.
     * @return a constructor for a matching class.
     * @throws IllegalArgumentException if no constructor was found.
     */
    private static ConstructorAccessor useAction(Class<?>... parameterTypes) {
        for (Class<?> subClass : DECLARED_CLASSES) {
            ConstructorAccessor accessor = Accessors.getConstructorAccessorOrNull(subClass, parameterTypes);
            if (accessor != null) {
                return accessor;
            }
        }
        throw new IllegalArgumentException(
                "No constructor with " + Arrays.toString(parameterTypes) + " in " + PACKET_CLASS);
    }

    /**
     * Construct a new wrapper for the entity use action class in the UseEntity packet.
     * @param handle - the NMS handle.
     * @return the created wrapper.
     */
    public static WrappedEnumEntityUseAction fromHandle(Object handle) {
        return new WrappedEnumEntityUseAction(handle);
    }

    /**
     * Get the jvm static action for attacking an entity.
     * @return the action for an entity attack.
     */
    public static WrappedEnumEntityUseAction attack() {
        if (ATTACK_WRAPPER == null) {
            throw new UnsupportedOperationException("Attack is no longer part of the USE_ENTITY packet on 26.1+");
        }

        return ATTACK_WRAPPER;
    }

    /**
     * Get an action for interacting with an entity.
     * @param hand - the hand used for the interact.
     * @return the action for an interact.
     */
    public static WrappedEnumEntityUseAction interact(Hand hand) {
        Object handle = MinecraftVersion.v26_1.atOrAbove()
                ? PACKET_CONSTRUCTOR.invoke(0, EnumWrappers.getHandConverter().getGeneric(hand),
                        BukkitConverters.getVectorConverter().getGeneric(new Vector()), false)
                : INTERACT.invoke(EnumWrappers.getHandConverter().getGeneric(hand));
        return new WrappedEnumEntityUseAction(handle);
    }

    /**
     * Get an action for interacting with an entity at a specific location.
     * @param hand - the hand used for the interact.
     * @param vector - the position of the interact.
     * @return the action for an interact_at.
     */
    public static WrappedEnumEntityUseAction interactAt(Hand hand, Vector vector) {
        Object handle = MinecraftVersion.v26_1.atOrAbove()
                ? PACKET_CONSTRUCTOR.invoke(0, EnumWrappers.getHandConverter().getGeneric(hand),
                        BukkitConverters.getVectorConverter().getGeneric(vector), false)
                : INTERACT_AT.invoke(EnumWrappers.getHandConverter().getGeneric(hand),
                        BukkitConverters.getVectorConverter().getGeneric(vector));
        return new WrappedEnumEntityUseAction(handle);
    }

    /**
     * Get the action used for the interact.
     * @return the interact action.
     */
    public EntityUseAction getAction() {
        return action;
    }

    /**
     * Get the hand used for the interact. Only available if this represents interact or interact_at.
     * @return the hand used for the interact.
     * @throws IllegalArgumentException if called for attack.
     */
    public Hand getHand() {
        return EnumWrappers.getHandConverter().getSpecific(getHandAccessor().get(handle));
    }

    /**
     * Sets the hand used for the interact.
     * @param hand the used hand.
     * @throws IllegalArgumentException if called for attack.
     */
    public void setHand(Hand hand) {
        getHandAccessor().set(handle, EnumWrappers.getHandConverter().getGeneric(hand));
    }

    /**
     * Get the position of the interact. Only available if this represents interact_at.
     * @return the position of the interact.
     * @throws IllegalArgumentException if called for attack or interact.
     */
    public Vector getPosition() {
        return getRawPosition(handle);
    }

    /**
     * Sets the position of the interact.
     * @param position the position.
     * @throws IllegalArgumentException if called for attack or interact.
     */
    public void setPosition(Vector position) {
        getPositionAccessor().set(handle, BukkitConverters.getVectorConverter().getGeneric(position));
    }

    @Override
    public WrappedEnumEntityUseAction deepClone() {
        switch (getAction()) {
            case ATTACK:
                return WrappedEnumEntityUseAction.attack();
            case INTERACT:
                return WrappedEnumEntityUseAction.interact(getHand());
            case INTERACT_AT:
                return WrappedEnumEntityUseAction.interactAt(getHand(), getPosition());
            default:
                throw new IllegalArgumentException("Invalid EntityUseAction: " + getAction());
        }
    }

    /**
     * Get a field accessor for the hand in the interact and interact_at type.
     * @return a field accessor for the hand field.
     * @throws IllegalArgumentException if called for attack.
     */
    private FieldAccessor getHandAccessor() {
        if (handAccessor == null) {
            handAccessor = MinecraftReflection.getHandEntityUseActionEnumFieldAccessor(handle);
        }
        return handAccessor;
    }

    /**
     * Get a field accessor for the position in the interact_at type.
     * @return a field accessor for the position field.
     * @throws IllegalArgumentException if called for attack or interact.
     */
    public FieldAccessor getPositionAccessor() {
        if (positionAccessor == null) {
            positionAccessor = MinecraftReflection.getVec3EntityUseActionEnumFieldAccessor(handle);
        }
        return positionAccessor;
    }

    private Vector getRawPosition(Object target) {
        return BukkitConverters.getVectorConverter().getSpecific(getPositionAccessor().get(target));
    }
}
