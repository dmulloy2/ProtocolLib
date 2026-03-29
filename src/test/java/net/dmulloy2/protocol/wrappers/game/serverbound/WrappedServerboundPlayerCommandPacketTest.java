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
    void testCreate() {
        // NMS constructor takes Entity; use wrapper-based approach
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket();
        w.setEntityId(10);
        w.setAction(EnumWrappers.PlayerAction.START_SPRINTING);
        w.setData(0);

        assertEquals(PacketType.Play.Client.ENTITY_ACTION, w.getHandle().getType());
        assertEquals(10, w.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.START_SPRINTING, w.getAction());
        assertEquals(0, w.getData());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedServerboundPlayerCommandPacket src = new WrappedServerboundPlayerCommandPacket();
        src.setEntityId(15);
        src.setAction(EnumWrappers.PlayerAction.STOP_SPRINTING);
        src.setData(0);

        WrappedServerboundPlayerCommandPacket wrapper = new WrappedServerboundPlayerCommandPacket(src.getHandle());

        assertEquals(15, wrapper.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.STOP_SPRINTING, wrapper.getAction());
        assertEquals(0, wrapper.getData());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundPlayerCommandPacket w = new WrappedServerboundPlayerCommandPacket();
        w.setEntityId(5);
        w.setAction(EnumWrappers.PlayerAction.START_RIDING_JUMP);
        w.setData(0);

        w.setAction(EnumWrappers.PlayerAction.STOP_RIDING_JUMP);

        assertEquals(5, w.getEntityId());
        assertEquals(EnumWrappers.PlayerAction.STOP_RIDING_JUMP, w.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundPlayerCommandPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
