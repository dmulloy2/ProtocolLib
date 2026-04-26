package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundOpenScreenPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int containerId} – ID assigned to this container session</li>
 *   <li>{@code MenuType<?> type} – inventory menu type, addressed by its registry key
 *       (e.g. {@code minecraft:generic_9x3})</li>
 *   <li>{@code Component title} – display title shown in the inventory screen</li>
 * </ul>
 */
public class WrappedClientboundOpenScreenPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.OPEN_WINDOW;

    /** NMS {@code MenuType} class. */
    private static final Class<?> MENU_TYPE_CLASS =
            MinecraftReflection.getMinecraftClass("world.inventory.MenuType");

    /** Converter between NMS {@code MenuType<?>} values and {@link MinecraftKey} identifiers. */
    private static final EquivalentConverter<MinecraftKey> MENU_TYPE_CONVERTER =
            WrappedRegistry.getRegistry(MENU_TYPE_CLASS).valueConverter();

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MENU_TYPE_CLASS, MENU_TYPE_CONVERTER)
            .withParam(MinecraftReflection.getIChatBaseComponentClass(),
                    BukkitConverters.getWrappedChatComponentConverter());

    public WrappedClientboundOpenScreenPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundOpenScreenPacket(int containerId, MinecraftKey menuType, WrappedChatComponent title) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(containerId, menuType, title)));
    }

    public WrappedClientboundOpenScreenPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    /**
     * Returns the {@link MinecraftKey} of this screen's {@code MenuType}
     * (e.g. {@code minecraft:generic_9x3}), or {@code null} if the field has not been set.
     */
    public MinecraftKey getMenuType() {
        return handle.getModifier().withType(MENU_TYPE_CLASS, MENU_TYPE_CONVERTER).read(0);
    }

    /**
     * Sets this screen's {@code MenuType} from its registry {@link MinecraftKey}.
     */
    public void setMenuType(MinecraftKey menuType) {
        handle.getModifier().withType(MENU_TYPE_CLASS, MENU_TYPE_CONVERTER).write(0, menuType);
    }

    public WrappedChatComponent getTitle() {
        return handle.getChatComponents().read(0);
    }

    public void setTitle(WrappedChatComponent title) {
        handle.getChatComponents().write(0, title);
    }
}
