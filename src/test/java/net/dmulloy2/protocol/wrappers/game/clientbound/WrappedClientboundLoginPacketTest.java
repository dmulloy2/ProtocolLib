package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLoginPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLoginPacket w = new WrappedClientboundLoginPacket(3, false, 5, 3, 7, true, true, false, true, new HashSet<>());

        assertEquals(PacketType.Play.Server.LOGIN, w.getHandle().getType());

        assertEquals(3, w.getPlayerId());
        assertFalse(w.isHardcore());
        assertEquals(5, w.getMaxPlayers());
        assertEquals(3, w.getChunkRadius());
        assertEquals(7, w.getSimulationDistance());
        assertTrue(w.isReducedDebugInfo());
        assertTrue(w.isShowDeathScreen());
        assertFalse(w.isDoLimitedCrafting());
        assertTrue(w.isEnforcesSecureChat());
        assertEquals(new HashSet<>(), w.getLevels());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLoginPacket w = new WrappedClientboundLoginPacket();

        assertEquals(PacketType.Play.Server.LOGIN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundLoginPacket source = new WrappedClientboundLoginPacket(3, false, 5, 3, 7, true, true, false, true, new HashSet<>());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLoginPacket wrapper = new WrappedClientboundLoginPacket(container);

        assertEquals(3, wrapper.getPlayerId());
        assertFalse(wrapper.isHardcore());
        assertEquals(5, wrapper.getMaxPlayers());
        assertEquals(3, wrapper.getChunkRadius());
        assertEquals(7, wrapper.getSimulationDistance());
        assertTrue(wrapper.isReducedDebugInfo());
        assertTrue(wrapper.isShowDeathScreen());
        assertFalse(wrapper.isDoLimitedCrafting());
        assertTrue(wrapper.isEnforcesSecureChat());
        assertEquals(new HashSet<>(), wrapper.getLevels());

        wrapper.setPlayerId(9);
        wrapper.setHardcore(true);
        wrapper.setMaxPlayers(0);
        wrapper.setChunkRadius(42);
        wrapper.setSimulationDistance(9);
        wrapper.setReducedDebugInfo(false);
        wrapper.setShowDeathScreen(false);
        wrapper.setDoLimitedCrafting(true);
        wrapper.setEnforcesSecureChat(false);
        wrapper.setLevels(new HashSet<>());

        assertEquals(9, wrapper.getPlayerId());
        assertTrue(wrapper.isHardcore());
        assertEquals(0, wrapper.getMaxPlayers());
        assertEquals(42, wrapper.getChunkRadius());
        assertEquals(9, wrapper.getSimulationDistance());
        assertFalse(wrapper.isReducedDebugInfo());
        assertFalse(wrapper.isShowDeathScreen());
        assertTrue(wrapper.isDoLimitedCrafting());
        assertFalse(wrapper.isEnforcesSecureChat());
        assertEquals(new HashSet<>(), wrapper.getLevels());

        assertEquals(9, source.getPlayerId());
        assertTrue(source.isHardcore());
        assertEquals(0, source.getMaxPlayers());
        assertEquals(42, source.getChunkRadius());
        assertEquals(9, source.getSimulationDistance());
        assertFalse(source.isReducedDebugInfo());
        assertFalse(source.isShowDeathScreen());
        assertTrue(source.isDoLimitedCrafting());
        assertFalse(source.isEnforcesSecureChat());
        assertEquals(new HashSet<>(), source.getLevels());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLoginPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
