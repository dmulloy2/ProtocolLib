package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.WrappedRegistry;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.damage.DamageType;
import org.bukkit.util.Vector;

import java.util.Optional;

/**
 * Wrapper for {@code ClientboundDamageEventPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity that received the damage</li>
 *   <li>{@code Holder<DamageType> sourceType} – damage type</li>
 *   <li>{@code int sourceCauseId} – entity ID of the damage cause, or {@code -1} if absent</li>
 *   <li>{@code int sourceDirectId} – entity ID of the direct source, or {@code -1} if absent</li>
 *   <li>{@code Optional<Vector> sourcePosition} – world position of the damage source, if any</li>
 * </ul>
 */
public class WrappedClientboundDamageEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DAMAGE_EVENT;

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(int.class)
            .withParam(MinecraftReflection.getHolderClass(),
                    Converters.holder(BukkitConverters.getDamageTypeConverter(),
                            WrappedRegistry.getRegistry(MinecraftReflection.getDamageTypeClass())))
            .withParam(int.class)
            .withParam(int.class)
            .withParam(Optional.class, Converters.optional(BukkitConverters.getVectorConverter()));

    public WrappedClientboundDamageEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDamageEventPacket(int entityId, DamageType sourceType, int sourceCauseId, int sourceDirectId, Optional<Vector> sourcePosition) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(entityId, sourceType, sourceCauseId, sourceDirectId, sourcePosition)));
    }

    public WrappedClientboundDamageEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().readSafely(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().writeSafely(0, entityId);
    }

    /** Returns the damage type, unwrapped from its registry {@code Holder}. */
    public DamageType getSourceType() {
        return handle.getDamageTypes().readSafely(0);
    }

    public void setSourceType(DamageType sourceType) {
        handle.getDamageTypes().writeSafely(0, sourceType);
    }

    public int getSourceCauseId() {
        return handle.getIntegers().readSafely(1);
    }

    public void setSourceCauseId(int sourceCauseId) {
        handle.getIntegers().writeSafely(1, sourceCauseId);
    }

    public int getSourceDirectId() {
        return handle.getIntegers().readSafely(2);
    }

    public void setSourceDirectId(int sourceDirectId) {
        handle.getIntegers().writeSafely(2, sourceDirectId);
    }

    public Optional<Vector> getSourcePosition() {
        return handle.getOptionals(BukkitConverters.getVectorConverter()).readSafely(0);
    }

    public void setSourcePosition(Optional<Vector> sourcePosition) {
        handle.getOptionals(BukkitConverters.getVectorConverter()).writeSafely(0, sourcePosition);
    }
}

