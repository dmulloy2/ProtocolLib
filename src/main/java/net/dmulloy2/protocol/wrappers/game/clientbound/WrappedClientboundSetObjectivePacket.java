package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedNumberFormat;
import java.util.Optional;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundSetObjectivePacket} (game phase, clientbound).
 *
 * <p>NMS field order: objectiveName (String), displayName (Component),
 * renderType (ObjectiveCriteria.RenderType enum), numberFormat (Optional), method (int).
 */
public class WrappedClientboundSetObjectivePacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.SCOREBOARD_OBJECTIVE;

    public WrappedClientboundSetObjectivePacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundSetObjectivePacket(String objectiveName, int method, WrappedChatComponent displayName, EnumWrappers.RenderType renderType, Optional<WrappedNumberFormat> numberFormat) {
        this();
        setObjectiveName(objectiveName);
        setMethod(method);
        setDisplayName(displayName);
        setRenderType(renderType);
        setNumberFormat(numberFormat);
    }

    public WrappedClientboundSetObjectivePacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getObjectiveName() {
        return handle.getStrings().read(0);
    }

    public void setObjectiveName(String objectiveName) {
        handle.getStrings().write(0, objectiveName);
    }

    public int getMethod() {
        return handle.getIntegers().read(0);
    }

    public void setMethod(int method) {
        handle.getIntegers().write(0, method);
    }

    public WrappedChatComponent getDisplayName() {
        return handle.getChatComponents().read(0);
    }

    public void setDisplayName(WrappedChatComponent displayName) {
        handle.getChatComponents().write(0, displayName);
    }

    public EnumWrappers.RenderType getRenderType() {
        return handle.getRenderTypes().read(0);
    }

    public void setRenderType(EnumWrappers.RenderType renderType) {
        handle.getRenderTypes().write(0, renderType);
    }

    public Optional<WrappedNumberFormat> getNumberFormat() {
        return handle.getOptionals(BukkitConverters.getWrappedNumberFormatConverter()).read(0);
    }

    public void setNumberFormat(Optional<WrappedNumberFormat> numberFormat) {
        handle.getOptionals(BukkitConverters.getWrappedNumberFormatConverter()).write(0, numberFormat);
    }
}
