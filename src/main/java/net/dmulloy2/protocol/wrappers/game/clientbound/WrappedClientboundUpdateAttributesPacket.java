package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedAttribute;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateAttributesPacket} (game phase, clientbound).
 */
public class WrappedClientboundUpdateAttributesPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.UPDATE_ATTRIBUTES;

    public WrappedClientboundUpdateAttributesPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateAttributesPacket(int entityId, List<WrappedAttribute> attributes) {
        this();
        setEntityId(entityId);
        setAttributes(attributes);
    }

    public WrappedClientboundUpdateAttributesPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public List<WrappedAttribute> getAttributes() {
        return handle.getAttributeCollectionModifier().read(0);
    }

    public void setAttributes(List<WrappedAttribute> attributes) {
        handle.getAttributeCollectionModifier().write(0, attributes);
    }
}
