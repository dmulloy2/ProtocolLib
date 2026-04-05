package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundUpdateAdvancementsPacket} (game phase, clientbound).
 */
public class WrappedClientboundUpdateAdvancementsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.ADVANCEMENTS;

    public WrappedClientboundUpdateAdvancementsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundUpdateAdvancementsPacket(boolean shouldReset, boolean shouldShowAdvancements) {
        this();
        setShouldReset(shouldReset);
        setShouldShowAdvancements(shouldShowAdvancements);
    }

    public WrappedClientboundUpdateAdvancementsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isShouldReset() {
        return handle.getBooleans().read(0);
    }

    public void setShouldReset(boolean shouldReset) {
        handle.getBooleans().write(0, shouldReset);
    }

    public boolean isShouldShowAdvancements() {
        return handle.getBooleans().read(1);
    }

    public void setShouldShowAdvancements(boolean shouldShowAdvancements) {
        handle.getBooleans().write(1, shouldShowAdvancements);
    }

    // TODO: missing field 'added' (NMS type: List<AdvancementHolder>)
    //   No ProtocolLib accessor exists for AdvancementHolder. Use handle.getModifier().read(1) for the raw List.
    // TODO: missing field 'removed' (NMS type: Set<Identifier>)
    //   Use getMinecraftKeys() once a dedicated Set accessor is added, or handle.getModifier().read(2) for the raw Set.
    // TODO: missing field 'progress' (NMS type: Map<Identifier, AdvancementProgress>)
    //   No ProtocolLib accessor exists for AdvancementProgress. Use handle.getModifier().read(3) for the raw Map.
}
