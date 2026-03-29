package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundInteractPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        // NMS constructor is complex; use wrapper-based approach
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket();
        w.setEntityId(42);
        w.setUsingSecondaryAction(false);

        assertEquals(PacketType.Play.Client.USE_ENTITY, w.getHandle().getType());
        assertEquals(42, w.getEntityId());
        assertFalse(w.isUsingSecondaryAction());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedServerboundInteractPacket src = new WrappedServerboundInteractPacket();
        src.setEntityId(77);
        src.setUsingSecondaryAction(true);
        src.setHand(EnumWrappers.Hand.OFF_HAND);

        WrappedServerboundInteractPacket wrapper = new WrappedServerboundInteractPacket(src.getHandle());

        assertEquals(77, wrapper.getEntityId());
        assertTrue(wrapper.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket();
        w.setEntityId(10);
        w.setUsingSecondaryAction(false);
        w.setHand(EnumWrappers.Hand.MAIN_HAND);

        w.setEntityId(20);

        assertEquals(20, w.getEntityId());
        assertFalse(w.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundInteractPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
