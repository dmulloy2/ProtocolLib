package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.time.Instant;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ServerboundChatCommandSignedPacket} (game phase, serverbound).
 */
public class WrappedServerboundChatCommandSignedPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Client.CHAT_COMMAND_SIGNED;

    public WrappedServerboundChatCommandSignedPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedServerboundChatCommandSignedPacket(String command, long salt, Instant timeStamp) {
        this();
        setCommand(command);
        setSalt(salt);
        setTimeStamp(timeStamp);
    }

    public WrappedServerboundChatCommandSignedPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getCommand() {
        return handle.getStrings().read(0);
    }

    public void setCommand(String command) {
        handle.getStrings().write(0, command);
    }

    public long getSalt() {
        return handle.getLongs().read(0);
    }

    public void setSalt(long salt) {
        handle.getLongs().write(0, salt);
    }

    public Instant getTimeStamp() {
        return handle.getInstants().read(0);
    }

    public void setTimeStamp(Instant timeStamp) {
        handle.getInstants().write(0, timeStamp);
    }

    // TODO: missing field 'argumentSignatures' (NMS type: ArgumentSignatures — map of argument name → MessageSignature)
    //   Use handle.getModifier().read(N) for the raw ArgumentSignatures object,
    //   or add a dedicated converter once ArgumentSignatures has a ProtocolLib wrapper.
    // TODO: missing field 'lastSeenMessages' (NMS type: LastSeenMessages.Update — acknowledgement of recent chat messages)
    //   Holds a sequence int and a bitset. Use handle.getModifier().read(N) for the raw Update object.
}
