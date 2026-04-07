package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Arrays;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundRecipeBookChangeSettingsPacket} (game phase, serverbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code RecipeBookType bookType} – which recipe book tab is changing (CRAFTING, FURNACE, etc.)</li>
 *   <li>{@code boolean isOpen} – whether the recipe book is open</li>
 *   <li>{@code boolean isFiltering} – whether to filter to craftable recipes only</li>
 * </ul>
 */
public class WrappedServerboundRecipeBookChangeSettingsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.RECIPE_SETTINGS;

    /**
     * Mirrors {@code net.minecraft.world.inventory.RecipeBookType}.
     * Constants must match NMS enum names exactly.
     */
    public enum RecipeBookType { CRAFTING, FURNACE, BLAST_FURNACE, SMOKER }

    private static final Class<?> BOOK_TYPE_NMS_CLASS = Arrays.stream(TYPE.getPacketClass().getDeclaredFields())
            .map(java.lang.reflect.Field::getType)
            .filter(Class::isEnum)
            .findFirst()
            .orElse(null);

    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(BOOK_TYPE_NMS_CLASS, new EnumWrappers.EnumConverter<>(BOOK_TYPE_NMS_CLASS, RecipeBookType.class))
            .withParam(boolean.class)
            .withParam(boolean.class);

    public WrappedServerboundRecipeBookChangeSettingsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundRecipeBookChangeSettingsPacket(RecipeBookType bookType, boolean open, boolean filtering) {
        this(new PacketContainer(TYPE, CONSTRUCTOR.create(bookType, open, filtering)));
    }

    public WrappedServerboundRecipeBookChangeSettingsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public RecipeBookType getBookType() {
        return handle.getEnumModifier(RecipeBookType.class, 0).readSafely(0);
    }

    public void setBookType(RecipeBookType bookType) {
        handle.getEnumModifier(RecipeBookType.class, 0).writeSafely(0, bookType);
    }

    public boolean isOpen() {
        return handle.getBooleans().readSafely(0);
    }

    public void setOpen(boolean open) {
        handle.getBooleans().writeSafely(0, open);
    }

    public boolean isFiltering() {
        return handle.getBooleans().readSafely(1);
    }

    public void setFiltering(boolean filtering) {
        handle.getBooleans().writeSafely(1, filtering);
    }
}
