package net.dmulloy2.protocol.wrappers.login.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundHelloPacket} (login phase, serverbound).
 */
public class WrappedServerboundHelloPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Login.Client.START;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(String.class)
            .withParam(UUID.class);

    public WrappedServerboundHelloPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundHelloPacket(String name, UUID profileId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(name, profileId)));
    }

    public WrappedServerboundHelloPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getName() {
        return handle.getStrings().read(0);
    }

    public void setName(String name) {
        handle.getStrings().write(0, name);
    }

    public UUID getProfileId() {
        return handle.getUUIDs().read(0);
    }

    public void setProfileId(UUID profileId) {
        handle.getUUIDs().write(0, profileId);
    }
}
