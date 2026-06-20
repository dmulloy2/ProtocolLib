package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedMinecartStep;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMoveMinecartPacketTest {

    @BeforeAll
    static void beforeAll() { BukkitInitialization.initializeAll(); }

    @Test
    void testAllArgsCreate() {
        List<WrappedMinecartStep> steps = List.of(
                new WrappedMinecartStep(new Vector(1, 2, 3), new Vector(0.1, 0.2, 0.3), 45.0F, 10.0F, 1.0F),
                new WrappedMinecartStep(new Vector(4, 5, 6), new Vector(0.4, 0.5, 0.6), 90.0F, 20.0F, 0.5F));

        WrappedClientboundMoveMinecartPacket wrapper = new WrappedClientboundMoveMinecartPacket(99, steps);

        assertEquals(PacketType.Play.Server.MOVE_MINECART, wrapper.getHandle().getType());
        assertEquals(99, wrapper.getEntityId());
        assertEquals(steps, wrapper.getLerpSteps());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMoveMinecartPacket w = new WrappedClientboundMoveMinecartPacket();
        assertEquals(PacketType.Play.Server.MOVE_MINECART, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new WrappedClientboundMoveMinecartPacket(99, List.of()).getHandle();
        WrappedClientboundMoveMinecartPacket wrapper = new WrappedClientboundMoveMinecartPacket(container);
        List<WrappedMinecartStep> steps = List.of(
                new WrappedMinecartStep(new Vector(7, 8, 9), new Vector(0.7, 0.8, 0.9), 180.0F, 30.0F, 0.25F));

        wrapper.setEntityId(99);
        wrapper.setLerpSteps(steps);

        assertEquals(99, wrapper.getEntityId());
        assertEquals(steps, wrapper.getLerpSteps());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMoveMinecartPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
