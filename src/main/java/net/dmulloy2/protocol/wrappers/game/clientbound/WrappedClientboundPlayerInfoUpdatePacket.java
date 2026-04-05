package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import java.util.List;
import java.util.Set;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundPlayerInfoUpdatePacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code EnumSet<Action> actions} – set of actions to apply to each player entry</li>
 *   <li>{@code List<Entry> entries} – player info entries being updated</li>
 * </ul>
 */
public class WrappedClientboundPlayerInfoUpdatePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.PLAYER_INFO;

    public WrappedClientboundPlayerInfoUpdatePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundPlayerInfoUpdatePacket(Set<EnumWrappers.PlayerInfoAction> actions, List<PlayerInfoData> entries) {
        this();
        setActions(actions);
        setEntries(entries);
    }

    public WrappedClientboundPlayerInfoUpdatePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Set<EnumWrappers.PlayerInfoAction> getActions() {
        return handle.getPlayerInfoActions().read(0);
    }

    public void setActions(Set<EnumWrappers.PlayerInfoAction> actions) {
        handle.getPlayerInfoActions().write(0, actions);
    }

    public List<PlayerInfoData> getEntries() {
        return handle.getPlayerInfoDataLists().read(1);
    }

    public void setEntries(List<PlayerInfoData> entries) {
        handle.getPlayerInfoDataLists().write(1, entries);
    }
}
