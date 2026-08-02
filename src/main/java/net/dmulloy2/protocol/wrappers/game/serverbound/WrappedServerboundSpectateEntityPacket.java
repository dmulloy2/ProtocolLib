package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftVersion;
import java.util.OptionalInt;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundSpectatorActionPacket} (Play phase, serverbound).
 *
 * <p>Sent by a spectating client to attach the camera to an entity or stop spectating.
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code OptionalInt spectateEntityId} – entity to spectate, or empty to stop spectating</li>
 * </ul>
 */
public class WrappedServerboundSpectateEntityPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.SPECTATE_ENTITY;
    private static final boolean USES_OPTIONAL_ENTITY_ID =
            MinecraftVersion.v26_2.atOrAbove();
    private static final EquivalentConstructor CONSTRUCTOR = createConstructor();

    public WrappedServerboundSpectateEntityPacket() {
        this(USES_OPTIONAL_ENTITY_ID
                ? PacketContainer.fromPacket(CONSTRUCTOR.create(OptionalInt.empty()))
                : new PacketContainer(TYPE));
    }

    public WrappedServerboundSpectateEntityPacket(int entityId) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(
                USES_OPTIONAL_ENTITY_ID ? OptionalInt.of(entityId) : entityId)));
    }

    public WrappedServerboundSpectateEntityPacket(OptionalInt entityId) {
        this(createPacket(entityId));
    }

    public WrappedServerboundSpectateEntityPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityId() {
        return getOptionalEntityId().orElseThrow(
                () -> new IllegalStateException("The packet does not contain a spectated entity"));
    }

    public void setEntityId(int entityId) {
        setOptionalEntityId(OptionalInt.of(entityId));
    }

    public OptionalInt getOptionalEntityId() {
        return USES_OPTIONAL_ENTITY_ID
                ? handle.getSpecificModifier(OptionalInt.class).read(0)
                : OptionalInt.of(handle.getIntegers().read(0));
    }

    public void setOptionalEntityId(OptionalInt entityId) {
        if (USES_OPTIONAL_ENTITY_ID) {
            handle.getSpecificModifier(OptionalInt.class).write(0, entityId);
        } else {
            handle.getIntegers().write(0, entityId.orElseThrow(
                    () -> new UnsupportedOperationException(
                            "Stopping entity spectating is unavailable on this Minecraft version")));
        }
    }

    private static EquivalentConstructor createConstructor() {
        return new EquivalentConstructor(TYPE).withParam(
                USES_OPTIONAL_ENTITY_ID ? OptionalInt.class : int.class);
    }

    private static PacketContainer createPacket(OptionalInt entityId) {
        if (!USES_OPTIONAL_ENTITY_ID && entityId.isEmpty()) {
            throw new UnsupportedOperationException(
                    "Stopping entity spectating is unavailable on this Minecraft version");
        }
        return PacketContainer.fromPacket(CONSTRUCTOR.create(
                USES_OPTIONAL_ENTITY_ID ? entityId : entityId.getAsInt()));
    }
}
