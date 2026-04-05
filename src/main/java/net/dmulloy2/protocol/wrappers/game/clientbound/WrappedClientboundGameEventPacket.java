package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundGameEventPacket} (Play phase, clientbound).
 *
 * <p>Notifies the client of a game-state change such as starting rain, changing
 * game mode, or ending the game.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int event} – the game-event/reason ID (see Minecraft protocol wiki)</li>
 *   <li>{@code float value} – numeric parameter for the event</li>
 * </ul>
 */
public class WrappedClientboundGameEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.GAME_STATE_CHANGE;

    public WrappedClientboundGameEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundGameEventPacket(int event, float value) {
        this();
        setEvent(event);
        setValue(value);
    }

    public WrappedClientboundGameEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the game-event ID (reason code). */
    public int getEvent() {
        return handle.getGameStateIDs().read(0);
    }

    public void setEvent(int event) {
        handle.getGameStateIDs().write(0, event);
    }

    /** Returns the float parameter associated with the event. */
    public float getValue() {
        return handle.getFloat().read(0);
    }

    public void setValue(float value) {
        handle.getFloat().write(0, value);
    }
}
