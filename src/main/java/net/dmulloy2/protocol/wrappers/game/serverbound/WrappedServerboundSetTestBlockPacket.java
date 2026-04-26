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
 * Wrapper for {@code ServerboundSetTestBlockPacket} (game phase, serverbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code BlockPos position} – the position of the test block</li>
 *   <li>{@code TestBlockMode mode} – the test block mode</li>
 *   <li>{@code String message} – the message to display</li>
 * </ul>
 */
public class WrappedServerboundSetTestBlockPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SET_TEST_BLOCK;

    /**
     * Mode of the test block, matching {@code net.minecraft.world.level.block.state.properties.TestBlockMode}.
     */
    public enum TestBlockMode {
        START, LOG, FAIL, ACCEPT
    }

    private static final Class<?> MODE_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
            .map(java.lang.reflect.Field::getType)
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(MODE_NMS_CLASS, new EnumWrappers.EnumConverter<>(MODE_NMS_CLASS, TestBlockMode.class))
            .withParam(String.class);

    public WrappedServerboundSetTestBlockPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetTestBlockPacket(BlockPosition position, TestBlockMode mode, String message) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(position, mode, message)));
    }

    public WrappedServerboundSetTestBlockPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getPosition() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setPosition(BlockPosition position) {
        handle.getBlockPositionModifier().write(0, position);
    }

    public TestBlockMode getMode() {
        return handle.getEnumModifier(TestBlockMode.class, 1).read(0);
    }

    public void setMode(TestBlockMode mode) {
        handle.getEnumModifier(TestBlockMode.class, 1).write(0, mode);
    }

    public String getMessage() {
        return handle.getStrings().read(0);
    }

    public void setMessage(String message) {
        handle.getStrings().write(0, message);
    }
}
