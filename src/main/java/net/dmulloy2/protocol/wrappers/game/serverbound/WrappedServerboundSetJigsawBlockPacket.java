package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Arrays;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetJigsawBlockPacket} (game phase, serverbound).
 *
 * <p>NMS field order: {@code pos, name, target, pool, finalState, joint, selectionPriority, placementPriority}
 */
public class WrappedServerboundSetJigsawBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_JIGSAW;

    /**
     * Mirrors {@code JigsawBlockEntity.JointType}: constants must match NMS names exactly.
     * Global field index of {@code joint} is 5.
     */
    public enum JointType { ROLLABLE, ALIGNED }

    private static final Class<?> JOINT_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
            .map(java.lang.reflect.Field::getType)
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter())
            .withParam(String.class)
            .withParam(JOINT_NMS_CLASS, new EnumWrappers.EnumConverter<>(JOINT_NMS_CLASS, JointType.class))
            .withParam(int.class)
            .withParam(int.class);

    public WrappedServerboundSetJigsawBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetJigsawBlockPacket(BlockPosition pos, MinecraftKey name, MinecraftKey target,
            MinecraftKey pool, String finalState, JointType joint,
            int selectionPriority, int placementPriority) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(pos, name, target, pool, finalState, joint, selectionPriority, placementPriority)));
    }

    public WrappedServerboundSetJigsawBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // -------------------------------------------------------------------------
    // pos — global index 0
    // -------------------------------------------------------------------------

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    // -------------------------------------------------------------------------
    // name / target / pool — MinecraftKey fields, indices 0 / 1 / 2
    // -------------------------------------------------------------------------

    /** Returns the jigsaw block's attachment-type name (global index 1). */
    public MinecraftKey getName() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setName(MinecraftKey name) {
        handle.getMinecraftKeys().write(0, name);
    }

    /** Returns the target pool-element name (global index 2). */
    public MinecraftKey getTarget() {
        return handle.getMinecraftKeys().read(1);
    }

    public void setTarget(MinecraftKey target) {
        handle.getMinecraftKeys().write(1, target);
    }

    /** Returns the template pool (global index 3). */
    public MinecraftKey getPool() {
        return handle.getMinecraftKeys().read(2);
    }

    public void setPool(MinecraftKey pool) {
        handle.getMinecraftKeys().write(2, pool);
    }

    // -------------------------------------------------------------------------
    // finalState — String, index 0
    // -------------------------------------------------------------------------

    public String getFinalState() {
        return handle.getStrings().read(0);
    }

    public void setFinalState(String finalState) {
        handle.getStrings().write(0, finalState);
    }

    // -------------------------------------------------------------------------
    // joint — JigsawBlockEntity.JointType enum, global index 5
    // -------------------------------------------------------------------------

    public JointType getJoint() {
        return handle.getEnumModifier(JointType.class, 5).read(0);
    }

    public void setJoint(JointType joint) {
        handle.getEnumModifier(JointType.class, 5).write(0, joint);
    }

    // -------------------------------------------------------------------------
    // selectionPriority / placementPriority — int, indices 0 / 1
    // -------------------------------------------------------------------------

    public int getSelectionPriority() {
        return handle.getIntegers().read(0);
    }

    public void setSelectionPriority(int selectionPriority) {
        handle.getIntegers().write(0, selectionPriority);
    }

    public int getPlacementPriority() {
        return handle.getIntegers().read(1);
    }

    public void setPlacementPriority(int placementPriority) {
        handle.getIntegers().write(1, placementPriority);
    }
}
