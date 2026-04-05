package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundResetScorePacket} (game phase, clientbound).
 */
public class WrappedClientboundResetScorePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RESET_SCORE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(String.class);

    public WrappedClientboundResetScorePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundResetScorePacket(String owner, String objectiveName) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(owner, objectiveName)));
    }

    public WrappedClientboundResetScorePacket(PacketContainer packet) {
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
}
