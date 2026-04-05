package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BlockPosition;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundGameTestHighlightPosPacket} (game phase, clientbound).
 */
public class WrappedClientboundGameTestHighlightPosPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.GAME_TEST_HIGHLIGHT_POS;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter())
            .withParam(MinecraftReflection.getBlockPositionClass(), BlockPosition.getConverter());

    public WrappedClientboundGameTestHighlightPosPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundGameTestHighlightPosPacket(BlockPosition absolutePos, BlockPosition relativePos) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(absolutePos, relativePos)));
    }

    public WrappedClientboundGameTestHighlightPosPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getAbsolutePos() {
        return handle.getBlockPositionModifier().read(0);
    }

    public void setAbsolutePos(BlockPosition absolutePos) {
        handle.getBlockPositionModifier().write(0, absolutePos);
    }

    public BlockPosition getRelativePos() {
        return handle.getBlockPositionModifier().read(1);
    }

    public void setRelativePos(BlockPosition relativePos) {
        handle.getBlockPositionModifier().write(1, relativePos);
    }
}
