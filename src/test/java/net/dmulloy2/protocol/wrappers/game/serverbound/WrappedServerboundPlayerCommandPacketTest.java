package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
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
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket(
                42, EnumWrappers.PlayerAction.OPEN_INVENTORY, 0);

        assertEquals(PacketType.Play.Client.ENTITY_ACTION, w.getHandle().getType());

        assertEquals(42, w.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.OPEN_INVENTORY, w.getAction());
        assertEquals(0, w.getData());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket();

        assertEquals(PacketType.Play.Client.ENTITY_ACTION, w.getHandle().getType());

        assertEquals(0, w.getEntityId());
        assertNotNull(w.getAction());
        assertEquals(0, w.getData());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerCommandPacket src = new WrappedServerboundPlayerCommandPacket(
                42, EnumWrappers.PlayerAction.START_SPRINTING, 0);
        ServerboundPlayerCommandPacket nmsPacket = (ServerboundPlayerCommandPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundPlayerCommandPacket wrapper = new WrappedServerboundPlayerCommandPacket(container);

        assertEquals(42, wrapper.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.START_SPRINTING, wrapper.getAction());
        assertEquals(0, wrapper.getData());

        wrapper.setEntityId(99);
        wrapper.setAction(EnumWrappers.PlayerAction.STOP_SPRINTING);
        wrapper.setData(5);

        assertEquals(99, wrapper.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.STOP_SPRINTING, wrapper.getAction());
        assertEquals(5, wrapper.getData());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
