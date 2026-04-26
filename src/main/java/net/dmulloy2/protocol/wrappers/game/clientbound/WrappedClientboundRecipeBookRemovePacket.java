package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AutoWrapper;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for {@code ClientboundRecipeBookRemovePacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code List<RecipeDisplayId> recipes} – recipe IDs to remove from the client book</li>
 * </ul>
 */
public class WrappedClientboundRecipeBookRemovePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_BOOK_REMOVE;

    /**
     * AutoWrapper for {@code record RecipeDisplayId(int index)}.
     * Shared with {@link net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundRecipeBookSeenRecipePacket}.
     */
    private static final AutoWrapper<WrappedRecipeDisplayId> RECIPE_ID_WRAPPER =
            AutoWrapper.wrap(WrappedRecipeDisplayId.class, "world.item.crafting.display.RecipeDisplayId");

    public WrappedClientboundRecipeBookRemovePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRecipeBookRemovePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the list of recipe IDs to remove, as integer indices.
     */
    public List<Integer> getRecipeIndices() {
        List<WrappedRecipeDisplayId> ids = handle.getLists(RECIPE_ID_WRAPPER).read(0);
        if (ids == null) return List.of();
        List<Integer> indices = new ArrayList<>(ids.size());
        for (WrappedRecipeDisplayId id : ids) {
            indices.add(id.index);
        }
        return indices;
    }

    public void setRecipeIndices(List<Integer> indices) {
        List<WrappedRecipeDisplayId> wrapped = new ArrayList<>(indices.size());
        for (int i : indices) {
            WrappedRecipeDisplayId w = new WrappedRecipeDisplayId();
            w.index = i;
            wrapped.add(w);
        }
        handle.getLists(RECIPE_ID_WRAPPER).write(0, wrapped);
    }

    /** POJO mirroring {@code record RecipeDisplayId(int index)}. */
    public static final class WrappedRecipeDisplayId {
        public int index;
        public WrappedRecipeDisplayId() {}
    }
}
