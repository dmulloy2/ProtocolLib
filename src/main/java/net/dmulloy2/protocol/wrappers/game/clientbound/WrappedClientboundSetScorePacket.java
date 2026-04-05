package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedNumberFormat;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetScorePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String owner} – entity name or UUID string whose score is being set</li>
 *   <li>{@code String objectiveName} – name of the scoreboard objective</li>
 *   <li>{@code int score} – the score value</li>
 *   <li>{@code Optional<Component> display} – optional custom display name for the entry</li>
 *   <li>{@code Optional<NumberFormat> numberFormat} – optional custom number format</li>
 * </ul>
 */
public class WrappedClientboundSetScorePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SCOREBOARD_SCORE;

    public WrappedClientboundSetScorePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetScorePacket(String owner, String objectiveName, int score, Optional<WrappedChatComponent> display, Optional<WrappedNumberFormat> numberFormat) {
        this();
        setOwner(owner);
        setObjectiveName(objectiveName);
        setScore(score);
        setDisplay(display);
        setNumberFormat(numberFormat);
    }

    public WrappedClientboundSetScorePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getOwner() {
        return handle.getStrings().read(0);
    }

    public void setOwner(String owner) {
        handle.getStrings().write(0, owner);
    }

    public String getObjectiveName() {
        return handle.getStrings().read(1);
    }

    public void setObjectiveName(String objectiveName) {
        handle.getStrings().write(1, objectiveName);
    }

    public int getScore() {
        return handle.getIntegers().read(0);
    }

    public void setScore(int score) {
        handle.getIntegers().write(0, score);
    }

    public Optional<WrappedChatComponent> getDisplay() {
        return handle.getOptionals(BukkitConverters.getWrappedChatComponentConverter()).read(0);
    }

    public void setDisplay(Optional<WrappedChatComponent> display) {
        handle.getOptionals(BukkitConverters.getWrappedChatComponentConverter()).write(0, display);
    }

    public Optional<WrappedNumberFormat> getNumberFormat() {
        return handle.getOptionals(BukkitConverters.getWrappedNumberFormatConverter()).read(0);
    }

    public void setNumberFormat(Optional<WrappedNumberFormat> numberFormat) {
        handle.getOptionals(BukkitConverters.getWrappedNumberFormatConverter()).write(0, numberFormat);
    }
}
