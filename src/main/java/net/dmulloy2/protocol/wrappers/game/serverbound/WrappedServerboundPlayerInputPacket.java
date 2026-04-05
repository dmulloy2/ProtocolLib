package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AutoWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundPlayerInputPacket} (game phase, serverbound).
 *
 * <p>NMS: {@code ServerboundPlayerInputPacket(Input input)}
 * where {@code Input} is {@code record Input(boolean forward, boolean backward, boolean left,
 * boolean right, boolean jump, boolean shift, boolean sprint)}.
 */
public class WrappedServerboundPlayerInputPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.STEER_VEHICLE;

    private static final AutoWrapper<WrappedInput> INPUT_WRAPPER =
            AutoWrapper.wrap(WrappedInput.class, "world.entity.player.Input");

    /**
     * Mirror of {@code record Input(boolean forward, boolean backward, boolean left,
     * boolean right, boolean jump, boolean shift, boolean sprint)}.
     * Field order matches NMS component declaration order.
     */
    public static final class WrappedInput {
        public boolean forward;
        public boolean backward;
        public boolean left;
        public boolean right;
        public boolean jump;
        public boolean shift;
        public boolean sprint;
        public WrappedInput() {}
    }

    public WrappedServerboundPlayerInputPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundPlayerInputPacket(WrappedInput input) {
        this();
        setInput(input);
    }

    public WrappedServerboundPlayerInputPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the player's current movement input state. */
    public WrappedInput getInput() {
        return INPUT_WRAPPER.getSpecific(handle.getModifier().read(0));
    }

    /** Sets the player's movement input state. */
    public void setInput(WrappedInput input) {
        handle.getModifier().write(0, INPUT_WRAPPER.getGeneric(input));
    }
}
