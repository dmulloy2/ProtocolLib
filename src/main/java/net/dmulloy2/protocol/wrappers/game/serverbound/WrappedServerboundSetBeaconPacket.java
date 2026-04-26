package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.potion.PotionEffectType;

/**
 * Wrapper for {@code ServerboundSetBeaconPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Optional<Holder<MobEffect>> primary} – primary beacon effect</li>
 *   <li>{@code Optional<Holder<MobEffect>> secondary} – secondary beacon effect</li>
 * </ul>
 */
public class WrappedServerboundSetBeaconPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.BEACON;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(Optional.class, Converters.optional(effectConverter()))
            .withParam(Optional.class, Converters.optional(effectConverter()));

    public WrappedServerboundSetBeaconPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundSetBeaconPacket(Optional<PotionEffectType> primary, Optional<PotionEffectType> secondary) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(primary, secondary)));
    }

    public WrappedServerboundSetBeaconPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    private static com.comphenix.protocol.reflect.EquivalentConverter<PotionEffectType> effectConverter() {
        return Converters.ignoreNull(Converters.holder(
                BukkitConverters.getEffectTypeConverter(),
                WrappedRegistry.getRegistry(MinecraftReflection.getMobEffectListClass())));
    }

    public Optional<PotionEffectType> getPrimary() {
        return handle.getOptionals(effectConverter()).read(0);
    }

    public void setPrimary(Optional<PotionEffectType> primary) {
        handle.getOptionals(effectConverter()).write(0, primary);
    }

    public Optional<PotionEffectType> getSecondary() {
        return handle.getOptionals(effectConverter()).read(1);
    }

    public void setSecondary(Optional<PotionEffectType> secondary) {
        handle.getOptionals(effectConverter()).write(1, secondary);
    }
}
