package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundClientInformationPacket} (configuration phase, serverbound).
 *
 * <p>The packet wraps a single {@code ClientInformation} record, exposed via
 * {@link #getInformation()} / {@link #setInformation(WrappedClientInformation)}.
 *
 * <p>NMS: {@code record ClientInformation(String language, int viewDistance,
 * ChatVisiblity chatVisibility, boolean chatColors, int modelCustomisation,
 * HumanoidArm mainHand, boolean textFilteringEnabled, boolean allowsListing,
 * ParticleStatus particleStatus)}
 */
public class WrappedServerboundClientInformationPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Client.CLIENT_INFORMATION;

    private static final Class<?> CLIENT_INFORMATION_CLASS =
            MinecraftReflection.getMinecraftClass("server.level.ClientInformation");
    private static final Class<?> HUMANOID_ARM_CLASS =
            MinecraftReflection.getMinecraftClass("world.entity.HumanoidArm");
    private static final Class<?> PARTICLE_STATUS_CLASS =
            MinecraftReflection.getMinecraftClass("server.level.ParticleStatus");

    private static final EnumWrappers.EnumConverter<MainHand> MAIN_HAND_CONVERTER =
            new EnumWrappers.EnumConverter<>(HUMANOID_ARM_CLASS, MainHand.class);

    private static final AutoWrapper<WrappedClientInformation> INFORMATION_WRAPPER =
            AutoWrapper.wrap(WrappedClientInformation.class, CLIENT_INFORMATION_CLASS)
                    .field(2, EnumWrappers.getChatVisibilityConverter())
                    .field(5, MAIN_HAND_CONVERTER)
                    .field(8, EnumWrappers.getGenericConverter(PARTICLE_STATUS_CLASS, ParticleStatus.class));

    public WrappedServerboundClientInformationPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundClientInformationPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /** Returns the client settings, read from the underlying NMS {@code ClientInformation} record. */
    public WrappedClientInformation getInformation() {
        return handle.getModifier().withType(CLIENT_INFORMATION_CLASS, INFORMATION_WRAPPER).read(0);
    }

    /** Writes the client settings back to the underlying NMS {@code ClientInformation} record. */
    public void setInformation(WrappedClientInformation info) {
        handle.getModifier().withType(CLIENT_INFORMATION_CLASS, INFORMATION_WRAPPER).write(0, info);
    }

    /** POJO mirroring {@code record ClientInformation(...)}. */
    public static final class WrappedClientInformation {
        public String language;
        public int viewDistance;
        /** ProtocolLib {@link EnumWrappers.ChatVisibility}. */
        public EnumWrappers.ChatVisibility chatVisibility;
        public boolean chatColors;
        public int modelCustomisation;
        /** Dominant arm: {@link MainHand#LEFT} or {@link MainHand#RIGHT}. */
        public MainHand mainHand;
        public boolean textFilteringEnabled;
        public boolean allowsListing;
        public ParticleStatus particleStatus;

        public WrappedClientInformation() {}
    }

    /** Mirrors {@code HumanoidArm}: the player's dominant arm setting. */
    public enum MainHand { LEFT, RIGHT }

    /** Mirrors NMS {@code ParticleStatus}; constant order must match. */
    public enum ParticleStatus { ALL, DECREASED, MINIMAL }
}
