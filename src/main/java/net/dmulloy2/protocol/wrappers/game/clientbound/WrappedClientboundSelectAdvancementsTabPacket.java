package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSelectAdvancementsTabPacket} (game phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code @Nullable Identifier tab} – key of the advancements tab to select,
 *       or {@code null} to close the advancements screen</li>
 * </ul>
 */
public class WrappedClientboundSelectAdvancementsTabPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SELECT_ADVANCEMENT_TAB;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter());

    public WrappedClientboundSelectAdvancementsTabPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSelectAdvancementsTabPacket(MinecraftKey tab) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(tab)));
    }

    public WrappedClientboundSelectAdvancementsTabPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getTab() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setTab(MinecraftKey tab) {
        handle.getMinecraftKeys().write(0, tab);
    }
}
