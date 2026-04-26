package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Optional;

/**
 * Wrapper for {@code ServerboundTestInstanceBlockActionPacket} (game phase, serverbound).
 *
 * <p>Fields (record components):
 * <ol>
 *   <li>{@code BlockPos pos} – the test-instance block's position</li>
 *   <li>{@code Action action} – the action to perform</li>
 *   <li>{@code TestInstanceBlockEntity.Data data} – full data record</li>
 * </ol>
 *
 * <p>The {@code Data} record is exposed as an {@link AutoWrapper} POJO via
 * {@link #getData()} / {@link #setData(WrappedData)}.
 */
public class WrappedServerboundTestInstanceBlockActionPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION;

    /** Mirrors {@code ServerboundTestInstanceBlockActionPacket.Action}. Constants must match NMS. */
    public enum Action { INIT, QUERY, SET, RESET, SAVE, EXPORT, RUN }

    /** Mirrors NMS {@code Rotation} (block-rotation enum); constant order must match. */
    public enum Rotation { NONE, CLOCKWISE_90, CLOCKWISE_180, COUNTERCLOCKWISE_90 }

    /** Mirrors NMS {@code TestInstanceBlockEntity.Status}; constant order must match. */
    public enum Status { CLEARED, RUNNING, FINISHED }

    // NMS Action enum class — found by simple name among the packet record's declared classes.
    private static final Class<?> ACTION_NMS_CLASS = findActionEnum();

    private static Class<?> findActionEnum() {
        for (Class<?> c : TYPE.getPacketClass().getDeclaredClasses()) {
            if (c.isEnum() && c.getSimpleName().equals("Action")) return c;
        }
        throw new IllegalStateException("Action enum not found");
    }

    private static final Class<?> ROTATION_NMS_CLASS =
            MinecraftReflection.getMinecraftClass("world.level.block.Rotation");
    private static final Class<?> STATUS_NMS_CLASS =
            MinecraftReflection.getMinecraftClass("world.level.block.entity.TestInstanceBlockEntity$Status");
    private static final Class<?> GAME_TEST_INSTANCE_CLASS =
            MinecraftReflection.getMinecraftClass("gametest.framework.GameTestInstance");

    /** Converter between NMS {@code ResourceKey<GameTestInstance>} and {@link MinecraftKey}. */
    private static final EquivalentConverter<MinecraftKey> TEST_KEY_CONVERTER =
            WrappedRegistry.getRegistry(GAME_TEST_INSTANCE_CLASS).resourceKeyConverter();

    private static final EnumWrappers.EnumConverter<Rotation> ROTATION_CONVERTER =
            new EnumWrappers.EnumConverter<>(ROTATION_NMS_CLASS, Rotation.class);
    private static final EnumWrappers.EnumConverter<Status> STATUS_CONVERTER =
            new EnumWrappers.EnumConverter<>(STATUS_NMS_CLASS, Status.class);

    private static final AutoWrapper<WrappedData> DATA_WRAPPER =
            AutoWrapper.wrap(WrappedData.class, "world.level.block.entity.TestInstanceBlockEntity$Data")
                    .field(0, Converters.optional(TEST_KEY_CONVERTER))
                    .field(1, BlockPosition.getConverter())
                    .field(2, ROTATION_CONVERTER)
                    .field(4, STATUS_CONVERTER)
                    .field(5, Converters.optional(BukkitConverters.getWrappedChatComponentConverter()));

    public WrappedServerboundTestInstanceBlockActionPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundTestInstanceBlockActionPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the test-instance block position. */
    public BlockPosition getPos() {
        return handle.getBlockPositionModifier().read(0);
    }

    /** Sets the test-instance block position. */
    public void setPos(BlockPosition pos) {
        handle.getBlockPositionModifier().write(0, pos);
    }

    /** Returns the action to perform. */
    public Action getAction() {
        return handle.getEnumModifier(Action.class, ACTION_NMS_CLASS).read(0);
    }

    /** Sets the action to perform. */
    public void setAction(Action action) {
        handle.getEnumModifier(Action.class, ACTION_NMS_CLASS).write(0, action);
    }

    /** Returns the test-instance data record. */
    public WrappedData getData() {
        return DATA_WRAPPER.getSpecific(handle.getModifier().read(2));
    }

    public void setData(WrappedData data) {
        handle.getModifier().write(2, DATA_WRAPPER.getGeneric(data));
    }

    /**
     * POJO mirroring {@code record TestInstanceBlockEntity.Data(
     *   Optional<ResourceKey<GameTestInstance>> test, Vec3i size, Rotation rotation,
     *   boolean ignoreEntities, Status status, Optional<Component> errorMessage)}.
     *
     * <p>Fields must be in the same order as the NMS record.
     */
    public static final class WrappedData {
        /** The selected test instance's registry key (e.g. {@code minecraft:my_test}), or empty. */
        public Optional<MinecraftKey> test = Optional.empty();
        /** Structure size — internally a {@code Vec3i}, exposed as a {@link BlockPosition}. */
        public BlockPosition size;
        public Rotation rotation;
        public boolean ignoreEntities;
        public Status status;
        public Optional<WrappedChatComponent> errorMessage = Optional.empty();

        public WrappedData() {}
    }
}
