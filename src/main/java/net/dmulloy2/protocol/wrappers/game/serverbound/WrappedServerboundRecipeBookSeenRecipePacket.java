package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.utility.MinecraftReflection;
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

    private static final Class<?> RECIPE_DISPLAY_ID_CLASS =
            MinecraftReflection.getMinecraftClass("world.item.crafting.display.RecipeDisplayId");

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(RECIPE_DISPLAY_ID_CLASS, RECIPE_ID_WRAPPER);

    /** Mirror of {@code record RecipeDisplayId(int index)}. */
    public static final class WrappedRecipeDisplayId {
        public int index;
        public WrappedRecipeDisplayId() {}
    }

    public WrappedServerboundRecipeBookSeenRecipePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundRecipeBookSeenRecipePacket(int recipeIndex) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(makeId(recipeIndex))));
    }

    public WrappedServerboundRecipeBookSeenRecipePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    private static WrappedRecipeDisplayId makeId(int index) {
        WrappedRecipeDisplayId id = new WrappedRecipeDisplayId();
        id.index = index;
        return id;
    }

    /** Returns the integer index of the recipe that the client has "seen". */
    public int getRecipeIndex() {
        WrappedRecipeDisplayId id = handle.getModifier()
                .withType(RECIPE_DISPLAY_ID_CLASS, RECIPE_ID_WRAPPER).read(0);
        return id == null ? 0 : id.index;
    }

    /** Sets the recipe index (wraps it in a {@code RecipeDisplayId} NMS record). */
    public void setRecipeIndex(int index) {
        handle.getModifier()
                .withType(RECIPE_DISPLAY_ID_CLASS, RECIPE_ID_WRAPPER)
                .write(0, makeId(index));
    }
}
