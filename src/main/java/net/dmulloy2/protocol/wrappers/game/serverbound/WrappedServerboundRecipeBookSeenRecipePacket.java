package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AutoWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundRecipeBookSeenRecipePacket} (game phase, serverbound).
 *
 * <p>NMS: {@code ServerboundRecipeBookSeenRecipePacket(RecipeDisplayId recipe)}
 * where {@code RecipeDisplayId} is {@code record RecipeDisplayId(int index)}.
 */
public class WrappedServerboundRecipeBookSeenRecipePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.RECIPE_DISPLAYED;

    private static final AutoWrapper<WrappedRecipeDisplayId> RECIPE_ID_WRAPPER =
            AutoWrapper.wrap(WrappedRecipeDisplayId.class, "world.item.crafting.display.RecipeDisplayId");

    /** Mirror of {@code record RecipeDisplayId(int index)}. */
    public static final class WrappedRecipeDisplayId {
        public int index;
        public WrappedRecipeDisplayId() {}
    }

    public WrappedServerboundRecipeBookSeenRecipePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundRecipeBookSeenRecipePacket(int recipeIndex) {
        this();
        setRecipeIndex(recipeIndex);
    }

    public WrappedServerboundRecipeBookSeenRecipePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the integer index of the recipe that the client has "seen". */
    public int getRecipeIndex() {
        return RECIPE_ID_WRAPPER.getSpecific(handle.getModifier().read(0)).index;
    }

    /** Sets the recipe index (wraps it in a {@code RecipeDisplayId} NMS record). */
    public void setRecipeIndex(int index) {
        WrappedRecipeDisplayId id = new WrappedRecipeDisplayId();
        id.index = index;
        handle.getModifier().write(0, RECIPE_ID_WRAPPER.getGeneric(id));
    }
}
