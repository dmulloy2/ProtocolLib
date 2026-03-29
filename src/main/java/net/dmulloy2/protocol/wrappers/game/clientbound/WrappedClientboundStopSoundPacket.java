package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundStopSoundPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code @Nullable Identifier name} – the specific sound to stop, or {@code null} to stop all sounds in the category</li>
 *   <li>{@code @Nullable SoundSource source} – the category to stop, or {@code null} to stop all categories</li>
 * </ul>
 *
 * <p>Both fields are nullable. Passing {@code null} for {@code source} stops all sounds regardless of
 * category; passing {@code null} for {@code name} stops all sounds in the given category.
 */
public class WrappedClientboundStopSoundPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.STOP_SOUND;

    public WrappedClientboundStopSoundPacket() {
        super(new PacketContainer(TYPE), TYPE);
            }

    public WrappedClientboundStopSoundPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the sound identifier to stop, or {@code null} if all sounds should stop. */
    public MinecraftKey getName() {
        return handle.getMinecraftKeys().read(0);
    }

    public void setName(MinecraftKey name) {
        handle.getMinecraftKeys().write(0, name);
    }

    /** Returns the sound category to stop, or {@code null} if all categories should stop. */
    public EnumWrappers.SoundCategory getSource() {
        return handle.getSoundCategories().read(0);
    }

    public void setSource(EnumWrappers.SoundCategory source) {
        handle.getSoundCategories().write(0, source);
    }
}
