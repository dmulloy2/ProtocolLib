package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.AutoWrapper;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.Either;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.AbstractPacket;

import java.util.List;

/**
 * Wrapper for {@code ClientboundServerLinksPacket} (configuration phase, clientbound).
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code List<ServerLinks.UntrustedEntry> links} – server-provided links</li>
 * </ul>
 *
 * <p>NMS: {@code record UntrustedEntry(Either<KnownLinkType, Component> type, String link)}
 */
public class WrappedClientboundServerLinksPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.SERVER_LINKS;

    /** Mirrors NMS {@code ServerLinks.KnownLinkType}; constant order must match. */
    public enum KnownLinkType {
        BUG_REPORT, COMMUNITY_GUIDELINES, SUPPORT, STATUS, FEEDBACK,
        COMMUNITY, WEBSITE, FORUMS, NEWS, ANNOUNCEMENTS
    }

    private static final Class<?> NMS_KNOWN_LINK_TYPE_CLASS =
            MinecraftReflection.getMinecraftClass("server.ServerLinks$KnownLinkType");

    private static final EquivalentConverter<Either<KnownLinkType, WrappedChatComponent>> TYPE_CONVERTER =
            BukkitConverters.getEitherConverter(
                    EnumWrappers.getGenericConverter(NMS_KNOWN_LINK_TYPE_CLASS, KnownLinkType.class),
                    BukkitConverters.getWrappedChatComponentConverter());

    private static final AutoWrapper<WrappedUntrustedEntry> ENTRY_WRAPPER =
            AutoWrapper.wrap(WrappedUntrustedEntry.class, "server.ServerLinks$UntrustedEntry")
                    .field(0, TYPE_CONVERTER);

    public WrappedClientboundServerLinksPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundServerLinksPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Returns the list of server links.
     */
    public List<WrappedUntrustedEntry> getLinks() {
        List<WrappedUntrustedEntry> links = handle.getLists(ENTRY_WRAPPER).read(0);
        return links != null ? links : List.of();
    }

    public void setLinks(List<WrappedUntrustedEntry> links) {
        handle.getLists(ENTRY_WRAPPER).write(0, links);
    }

    /**
     * POJO mirroring {@code record ServerLinks.UntrustedEntry(Either<KnownLinkType, Component> type, String link)}.
     */
    public static final class WrappedUntrustedEntry {
        /** Either a {@link KnownLinkType} (left) or a custom {@link WrappedChatComponent} display name (right). */
        public Either<KnownLinkType, WrappedChatComponent> type;
        /** The URL string. */
        public String link;

        public WrappedUntrustedEntry() {}

        public WrappedUntrustedEntry(Either<KnownLinkType, WrappedChatComponent> type, String link) {
            this.type = type;
            this.link = link;
        }
    }
}
