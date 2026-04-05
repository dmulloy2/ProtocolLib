package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.EquivalentConstructor;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChangeGameModePacket} (game phase, serverbound).
 */
public class WrappedServerboundChangeGameModePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHANGE_GAME_MODE;
    private static final EquivalentConstructor CONSTRUCTOR = new EquivalentConstructor(TYPE)
            .withParam(EnumWrappers.getGameModeClass(), EnumWrappers.getGameModeConverter());

    public WrappedServerboundChangeGameModePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChangeGameModePacket(EnumWrappers.NativeGameMode mode) {
        this(PacketContainer.fromPacket(CONSTRUCTOR.create(mode)));
    }

    public WrappedServerboundChangeGameModePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public EnumWrappers.NativeGameMode getMode() {
        return handle.getEnumModifier(EnumWrappers.NativeGameMode.class, EnumWrappers.getGameModeClass()).read(0);
    }

    public void setMode(EnumWrappers.NativeGameMode mode) {
        handle.getEnumModifier(EnumWrappers.NativeGameMode.class, EnumWrappers.getGameModeClass()).write(0, mode);
    }
}
