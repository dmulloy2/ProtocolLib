package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.world.InteractionHand;
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
    void testAllArgsCreate() {
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket(3, EnumWrappers.Hand.MAIN_HAND, new Vector(7.0, 8.0, 9.0), true);

        assertEquals(PacketType.Play.Client.USE_ENTITY, w.getHandle().getType());

        assertEquals(3, w.getEntityId());
        assertTrue(w.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
        assertEquals(new Vector(7.0, 8.0, 9.0), w.getLocation());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundInteractPacket w = new WrappedServerboundInteractPacket();

        assertEquals(PacketType.Play.Client.USE_ENTITY, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundInteractPacket source = new WrappedServerboundInteractPacket(3, EnumWrappers.Hand.MAIN_HAND, new Vector(7.0, 8.0, 9.0), true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundInteractPacket wrapper = new WrappedServerboundInteractPacket(container);

        assertEquals(3, wrapper.getEntityId());
        assertTrue(wrapper.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());
        assertEquals(new Vector(7.0, 8.0, 9.0), wrapper.getLocation());

        wrapper.setEntityId(9);
        wrapper.setUsingSecondaryAction(false);
        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);
        wrapper.setLocation(new Vector(10.0, 20.0, 30.0));

        assertEquals(9, wrapper.getEntityId());
        assertFalse(wrapper.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertEquals(new Vector(10.0, 20.0, 30.0), wrapper.getLocation());

        assertEquals(9, source.getEntityId());
        assertFalse(source.isUsingSecondaryAction());
        assertEquals(EnumWrappers.Hand.OFF_HAND, source.getHand());
        assertEquals(new Vector(10.0, 20.0, 30.0), source.getLocation());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundInteractPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
