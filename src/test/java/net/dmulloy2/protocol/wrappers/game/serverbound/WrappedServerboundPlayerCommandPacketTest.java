package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundPlayerCommandPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket(3, EnumWrappers.PlayerAction.STOP_SLEEPING, 5);

        assertEquals(PacketType.Play.Client.ENTITY_ACTION, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.STOP_SLEEPING, w.getAction());
        assertEquals(5, w.getData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket();

        assertEquals(PacketType.Play.Client.ENTITY_ACTION, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerCommandPacket source = new WrappedServerboundPlayerCommandPacket(3, EnumWrappers.PlayerAction.STOP_SLEEPING, 5);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerCommandPacket wrapper = new WrappedServerboundPlayerCommandPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.STOP_SLEEPING, wrapper.getAction());
        assertEquals(5, wrapper.getData());

        wrapper.setEntityId(9);
        wrapper.setAction(EnumWrappers.PlayerAction.START_SPRINTING);
        wrapper.setData(0);

        assertEquals(9, wrapper.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.START_SPRINTING, wrapper.getAction());
        assertEquals(0, wrapper.getData());

        assertEquals(9, source.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.START_SPRINTING, source.getAction());
        assertEquals(0, source.getData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerCommandPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
