package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundInteractPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket(77, true, EnumWrappers.Hand.MAIN_HAND);

        assertEquals(PacketType.Play.Client.USE_ENTITY, w.getHandle().getType());

        assertEquals(77, w.getEntityId());
        assertTrue(w.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket();

        assertEquals(PacketType.Play.Client.USE_ENTITY, w.getHandle().getType());

        assertEquals(0, w.getEntityId());
        assertFalse(w.isUsingSecondaryAction());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundInteractPacket src = new WrappedServerboundInteractPacket(77, true, EnumWrappers.Hand.MAIN_HAND);
        ServerboundInteractPacket nmsPacket = (ServerboundInteractPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundInteractPacket wrapper = new WrappedServerboundInteractPacket(container);

        assertEquals(77, wrapper.getEntityId());
        assertTrue(wrapper.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());

        wrapper.setEntityId(100);
        wrapper.setUsingSecondaryAction(false);
        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);

        assertEquals(100, wrapper.getEntityId());
        assertFalse(wrapper.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundInteractPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
