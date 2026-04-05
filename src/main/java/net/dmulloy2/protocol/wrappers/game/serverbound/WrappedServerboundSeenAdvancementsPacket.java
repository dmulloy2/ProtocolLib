package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;

/**
 * Wrapper for {@code ServerboundSeenAdvancementsPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Action action} – OPEN_TAB or CLOSE_SCREEN</li>
 *   <li>{@code Optional<ResourceLocation> tab} – the advancement tab to open (only when action is OPEN_TAB)</li>
 * </ul>
 */
public class WrappedServerboundSeenAdvancementsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ADVANCEMENTS;

    /**
     * The client's action relating to the advancement screen.
     * Matches {@code ServerboundSeenAdvancementsPacket.Action}.
     */
    public enum Action {
        OPENED_TAB, CLOSED_SCREEN
    }

    public WrappedServerboundSeenAdvancementsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSeenAdvancementsPacket(Action action, MinecraftKey tab) {
        this();
        setAction(action);
        setTab(tab);
    }

    public WrappedServerboundSeenAdvancementsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Action getAction() {
        return handle.getEnumModifier(Action.class, 0).read(0);
    }

    public void setAction(Action action) {
        handle.getEnumModifier(Action.class, 0).write(0, action);
    }

    public MinecraftKey getTab() {
        return handle.getMinecraftKeys().readSafely(0);
    }

    public void setTab(MinecraftKey tab) {
        handle.getMinecraftKeys().writeSafely(0, tab);
    }
}
