package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedTeamParameters;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetPlayerTeamPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String name} – team name (max 16 chars)</li>
 *   <li>{@code int method} – 0=create, 1=remove, 2=update, 3=addPlayers, 4=removePlayers</li>
 *   <li>{@code Optional<Parameters> parameters} – team display parameters; present for methods 0, 1, 2</li>
 *   <li>{@code Collection<String> players} – player names to add/remove (methods 3 and 4)</li>
 * </ul>
 */
public class WrappedClientboundSetPlayerTeamPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SCOREBOARD_TEAM;

    public WrappedClientboundSetPlayerTeamPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetPlayerTeamPacket(String name, int method, Optional<WrappedTeamParameters> parameters, List<String> players) {
        this();
        setName(name);
        setMethod(method);
        setParameters(parameters);
        setPlayers(players);
    }

    public WrappedClientboundSetPlayerTeamPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getName() {
        return handle.getStrings().read(0);
    }

    public void setName(String name) {
        handle.getStrings().write(0, name);
    }

    public int getMethod() {
        return handle.getIntegers().read(0);
    }

    public void setMethod(int method) {
        handle.getIntegers().write(0, method);
    }

    /** Returns team display parameters (present for create/remove/update methods). */
    public Optional<WrappedTeamParameters> getParameters() {
        return handle.getOptionals(BukkitConverters.getWrappedTeamParametersConverter()).read(0);
    }

    public void setParameters(Optional<WrappedTeamParameters> parameters) {
        handle.getOptionals(BukkitConverters.getWrappedTeamParametersConverter()).write(0, parameters);
    }

    public List<String> getPlayers() {
        return (List<String>) handle.getModifier()
                .withType(Collection.class, BukkitConverters.getListConverter(Converters.passthrough(String.class)))
                .read(0);
    }

    public void setPlayers(List<String> players) {
        handle.getModifier()
                .withType(Collection.class, BukkitConverters.getListConverter(Converters.passthrough(String.class)))
                .write(0, players);
    }
}
