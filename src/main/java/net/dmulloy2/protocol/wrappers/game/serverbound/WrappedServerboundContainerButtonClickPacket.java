package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundContainerButtonClickPacket} (Play phase, serverbound).
 *
 * <p>Sent when the player clicks a button inside a container UI (e.g. an enchanting table slot).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int containerId} – ID of the open container</li>
 *   <li>{@code int buttonId} – index of the button that was clicked</li>
 * </ul>
 */
public class WrappedServerboundContainerButtonClickPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.ENCHANT_ITEM;

    public WrappedServerboundContainerButtonClickPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedServerboundContainerButtonClickPacket(int containerId, int buttonId) {
        this(PacketConstructor.DEFAULT.withPacket(TYPE, new Class<?>[] { int.class, int.class }).createPacket(containerId, buttonId));
    }

    public WrappedServerboundContainerButtonClickPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getContainerId() {
        return handle.getIntegers().read(0);
    }

    public void setContainerId(int containerId) {
        handle.getIntegers().write(0, containerId);
    }

    public int getButtonId() {
        return handle.getIntegers().read(1);
    }

    public void setButtonId(int buttonId) {
        handle.getIntegers().write(1, buttonId);
    }
}
