package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLoginFinishedPacket} (login phase, clientbound).
 *
 * <p>Sent by the server to confirm successful login and provide the player's game profile.
 * Minecraft 26.2 and later also include a session identifier.
 */
public class WrappedClientboundLoginFinishedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Server.SUCCESS;
    private static final boolean HAS_SESSION_ID =
            MinecraftVersion.v26_2.atOrAbove();
    private static final EquivalentConstructor CONSTRUCTOR = createConstructor();

    public WrappedClientboundLoginFinishedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    /**
     * Creates a login-finished packet with a generated session identifier.
     */
    public WrappedClientboundLoginFinishedPacket(WrappedGameProfile gameProfile) {
        this(gameProfile, UUID.randomUUID());
    }

    public WrappedClientboundLoginFinishedPacket(WrappedGameProfile gameProfile, UUID sessionId) {
        this(PacketContainer.fromPacket(HAS_SESSION_ID
                ? CONSTRUCTOR.create(gameProfile, sessionId)
                : CONSTRUCTOR.create(gameProfile)));
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

    public UUID getSessionId() {
        return handle.getUUIDs().readSafely(0);
    }

    public void setSessionId(UUID sessionId) {
        handle.getUUIDs().writeSafely(0, sessionId);
    }

    private static EquivalentConstructor createConstructor() {
        EquivalentConstructor constructor = new EquivalentConstructor(TYPE)
                .withParam(
                        MinecraftReflection.getGameProfileClass(),
                        BukkitConverters.getWrappedGameProfileConverter());
        return HAS_SESSION_ID ? constructor.withParam(UUID.class) : constructor;
    }
}
