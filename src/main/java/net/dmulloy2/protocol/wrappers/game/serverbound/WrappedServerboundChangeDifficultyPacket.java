package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChangeDifficultyPacket} (game phase, serverbound).
 */
public class WrappedServerboundChangeDifficultyPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.DIFFICULTY_CHANGE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(EnumWrappers.getDifficultyClass(), EnumWrappers.getDifficultyConverter());

    public WrappedServerboundChangeDifficultyPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChangeDifficultyPacket(EnumWrappers.Difficulty difficulty) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(difficulty)));
    }

    public WrappedServerboundChangeDifficultyPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public EnumWrappers.Difficulty getDifficulty() {
        return handle.getEnumModifier(EnumWrappers.Difficulty.class, EnumWrappers.getDifficultyClass()).read(0);
    }

    public void setDifficulty(EnumWrappers.Difficulty difficulty) {
        handle.getEnumModifier(EnumWrappers.Difficulty.class, EnumWrappers.getDifficultyClass()).write(0, difficulty);
    }
}
