package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerInfoUpdatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerInfoUpdatePacket w = new WrappedClientboundPlayerInfoUpdatePacket(new HashSet<>(), List.of());

        assertEquals(PacketType.Play.Server.PLAYER_INFO, w.getHandle().getType());

        assertEquals(new HashSet<>(), w.getActions());
        assertEquals(List.of(), w.getEntries());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerInfoUpdatePacket w = new WrappedClientboundPlayerInfoUpdatePacket();

        assertEquals(PacketType.Play.Server.PLAYER_INFO, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerInfoUpdatePacket source = new WrappedClientboundPlayerInfoUpdatePacket(new HashSet<>(), List.of());
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerInfoUpdatePacket wrapper = new WrappedClientboundPlayerInfoUpdatePacket(container);

        assertEquals(new HashSet<>(), wrapper.getActions());
        assertEquals(List.of(), wrapper.getEntries());

        wrapper.setActions(new HashSet<>());
        wrapper.setEntries(List.of());

        assertEquals(new HashSet<>(), wrapper.getActions());
        assertEquals(List.of(), wrapper.getEntries());

        assertEquals(new HashSet<>(), source.getActions());
        assertEquals(List.of(), source.getEntries());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerInfoUpdatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
