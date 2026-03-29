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

    // Common event IDs for convenience
    public static final int NO_RESPAWN_BLOCK = 0;
    public static final int BEGIN_RAINING    = 1;
    public static final int END_RAINING      = 2;
    public static final int CHANGE_GAME_MODE = 3;
    public static final int WIN_GAME         = 4;
    public static final int DEMO_EVENT       = 5;
    public static final int ARROW_HIT_PLAYER = 6;
    public static final int RAIN_LEVEL       = 7;
    public static final int THUNDER_LEVEL    = 8;
    public static final int PUFFER_FISH_STING = 9;
    public static final int GUARDIAN_ELDER_EFFECT = 10;
    public static final int RESPAWN_SCREEN   = 11;

    public WrappedClientboundGameEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
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
