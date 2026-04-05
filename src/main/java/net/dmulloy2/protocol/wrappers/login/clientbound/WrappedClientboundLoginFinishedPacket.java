package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLoginFinishedPacket} (login phase, clientbound).
 *
 * <p>Sent by the server to confirm successful login and provide the player's game profile.
 */
public class WrappedClientboundLoginFinishedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.SUCCESS;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getGameProfileClass(), BukkitConverters.getWrappedGameProfileConverter());

    public WrappedClientboundLoginFinishedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLoginFinishedPacket(WrappedGameProfile gameProfile) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(gameProfile)));
    }

    public WrappedClientboundLoginFinishedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedGameProfile getGameProfile() {
        return handle.getGameProfiles().read(0);
    }

    public void setGameProfile(WrappedGameProfile gameProfile) {
        handle.getGameProfiles().write(0, gameProfile);
    }
}
