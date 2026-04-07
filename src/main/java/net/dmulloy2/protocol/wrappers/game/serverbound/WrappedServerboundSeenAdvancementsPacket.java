package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Arrays;
import net.dmulloy2.protocol.AbstractPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;

/**
 * Wrapper for {@code ServerboundSeenAdvancementsPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Action action} – OPEN_TAB or CLOSE_SCREEN</li>
 *   <li>{@code @Nullable ResourceLocation tab} – the advancement tab to open (only when action is OPEN_TAB, otherwise {@code null})</li>
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

    private static final Class<?> ACTION_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredClasses())
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(ACTION_NMS_CLASS, new EnumWrappers.EnumConverter<>(ACTION_NMS_CLASS, Action.class))
            .withParam(MinecraftReflection.getMinecraftKeyClass(), MinecraftKey.getConverter());

    public WrappedServerboundSeenAdvancementsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSeenAdvancementsPacket(Action action, MinecraftKey tab) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(action, tab)));
    }

    public WrappedServerboundSeenAdvancementsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Action getAction() {
        return handle.getEnumModifier(Action.class, 0).readSafely(0);
    }

    public void setAction(Action action) {
        handle.getEnumModifier(Action.class, 0).writeSafely(0, action);
    }

    public MinecraftKey getTab() {
        return handle.getMinecraftKeys().readSafely(0);
    }

    public void setTab(MinecraftKey tab) {
        handle.getMinecraftKeys().writeSafely(0, tab);
    }
}
