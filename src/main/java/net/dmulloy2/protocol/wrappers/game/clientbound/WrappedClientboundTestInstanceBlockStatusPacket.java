package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.Optional;

/**
 * Wrapper for {@code ClientboundTestInstanceBlockStatus} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code Component status} – the status message</li>
 *   <li>{@code Optional<Vec3i> size} – optional test block size</li>
 * </ul>
 *
 * <p>{@code Vec3i} is stored as a {@link BlockPosition} ({@code BlockPos extends Vec3i}).
 */
public class WrappedClientboundTestInstanceBlockStatusPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TEST_INSTANCE_BLOCK_STATUS;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(),
                    BukkitConverters.getWrappedChatComponentConverter())
            .withParam(Optional.class, Converters.optional(BlockPosition.getConverter()));

    public WrappedClientboundTestInstanceBlockStatusPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTestInstanceBlockStatusPacket(WrappedChatComponent status, Optional<BlockPosition> size) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(status, size)));
    }

    public WrappedClientboundTestInstanceBlockStatusPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedChatComponent getStatus() {
        return handle.getChatComponents().read(0);
    }

    public void setStatus(WrappedChatComponent status) {
        handle.getChatComponents().write(0, status);
    }

    /**
     * Returns the optional test block size. Internally stored as {@code Optional<Vec3i>};
     * converted via {@link BlockPosition#getConverter()}.
     */
    public Optional<BlockPosition> getSize() {
        Optional<BlockPosition> size = handle.getOptionals(BlockPosition.getConverter()).read(0);
        return size != null ? size : Optional.empty();
    }

    /** Sets the optional test block size. */
    public void setSize(Optional<BlockPosition> size) {
        handle.getOptionals(BlockPosition.getConverter()).write(0, size);
    }
}
