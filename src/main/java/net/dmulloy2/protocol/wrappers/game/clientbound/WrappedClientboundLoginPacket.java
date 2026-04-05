package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import java.util.Set;
import net.dmulloy2.protocol.AbstractPacket;
import org.bukkit.World;

/**
 * Wrapper for {@code ClientboundLoginPacket} (game phase, clientbound).
 */
public class WrappedClientboundLoginPacket extends AbstractPacket {

    public static final PacketType TYPE = PacketType.Play.Server.LOGIN;

    public WrappedClientboundLoginPacket() {
        super(new PacketContainer(TYPE), TYPE);
    }

    public WrappedClientboundLoginPacket(int playerId, boolean hardcore, int maxPlayers, int chunkRadius, int simulationDistance, boolean reducedDebugInfo, boolean showDeathScreen, boolean doLimitedCrafting, boolean enforcesSecureChat, Set<World> levels) {
        this();
        setPlayerId(playerId);
        setHardcore(hardcore);
        setMaxPlayers(maxPlayers);
        setChunkRadius(chunkRadius);
        setSimulationDistance(simulationDistance);
        setReducedDebugInfo(reducedDebugInfo);
        setShowDeathScreen(showDeathScreen);
        setDoLimitedCrafting(doLimitedCrafting);
        setEnforcesSecureChat(enforcesSecureChat);
        setLevels(levels);
    }

    public WrappedClientboundLoginPacket(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getPlayerId() {
        return handle.getIntegers().read(0);
    }

    public void setPlayerId(int playerId) {
        handle.getIntegers().write(0, playerId);
    }

    public boolean isHardcore() {
        return handle.getBooleans().read(0);
    }

    public void setHardcore(boolean hardcore) {
        handle.getBooleans().write(0, hardcore);
    }

    public int getMaxPlayers() {
        return handle.getIntegers().read(1);
    }

    public void setMaxPlayers(int maxPlayers) {
        handle.getIntegers().write(1, maxPlayers);
    }

    public int getChunkRadius() {
        return handle.getIntegers().read(2);
    }

    public void setChunkRadius(int chunkRadius) {
        handle.getIntegers().write(2, chunkRadius);
    }

    public int getSimulationDistance() {
        return handle.getIntegers().read(3);
    }

    public void setSimulationDistance(int simulationDistance) {
        handle.getIntegers().write(3, simulationDistance);
    }

    public boolean isReducedDebugInfo() {
        return handle.getBooleans().read(1);
    }

    public void setReducedDebugInfo(boolean reducedDebugInfo) {
        handle.getBooleans().write(1, reducedDebugInfo);
    }

    public boolean isShowDeathScreen() {
        return handle.getBooleans().read(2);
    }

    public void setShowDeathScreen(boolean showDeathScreen) {
        handle.getBooleans().write(2, showDeathScreen);
    }

    public boolean isDoLimitedCrafting() {
        return handle.getBooleans().read(3);
    }

    public void setDoLimitedCrafting(boolean doLimitedCrafting) {
        handle.getBooleans().write(3, doLimitedCrafting);
    }

    public boolean isEnforcesSecureChat() {
        return handle.getBooleans().read(4);
    }

    public void setEnforcesSecureChat(boolean enforcesSecureChat) {
        handle.getBooleans().write(4, enforcesSecureChat);
    }

    /** Returns the set of all dimension levels/worlds the client is allowed to enter. */
    public Set<World> getLevels() {
        return handle.getSets(BukkitConverters.getWorldKeyConverter()).read(0);
    }

    public void setLevels(Set<World> levels) {
        handle.getSets(BukkitConverters.getWorldKeyConverter()).write(0, levels);
    }

    // TODO: missing field 'commonPlayerSpawnInfo' (NMS type: CommonPlayerSpawnInfo)
    //   CommonPlayerSpawnInfo holds: Holder<DimensionType> dimensionType, ResourceKey<Level> dimension,
    //   long seed, GameType gameType, @Nullable GameType previousGameType, boolean isDebug,
    //   boolean isFlat, Optional<GlobalPos> lastDeathLocation, int portalCooldown, int seaLevel.
    //   This is a shared type used by both Login and Respawn packets.
    //   Add a WrappedCommonPlayerSpawnInfo wrapper class and a getCommonPlayerSpawnInfos() accessor
    //   in AbstractStructure, then expose it here.
}
