package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.Converters;
import java.util.Map;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundCustomReportDetailsPacket} (configuration phase, clientbound).
 *
 * <p>Packet structure:
 * <ul>
 *   <li>{@code Map<String, String> details} – custom crash report detail entries</li>
 * </ul>
 */
public class WrappedClientboundCustomReportDetailsPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Configuration.Server.REPORT_DETAILS;

    public WrappedClientboundCustomReportDetailsPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundCustomReportDetailsPacket(Map<String, String> details) {
        this();
        setDetails(details);
    }

    public WrappedClientboundCustomReportDetailsPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public Map<String, String> getDetails() {
        return handle.getMaps(
                Converters.passthrough(String.class),
                Converters.passthrough(String.class)).read(0);
    }

    public void setDetails(Map<String, String> details) {
        handle.getMaps(
                Converters.passthrough(String.class),
                Converters.passthrough(String.class)).write(0, details);
    }
}
