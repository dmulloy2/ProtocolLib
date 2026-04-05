package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.util.Vector;

/**
 * Wrapper for {@code ClientboundDamageEventPacket} (Play phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code int entityId} – entity that received the damage</li>
 *   <li>{@code Holder<DamageType> sourceType} – damage type (not exposed; use the raw modifier)</li>
 *   <li>{@code int sourceCauseId} – entity ID of the damage cause, or {@code 0} if absent</li>
 *   <li>{@code int sourceDirectId} – entity ID of the direct source, or {@code 0} if absent</li>
 *   <li>{@code Optional<Vector> sourcePosition} – world position of the damage source, if any</li>
 * </ul>
 */
public class WrappedClientboundDamageEventPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.DAMAGE_EVENT;

    public WrappedClientboundDamageEventPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundDamageEventPacket(int entityId, int sourceCauseId, int sourceDirectId, Optional<Vector> sourcePosition) {
        this();
        setEntityId(entityId);
        setSourceCauseId(sourceCauseId);
        setSourceDirectId(sourceDirectId);
        setSourcePosition(sourcePosition);
    }

    public WrappedClientboundDamageEventPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return handle.getIntegers().read(0);
    }

    public void setEntityId(int entityId) {
        handle.getIntegers().write(0, entityId);
    }

    public int getSourceCauseId() {
        return handle.getIntegers().read(1);
    }

    public void setSourceCauseId(int sourceCauseId) {
        handle.getIntegers().write(1, sourceCauseId);
    }

    public int getSourceDirectId() {
        return handle.getIntegers().read(2);
    }

    public void setSourceDirectId(int sourceDirectId) {
        handle.getIntegers().write(2, sourceDirectId);
    }

    public Optional<Vector> getSourcePosition() {
        return handle.getOptionals(BukkitConverters.getVectorConverter()).read(0);
    }

    public void setSourcePosition(Optional<Vector> sourcePosition) {
        handle.getOptionals(BukkitConverters.getVectorConverter()).write(0, sourcePosition);
    }
}
