package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.ChunkCoordIntPair;
import com.comphenix.protocol.wrappers.Converters;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedResourceKey;
import java.util.Objects;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.UUID;

/**
 * Wrapper for {@code ClientboundTrackedWaypointPacket} (game phase, clientbound).
 * Tracks a waypoint on the client. Fields have no ProtocolLib accessor.
 */
public class WrappedClientboundTrackedWaypointPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.TRACKED_WAYPOINT;

    private static final Class<?> OPERATION_CLASS =
            MinecraftReflection.getMinecraftClass("network.protocol.game.ClientboundTrackedWaypointPacket$Operation");
    private static final Class<?> TRACKED_WAYPOINT_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.TrackedWaypoint");
    private static final Class<?> EMPTY_WAYPOINT_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.TrackedWaypoint$EmptyWaypoint");
    private static final Class<?> VEC3I_WAYPOINT_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.TrackedWaypoint$Vec3iWaypoint");
    private static final Class<?> CHUNK_WAYPOINT_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.TrackedWaypoint$ChunkWaypoint");
    private static final Class<?> AZIMUTH_WAYPOINT_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.TrackedWaypoint$AzimuthWaypoint");
    private static final Class<?> WAYPOINT_ICON_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.Waypoint$Icon");
    private static final Class<?> WAYPOINT_STYLE_ASSETS_CLASS =
            MinecraftReflection.getMinecraftClass("world.waypoints.WaypointStyleAssets");
    private static final Class<?> VEC3I_CLASS =
            MinecraftReflection.getMinecraftClass("core.Vec3i");
    private static final Class<?> EITHER_CLASS =
            MinecraftReflection.getLibraryClass("com.mojang.datafixers.util.Either");

    private static final Object WAYPOINT_STYLE_REGISTRY_KEY = Objects.requireNonNull(
            Accessors.getFieldAccessorOrNull(
                    WAYPOINT_STYLE_ASSETS_CLASS,
                    "ROOT_ID",
                    MinecraftReflection.getResourceKey()))
            .get(null);
    private static final ConstructorAccessor WAYPOINT_ICON_CONSTRUCTOR =
            Accessors.getConstructorAccessor(WAYPOINT_ICON_CLASS, MinecraftReflection.getResourceKey(), Optional.class);
    private static final FieldAccessor WAYPOINT_ICON_STYLE =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(
                    WAYPOINT_ICON_CLASS,
                    "style",
                    MinecraftReflection.getResourceKey()));
    private static final FieldAccessor WAYPOINT_ICON_COLOR =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(WAYPOINT_ICON_CLASS, "color", Optional.class));

    private static final ConstructorAccessor EMPTY_WAYPOINT_CONSTRUCTOR =
            Accessors.getConstructorAccessor(EMPTY_WAYPOINT_CLASS, UUID.class);
    private static final ConstructorAccessor VEC3I_WAYPOINT_CONSTRUCTOR =
            Accessors.getConstructorAccessor(VEC3I_WAYPOINT_CLASS, UUID.class, WAYPOINT_ICON_CLASS, VEC3I_CLASS);
    private static final ConstructorAccessor CHUNK_WAYPOINT_CONSTRUCTOR =
            Accessors.getConstructorAccessor(CHUNK_WAYPOINT_CLASS, UUID.class, WAYPOINT_ICON_CLASS,
                    MinecraftReflection.getChunkCoordIntPair());
    private static final ConstructorAccessor AZIMUTH_WAYPOINT_CONSTRUCTOR =
            Accessors.getConstructorAccessor(AZIMUTH_WAYPOINT_CLASS, UUID.class, WAYPOINT_ICON_CLASS, float.class);
    private static final ConstructorAccessor VEC3I_CONSTRUCTOR =
            Accessors.getConstructorAccessor(VEC3I_CLASS, int.class, int.class, int.class);

    private static final MethodAccessor GET_TRACKED_WAYPOINT_ID =
            Accessors.getMethodAccessor(TRACKED_WAYPOINT_CLASS, "id");
    private static final MethodAccessor GET_TRACKED_WAYPOINT_ICON =
            Accessors.getMethodAccessor(TRACKED_WAYPOINT_CLASS, "icon");
    private static final MethodAccessor EITHER_LEFT =
            Accessors.getMethodAccessor(EITHER_CLASS, "left");
    private static final MethodAccessor GET_VEC3I_X =
            Accessors.getMethodAccessor(VEC3I_CLASS, "getX");
    private static final MethodAccessor GET_VEC3I_Y =
            Accessors.getMethodAccessor(VEC3I_CLASS, "getY");
    private static final MethodAccessor GET_VEC3I_Z =
            Accessors.getMethodAccessor(VEC3I_CLASS, "getZ");

    private static final FieldAccessor VEC3I_WAYPOINT_VECTOR =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(VEC3I_WAYPOINT_CLASS, "vector", VEC3I_CLASS));
    private static final FieldAccessor CHUNK_WAYPOINT_CHUNK =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(
                    CHUNK_WAYPOINT_CLASS,
                    "chunkPos",
                    MinecraftReflection.getChunkCoordIntPair()));
    private static final FieldAccessor AZIMUTH_WAYPOINT_ANGLE =
            Objects.requireNonNull(Accessors.getFieldAccessorOrNull(AZIMUTH_WAYPOINT_CLASS, "angle", float.class));

    private static final EquivalentConverter<Operation> OPERATION_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(Operation specific) {
                    return operation(specific);
                }

                @Override
                public Operation getSpecific(Object generic) {
                    return Operation.valueOf(((Enum<?>) generic).name());
                }

                @Override
                public Class<Operation> getSpecificType() {
                    return Operation.class;
                }
            });

    private static final EquivalentConverter<WaypointIcon> WAYPOINT_ICON_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(WaypointIcon specific) {
                    return WAYPOINT_ICON_CONSTRUCTOR.invoke(
                            WrappedResourceKey.of(WAYPOINT_STYLE_REGISTRY_KEY, specific.style).getHandle(),
                            specific.color);
                }

                @Override
                public WaypointIcon getSpecific(Object generic) {
                    return new WaypointIcon(
                            WrappedResourceKey.fromHandle(WAYPOINT_ICON_STYLE.get(generic)).getLocation(),
                            color(generic));
                }

                @Override
                public Class<WaypointIcon> getSpecificType() {
                    return WaypointIcon.class;
                }
            });

    private static final EquivalentConverter<TrackedWaypoint> TRACKED_WAYPOINT_CONVERTER =
            Converters.ignoreNull(new EquivalentConverter<>() {
                @Override
                public Object getGeneric(TrackedWaypoint specific) {
                    Object icon = WAYPOINT_ICON_CONVERTER.getGeneric(specific.icon);
                    return switch (specific.type) {
                        case EMPTY -> EMPTY_WAYPOINT_CONSTRUCTOR.invoke(specific.identifier);
                        case VEC3I -> VEC3I_WAYPOINT_CONSTRUCTOR.invoke(
                                specific.identifier,
                                icon,
                                vec3i(Objects.requireNonNull(specific.position, "position")));
                        case CHUNK -> CHUNK_WAYPOINT_CONSTRUCTOR.invoke(
                                specific.identifier,
                                icon,
                                ChunkCoordIntPair.getConverter()
                                        .getGeneric(Objects.requireNonNull(specific.chunk, "chunk")));
                        case AZIMUTH -> AZIMUTH_WAYPOINT_CONSTRUCTOR.invoke(
                                specific.identifier,
                                icon,
                                Objects.requireNonNull(specific.angle, "angle"));
                    };
                }

                @Override
                public TrackedWaypoint getSpecific(Object generic) {
                    UUID identifier = uuidIdentifier(generic);
                    WaypointIcon icon = WAYPOINT_ICON_CONVERTER.getSpecific(GET_TRACKED_WAYPOINT_ICON.invoke(generic));

                    if (EMPTY_WAYPOINT_CLASS.isInstance(generic)) {
                        return TrackedWaypoint.empty(identifier);
                    }
                    if (VEC3I_WAYPOINT_CLASS.isInstance(generic)) {
                        return TrackedWaypoint.position(identifier, icon,
                                position(VEC3I_WAYPOINT_VECTOR.get(generic)));
                    }
                    if (CHUNK_WAYPOINT_CLASS.isInstance(generic)) {
                        return TrackedWaypoint.chunk(identifier, icon,
                                ChunkCoordIntPair.getConverter().getSpecific(CHUNK_WAYPOINT_CHUNK.get(generic)));
                    }
                    if (AZIMUTH_WAYPOINT_CLASS.isInstance(generic)) {
                        return TrackedWaypoint.azimuth(identifier, icon, (float) AZIMUTH_WAYPOINT_ANGLE.get(generic));
                    }
                    throw new IllegalArgumentException("Unknown tracked waypoint type: " + generic.getClass().getName());
                }

                @Override
                public Class<TrackedWaypoint> getSpecificType() {
                    return TrackedWaypoint.class;
                }
            });

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(OPERATION_CLASS, OPERATION_CONVERTER)
            .withParam(TRACKED_WAYPOINT_CLASS, TRACKED_WAYPOINT_CONVERTER);

    public enum Operation {
        TRACK, UNTRACK, UPDATE
    }

    public WrappedClientboundTrackedWaypointPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundTrackedWaypointPacket(Operation operation, TrackedWaypoint waypoint) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(operation, waypoint)));
    }

    public WrappedClientboundTrackedWaypointPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Operation getOperation() {
        return handle.getModifier().withType(OPERATION_CLASS, OPERATION_CONVERTER).read(0);
    }

    public void setOperation(Operation operation) {
        handle.getModifier().withType(OPERATION_CLASS, OPERATION_CONVERTER).write(0, operation);
    }

    public TrackedWaypoint getWaypoint() {
        return handle.getModifier().withType(TRACKED_WAYPOINT_CLASS, TRACKED_WAYPOINT_CONVERTER).read(0);
    }

    public void setWaypoint(TrackedWaypoint waypoint) {
        handle.getModifier().withType(TRACKED_WAYPOINT_CLASS, TRACKED_WAYPOINT_CONVERTER).write(0, waypoint);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Object operation(Operation operation) {
        return Enum.valueOf((Class) OPERATION_CLASS, operation.name());
    }

    @SuppressWarnings("unchecked")
    private static Optional<Integer> color(Object generic) {
        return (Optional<Integer>) WAYPOINT_ICON_COLOR.get(generic);
    }

    @SuppressWarnings("unchecked")
    private static UUID uuidIdentifier(Object generic) {
        Optional<UUID> identifier = (Optional<UUID>) EITHER_LEFT.invoke(GET_TRACKED_WAYPOINT_ID.invoke(generic));
        return identifier.orElseThrow(() ->
                new IllegalStateException("String waypoint identifiers are not supported by this wrapper"));
    }

    private static Object vec3i(WaypointPosition position) {
        return VEC3I_CONSTRUCTOR.invoke(position.x, position.y, position.z);
    }

    private static WaypointPosition position(Object generic) {
        return new WaypointPosition(
                (int) GET_VEC3I_X.invoke(generic),
                (int) GET_VEC3I_Y.invoke(generic),
                (int) GET_VEC3I_Z.invoke(generic));
    }

    public enum WaypointType {
        EMPTY, VEC3I, CHUNK, AZIMUTH
    }

    /** Wrapper for {@code Waypoint.Icon}. */
    public static final class WaypointIcon {
        private final MinecraftKey style;
        private final Optional<Integer> color;

        public WaypointIcon(MinecraftKey style, Optional<Integer> color) {
            this.style = style;
            this.color = color;
        }

        public static WaypointIcon defaultIcon() {
            return new WaypointIcon(new MinecraftKey("default"), Optional.empty());
        }

        public MinecraftKey getStyle() {
            return style;
        }

        public Optional<Integer> getColor() {
            return color;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WaypointIcon that)) {
                return false;
            }
            return Objects.equals(style, that.style) && Objects.equals(color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(style, color);
        }
    }

    public static final class WaypointPosition {
        private final int x;
        private final int y;
        private final int z;

        public WaypointPosition(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof WaypointPosition that)) {
                return false;
            }
            return x == that.x && y == that.y && z == that.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    /** Wrapper for {@code TrackedWaypoint}. */
    public static final class TrackedWaypoint {
        private final WaypointType type;
        private final UUID identifier;
        private final WaypointIcon icon;
        private final WaypointPosition position;
        private final ChunkCoordIntPair chunk;
        private final Float angle;

        private TrackedWaypoint(WaypointType type, UUID identifier, WaypointIcon icon,
                WaypointPosition position, ChunkCoordIntPair chunk, Float angle) {
            this.type = type;
            this.identifier = identifier;
            this.icon = icon;
            this.position = position;
            this.chunk = chunk;
            this.angle = angle;
        }

        public static TrackedWaypoint empty(UUID identifier) {
            return new TrackedWaypoint(WaypointType.EMPTY, identifier, WaypointIcon.defaultIcon(), null, null, null);
        }

        public static TrackedWaypoint position(UUID identifier, WaypointIcon icon, WaypointPosition position) {
            return new TrackedWaypoint(WaypointType.VEC3I, identifier, icon, position, null, null);
        }

        public static TrackedWaypoint chunk(UUID identifier, WaypointIcon icon, ChunkCoordIntPair chunk) {
            return new TrackedWaypoint(WaypointType.CHUNK, identifier, icon, null, chunk, null);
        }

        public static TrackedWaypoint azimuth(UUID identifier, WaypointIcon icon, float angle) {
            return new TrackedWaypoint(WaypointType.AZIMUTH, identifier, icon, null, null, angle);
        }

        public WaypointType getType() {
            return type;
        }

        public UUID getIdentifier() {
            return identifier;
        }

        public WaypointIcon getIcon() {
            return icon;
        }

        public WaypointPosition getPosition() {
            return position;
        }

        public ChunkCoordIntPair getChunk() {
            return chunk;
        }

        public Float getAngle() {
            return angle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TrackedWaypoint that)) {
                return false;
            }
            return type == that.type
                    && Objects.equals(identifier, that.identifier)
                    && Objects.equals(icon, that.icon)
                    && Objects.equals(position, that.position)
                    && Objects.equals(chunk, that.chunk)
                    && Objects.equals(angle, that.angle);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, identifier, icon, position, chunk, angle);
        }
    }
}
