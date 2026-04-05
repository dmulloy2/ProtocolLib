package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AutoWrapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.dmulloy2.protocol.AbstractPacket;
import com.comphenix.protocol.reflect.EquivalentConverter;

/**
 * Wrapper for {@code ClientboundMapItemDataPacket} (Play phase, clientbound).
 *
 * <p>NMS structure (1.21+):
 * <pre>
 * record ClientboundMapItemDataPacket(
 *         MapId                                   mapId,         // field 0
 *         byte                                    scale,         // field 1
 *         boolean                                 locked,        // field 2
 *         Optional&lt;List&lt;MapDecoration&gt;&gt;  decorations,   // field 3
 *         Optional&lt;MapItemSavedData.MapPatch&gt; colorPatch)   // field 4
 *
 * record MapId(int id)
 * record MapDecoration(Holder&lt;MapDecorationType&gt; type, byte x, byte y, byte rot, Optional&lt;Component&gt; name)
 * record MapItemSavedData.MapPatch(int startX, int startY, int width, int height, byte[] mapColors)
 * </pre>
 *
 * <p>All five fields are accessed via {@link AutoWrapper} POJOs using the raw
 * {@link PacketContainer#getModifier()} accessor (global field indices 0–4).
 * {@link WrappedMapDecoration#type} ({@code Holder&lt;MapDecorationType&gt;}) and
 * {@link WrappedMapDecoration#name} ({@code Optional&lt;Component&gt;}) are kept as raw
 * {@link Object} pass-throughs to avoid NMS compile-time dependencies.
 */
public class WrappedClientboundMapItemDataPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MAP;

    // AutoWrappers for the nested NMS record types.
    private static final AutoWrapper<WrappedMapId> MAP_ID_WRAPPER =
            AutoWrapper.wrap(WrappedMapId.class, "world.level.saveddata.maps.MapId");
    private static final AutoWrapper<WrappedMapDecoration> MAP_DECORATION_WRAPPER =
            AutoWrapper.wrap(WrappedMapDecoration.class, "world.level.saveddata.maps.MapDecoration");
    private static final AutoWrapper<WrappedMapPatch> MAP_PATCH_WRAPPER =
            AutoWrapper.wrap(WrappedMapPatch.class, "world.level.saveddata.maps.MapItemSavedData$MapPatch");

    public WrappedClientboundMapItemDataPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundMapItemDataPacket(int mapId, byte scale, boolean locked, Optional<List<WrappedMapDecoration>> decorations, Optional<WrappedMapPatch> colorPatch) {
        this();
        setMapId(mapId);
        setScale(scale);
        setLocked(locked);
        setDecorations(decorations);
        setColorPatch(colorPatch);
    }

    public WrappedClientboundMapItemDataPacket(int mapId, byte scale, boolean locked) {
        this(mapId, scale, locked, Optional.empty(), Optional.empty());
    }

    public WrappedClientboundMapItemDataPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    // ---- mapId (global field 0) ---------------------------------------------

    /** Returns the map id from the wrapped {@code MapId} record. */
    public int getMapId() {
        return MAP_ID_WRAPPER.getSpecific(handle.getModifier().read(0)).id;
    }

    public void setMapId(int mapId) {
        WrappedMapId w = new WrappedMapId();
        w.id = mapId;
        handle.getModifier().write(0, MAP_ID_WRAPPER.getGeneric(w));
    }

    // ---- scale (global field 1) ---------------------------------------------

    public byte getScale() {
        return handle.getBytes().read(0);
    }

    public void setScale(byte scale) {
        handle.getBytes().write(0, scale);
    }

    // ---- locked (global field 2) --------------------------------------------

    public boolean isLocked() {
        return handle.getBooleans().read(0);
    }

    public void setLocked(boolean locked) {
        handle.getBooleans().write(0, locked);
    }

    // ---- decorations (global field 3) ---------------------------------------

    /**
     * Returns the map decorations.
     * The {@link WrappedMapDecoration#type} and {@link WrappedMapDecoration#name} fields
     * are raw NMS objects ({@code Holder<MapDecorationType>} and {@code Optional<Component>}
     * respectively) — pass them through unchanged when writing back.
     */
    @SuppressWarnings("unchecked")
    public Optional<List<WrappedMapDecoration>> getDecorations() {
        Optional<List<Object>> raw = (Optional<List<Object>>) handle.getModifier().read(3);
        if (raw == null) return Optional.empty();
        return raw.map(list -> list.stream()
                .map(MAP_DECORATION_WRAPPER::getSpecific)
                .collect(Collectors.toList()));
    }

    public void setDecorations(Optional<List<WrappedMapDecoration>> decorations) {
        Optional<List<Object>> nms = decorations.map(list -> list.stream()
                .map(MAP_DECORATION_WRAPPER::getGeneric)
                .collect(Collectors.toList()));
        handle.getModifier().write(3, nms);
    }

    // ---- colorPatch (global field 4) ----------------------------------------

    @SuppressWarnings("unchecked")
    public Optional<WrappedMapPatch> getColorPatch() {
        Optional<Object> raw = (Optional<Object>) handle.getModifier().read(4);
        if (raw == null) return Optional.empty();
        return raw.map(MAP_PATCH_WRAPPER::getSpecific);
    }

    public void setColorPatch(Optional<WrappedMapPatch> colorPatch) {
        Optional<Object> nms = colorPatch.map(MAP_PATCH_WRAPPER::getGeneric);
        handle.getModifier().write(4, nms);
    }

    // ---- inner POJOs --------------------------------------------------------

    /**
     * POJO mirroring {@code record MapId(int id)}.
     * Field {@code id} must be declared first (and only) to match the NMS record layout.
     */
    public static final class WrappedMapId {
        public int id;

        public WrappedMapId() {
        }
    }

    /**
     * POJO mirroring
     * {@code record MapDecoration(Holder<MapDecorationType> type, byte x, byte y, byte rot, Optional<Component> name)}.
     *
     * <p>Fields must be declared in the same order as the NMS record.
     * {@code type} ({@code Holder<MapDecorationType>}) and {@code name} ({@code Optional<Component>})
     * are typed as {@link Object} — they are passed through the AutoWrapper without conversion.
     */
    public static final class WrappedMapDecoration {
        /** NMS {@code Holder<MapDecorationType>} — pass-through as raw {@link Object}. */
        public Object type;
        public byte x;
        public byte y;
        public byte rot;
        /** NMS {@code Optional<Component>} — pass-through as raw {@link Object}. */
        public Object name;

        public WrappedMapDecoration() {
        }
    }

    /**
     * POJO mirroring
     * {@code record MapItemSavedData.MapPatch(int startX, int startY, int width, int height, byte[] mapColors)}.
     *
     * <p>Fields must be declared in the same order as the NMS record.
     */
    public static final class WrappedMapPatch {
        public int startX;
        public int startY;
        public int width;
        public int height;
        public byte[] mapColors;

        public WrappedMapPatch() {
        }
    }
}
