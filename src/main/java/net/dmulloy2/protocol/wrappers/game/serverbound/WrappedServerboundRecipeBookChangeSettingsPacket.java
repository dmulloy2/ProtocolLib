package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundRecipeBookChangeSettingsPacket} (game phase, serverbound).
 */
public class WrappedServerboundRecipeBookChangeSettingsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.RECIPE_SETTINGS;

    public WrappedServerboundRecipeBookChangeSettingsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundRecipeBookChangeSettingsPacket(boolean open, boolean filtering) {
        this();
        setOpen(open);
        setFiltering(filtering);
    }

    public WrappedServerboundRecipeBookChangeSettingsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public boolean isOpen() {
        return handle.getBooleans().read(0);
    }

    public void setOpen(boolean open) {
        handle.getBooleans().write(0, open);
    }

    public boolean isFiltering() {
        return handle.getBooleans().read(1);
    }

    public void setFiltering(boolean filtering) {
        handle.getBooleans().write(1, filtering);
    }
}
