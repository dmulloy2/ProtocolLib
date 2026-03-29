package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSelectTradePacket} (Play phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int item} – zero-based index of the selected trade offer</li>
 * </ul>
 */
public class WrappedServerboundSelectTradePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.TR_SEL;

    public WrappedServerboundSelectTradePacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundSelectTradePacket(int item) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { int.class }).createPacket(item));
    }

    public WrappedServerboundSelectTradePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the zero-based index of the selected trade offer.
     */
    public int getItem() {
        return handle.getIntegers().read(0);
    }

    /**
     * Sets the zero-based index of the selected trade offer.
     */
    public void setItem(int item) {
        handle.getIntegers().write(0, item);
    }
}
