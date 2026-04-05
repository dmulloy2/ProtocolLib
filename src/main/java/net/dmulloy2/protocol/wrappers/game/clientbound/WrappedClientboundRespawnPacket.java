package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dmulloy2.protocol.AbstractPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;

/**
 * Wrapper for {@code ClientboundRespawnPacket} (game phase, clientbound).
 */
public class WrappedClientboundRespawnPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.RESPAWN;

    public WrappedClientboundRespawnPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundRespawnPacket(byte dataToKeep) {
        this();
        setDataToKeep(dataToKeep);
    }

    public WrappedClientboundRespawnPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public byte getDataToKeep() {
        return handle.getBytes().read(0);
    }

    public void setDataToKeep(byte dataToKeep) {
        handle.getBytes().write(0, dataToKeep);
    }

    // TODO: missing field 'commonPlayerSpawnInfo' (NMS type: CommonPlayerSpawnInfo)
    //   Same as the Login packet's commonPlayerSpawnInfo. Add a WrappedCommonPlayerSpawnInfo wrapper
    //   and getCommonPlayerSpawnInfos() in AbstractStructure, then use it here.
}
