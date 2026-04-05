package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;

/**
 * Wrapper for {@code ClientboundLevelChunkWithLightPacket} (game phase, clientbound).
 */
public class WrappedClientboundLevelChunkWithLightPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.MAP_CHUNK;

    public WrappedClientboundLevelChunkWithLightPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLevelChunkWithLightPacket(int x, int z) {
        this();
        setX(x);
        setZ(z);
    }

    public WrappedClientboundLevelChunkWithLightPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getX() {
        return handle.getIntegers().read(0);
    }

    public void setX(int x) {
        handle.getIntegers().write(0, x);
    }

    public int getZ() {
        return handle.getIntegers().read(1);
    }

    public void setZ(int z) {
        handle.getIntegers().write(1, z);
    }
}
