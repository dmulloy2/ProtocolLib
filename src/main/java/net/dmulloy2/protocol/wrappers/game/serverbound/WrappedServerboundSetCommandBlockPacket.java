package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Arrays;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSetCommandBlockPacket} (game phase, serverbound).
 *
 * <p>NMS constructor order: {@code (BlockPos pos, String command, Mode mode,
 * boolean trackOutput, boolean conditional, boolean automatic)}.
 */
public class WrappedServerboundSetCommandBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_COMMAND_BLOCK;

    /**
     * Mirrors {@code CommandBlockEntity.Mode}: constants must match NMS names exactly.
     * Global field index of {@code mode} is 5.
     */
    public enum Mode { SEQUENCE, AUTO, REDSTONE }

    // The only enum field in this packet is CommandBlockEntity.Mode
    private static final Class<?> MODE_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
            .map(java.lang.reflect.Field::getType)
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    // NMS constructor order: (BlockPos, String, Mode, boolean, boolean, boolean)
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(String.class)
            .withParam(MODE_NMS_CLASS, new EnumWrappers.EnumConverter<>(MODE_NMS_CLASS, Mode.class))
            .withParam(boolean.class)
            .withParam(boolean.class)
            .withParam(boolean.class);

    public WrappedServerboundSetCommandBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetCommandBlockPacket(BlockPosition pos, String command,
            boolean trackOutput, boolean conditional, boolean automatic, Mode mode) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(pos, command, mode, trackOutput, conditional, automatic)));
    }

    public WrappedServerboundSetCommandBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().readSafely(0);
    }

    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().writeSafely(0, pos);
    }

    public String getCommand() {
        return handle.getStrings().readSafely(0);
    }

    public void setCommand(String command) {
        handle.getStrings().writeSafely(0, command);
    }

    public boolean isTrackOutput() {
        return handle.getBooleans().readSafely(0);
    }

    public void setTrackOutput(boolean trackOutput) {
        handle.getBooleans().writeSafely(0, trackOutput);
    }

    public boolean isConditional() {
        return handle.getBooleans().readSafely(1);
    }

    public void setConditional(boolean conditional) {
        handle.getBooleans().writeSafely(1, conditional);
    }

    public boolean isAutomatic() {
        return handle.getBooleans().readSafely(2);
    }

    public void setAutomatic(boolean automatic) {
        handle.getBooleans().writeSafely(2, automatic);
    }

    /** Returns the command block mode (global field index 5). */
    public Mode getMode() {
        return handle.getEnumModifier(Mode.class, 5).readSafely(0);
    }

    public void setMode(Mode mode) {
        handle.getEnumModifier(Mode.class, 5).writeSafely(0, mode);
    }
}
