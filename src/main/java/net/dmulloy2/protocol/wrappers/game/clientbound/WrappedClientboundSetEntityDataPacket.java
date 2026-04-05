package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import java.util.List;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetEntityDataPacket} (game phase, clientbound).
 */
public class WrappedClientboundSetEntityDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_METADATA;

    public WrappedClientboundSetEntityDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetEntityDataPacket(int id, List<WrappedDataValue> packedItems) {
        this();
        setId(id);
        setPackedItems(packedItems);
    }

    public WrappedClientboundSetEntityDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getId() {
        return handle.getIntegers().read(0);
    }

    public void setId(int id) {
        handle.getIntegers().write(0, id);
    }

    public List<WrappedDataValue> getPackedItems() {
        return handle.getDataValueCollectionModifier().read(0);
    }

    public void setPackedItems(List<WrappedDataValue> packedItems) {
        handle.getDataValueCollectionModifier().write(0, packedItems);
    }
}
