package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundRenameItemPacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code String name} – the new name entered in the anvil's rename field</li>
 * </ul>
 */
public class WrappedServerboundRenameItemPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ITEM_NAME;

    public WrappedServerboundRenameItemPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundRenameItemPacket(String name) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { String.class }).createPacket(name));
    }

    public WrappedServerboundRenameItemPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the new name entered in the anvil's rename field.
     */
    public String getName() {
        return handle.getStrings().read(0);
    }

    /**
     * Sets the new name entered in the anvil's rename field.
     */
    public void setName(String name) {
        handle.getStrings().write(0, name);
    }
}
