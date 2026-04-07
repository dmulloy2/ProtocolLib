package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.Converters;
import java.util.List;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundEditBookPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int slot} – hotbar slot of the book being edited</li>
 *   <li>{@code List<String> pages} – list of page texts</li>
 *   <li>{@code Optional<String> title} – title when signing the book, or empty when saving</li>
 * </ul>
 */
public class WrappedServerboundEditBookPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.B_EDIT;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(List.class)
            .withParam(Optional.class);

    public WrappedServerboundEditBookPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundEditBookPacket(int slot, List<String> pages, Optional<String> title) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(slot, pages, title)));
    }

    public WrappedServerboundEditBookPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getSlot() {
        return handle.getIntegers().readSafely(0);
    }

    public void setSlot(int slot) {
        handle.getIntegers().writeSafely(0, slot);
    }

    public List<String> getPages() {
        return handle.getLists(Converters.passthrough(String.class)).readSafely(0);
    }

    public void setPages(List<String> pages) {
        handle.getLists(Converters.passthrough(String.class)).writeSafely(0, pages);
    }

    public Optional<String> getTitle() {
        return handle.getOptionals(Converters.passthrough(String.class)).readSafely(0);
    }

    public void setTitle(Optional<String> title) {
        handle.getOptionals(Converters.passthrough(String.class)).writeSafely(0, title);
    }
}
