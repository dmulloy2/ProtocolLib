package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.UUID;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundResourcePackPacket} (configuration phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code UUID id} – unique identifier of the resource pack</li>
 *   <li>{@code ResourcePackStatusAction action} – the client's response status</li>
 * </ul>
 */
public class WrappedServerboundResourcePackPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.RESOURCE_PACK_ACK;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(UUID.class)
            .withParam(EnumWrappers.getResourcePackStatusClass(), EnumWrappers.getResourcePackStatusConverter());

    public WrappedServerboundResourcePackPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundResourcePackPacket(UUID id, EnumWrappers.ResourcePackStatus action) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(id, action)));
    }

    public WrappedServerboundResourcePackPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public UUID getId() {
        return handle.getUUIDs().read(0);
    }

    public void setId(UUID id) {
        handle.getUUIDs().write(0, id);
    }

    public EnumWrappers.ResourcePackStatus getAction() {
        return handle.getResourcePackStatus().read(0);
    }

    public void setAction(EnumWrappers.ResourcePackStatus action) {
        handle.getResourcePackStatus().write(0, action);
    }
}
