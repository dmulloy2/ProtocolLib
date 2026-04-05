package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerInfoRemovePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundPlayerInfoRemovePacket w = new WrappedClientboundPlayerInfoRemovePacket(List.of(UUID.fromString("12345678-1234-1234-1234-123456789abc")));

        assertEquals(PacketType.Play.Server.PLAYER_INFO_REMOVE, w.getHandle().getType());

        assertEquals(List.of(UUID.fromString("12345678-1234-1234-1234-123456789abc")), w.getProfileIds());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundPlayerInfoRemovePacket w = new WrappedClientboundPlayerInfoRemovePacket();

        assertEquals(PacketType.Play.Server.PLAYER_INFO_REMOVE, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerInfoRemovePacket source = new WrappedClientboundPlayerInfoRemovePacket(List.of(UUID.fromString("12345678-1234-1234-1234-123456789abc")));
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundPlayerInfoRemovePacket wrapper = new WrappedClientboundPlayerInfoRemovePacket(container);

        assertEquals(List.of(UUID.fromString("12345678-1234-1234-1234-123456789abc")), wrapper.getProfileIds());

        wrapper.setProfileIds(List.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")));

        assertEquals(List.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")), wrapper.getProfileIds());

        assertEquals(List.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee")), source.getProfileIds());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerInfoRemovePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
