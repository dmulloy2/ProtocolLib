package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.AutoWrapper;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundRecipeBookSettingsPacket} (game phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code RecipeBookSettings bookSettings} – open/filter state per recipe book type</li>
 * </ul>
 *
 * <p>NMS: {@code RecipeBookSettings} holds four {@code TypeSettings crafting/furnace/blastFurnace/smoker}
 * where {@code record TypeSettings(boolean open, boolean filtering)}.
 */
public class WrappedClientboundRecipeBookSettingsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RECIPE_BOOK_SETTINGS;

    private static final AutoWrapper<WrappedTypeSettings> TYPE_SETTINGS_WRAPPER =
            AutoWrapper.wrap(WrappedTypeSettings.class, "stats.RecipeBookSettings$TypeSettings");

    private static final AutoWrapper<WrappedRecipeBookSettings> BOOK_SETTINGS_WRAPPER =
            AutoWrapper.wrap(WrappedRecipeBookSettings.class, "stats.RecipeBookSettings")
                    .field(0, TYPE_SETTINGS_WRAPPER)
                    .field(1, TYPE_SETTINGS_WRAPPER)
                    .field(2, TYPE_SETTINGS_WRAPPER)
                    .field(3, TYPE_SETTINGS_WRAPPER);

    public WrappedClientboundRecipeBookSettingsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRecipeBookSettingsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public WrappedRecipeBookSettings getBookSettings() {
        return BOOK_SETTINGS_WRAPPER.getSpecific(handle.getModifier().read(0));
    }

    public void setBookSettings(WrappedRecipeBookSettings settings) {
        handle.getModifier().write(0, BOOK_SETTINGS_WRAPPER.getGeneric(settings));
    }

    /**
     * POJO mirroring {@code RecipeBookSettings} (four {@link WrappedTypeSettings} in NMS field order:
     * crafting, furnace, blastFurnace, smoker).
     */
    public static final class WrappedRecipeBookSettings {
        public WrappedTypeSettings crafting  = new WrappedTypeSettings();
        public WrappedTypeSettings furnace   = new WrappedTypeSettings();
        public WrappedTypeSettings blastFurnace = new WrappedTypeSettings();
        public WrappedTypeSettings smoker    = new WrappedTypeSettings();

        public WrappedRecipeBookSettings() {}
    }

    /**
     * POJO mirroring {@code record RecipeBookSettings.TypeSettings(boolean open, boolean filtering)}.
     */
    public static final class WrappedTypeSettings {
        public boolean open;
        public boolean filtering;

        public WrappedTypeSettings() {}

        public WrappedTypeSettings(boolean open, boolean filtering) {
            this.open = open;
            this.filtering = filtering;
        }
    }
}
