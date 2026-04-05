package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Wrapper for {@code ClientboundSetCursorItemPacket} (game phase, clientbound).
 */
public class WrappedClientboundSetCursorItemPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_CURSOR_ITEM;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(MinecraftReflection.getItemStackClass(), BukkitConverters.getItemStackConverter());

    public WrappedClientboundSetCursorItemPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetCursorItemPacket(ItemStack contents) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(contents)));
    }

    public WrappedClientboundSetCursorItemPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public ItemStack getContents() {
        return handle.getItemModifier().read(0);
    }

    public void setContents(ItemStack contents) {
        handle.getItemModifier().write(0, contents);
    }
}
