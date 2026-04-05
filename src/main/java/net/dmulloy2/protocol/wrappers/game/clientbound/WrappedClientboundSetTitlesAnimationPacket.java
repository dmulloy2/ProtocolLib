package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetTitlesAnimationPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int fadeIn} – fade-in duration in ticks</li>
 *   <li>{@code int stay} – stay duration in ticks</li>
 *   <li>{@code int fadeOut} – fade-out duration in ticks</li>
 * </ul>
 */
public class WrappedClientboundSetTitlesAnimationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SET_TITLES_ANIMATION;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(int.class)
            .withParam(int.class);

    public WrappedClientboundSetTitlesAnimationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetTitlesAnimationPacket(int fadeIn, int stay, int fadeOut) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(fadeIn, stay, fadeOut)));
    }

    public WrappedClientboundSetTitlesAnimationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getFadeIn() {
        return handle.getIntegers().read(0);
    }

    public void setFadeIn(int fadeIn) {
        handle.getIntegers().write(0, fadeIn);
    }

    public int getStay() {
        return handle.getIntegers().read(1);
    }

    public void setStay(int stay) {
        handle.getIntegers().write(1, stay);
    }

    public int getFadeOut() {
        return handle.getIntegers().read(2);
    }

    public void setFadeOut(int fadeOut) {
        handle.getIntegers().write(2, fadeOut);
    }
}
