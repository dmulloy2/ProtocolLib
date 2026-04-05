package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundLockDifficultyPacket} (game phase, serverbound).
 */
public class WrappedServerboundLockDifficultyPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.DIFFICULTY_LOCK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(boolean.class);

    public WrappedServerboundLockDifficultyPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundLockDifficultyPacket(boolean locked) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(locked)));
    }

    public WrappedServerboundLockDifficultyPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isLocked() {
        return handle.getBooleans().read(0);
    }

    public void setLocked(boolean locked) {
        handle.getBooleans().write(0, locked);
    }
}
