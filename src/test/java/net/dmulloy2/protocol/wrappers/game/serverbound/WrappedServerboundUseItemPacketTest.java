package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundUseItemPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundUseItemPacket w = new WrappedServerboundUseItemPacket(
                EnumWrappers.Hand.MAIN_HAND, 3, 45.0f, -10.0f);

        assertEquals(PacketType.Play.Client.USE_ITEM, w.getHandle().getType());

        ServerboundUseItemPacket p = (ServerboundUseItemPacket) w.getHandle().getHandle();

        assertEquals(InteractionHand.MAIN_HAND, p.getHand());
        assertEquals(3, p.getSequence());
        assertEquals(45.0f, p.getYRot(), 1e-4f);
        assertEquals(-10.0f, p.getXRot(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundUseItemPacket w = new WrappedServerboundUseItemPacket();

        assertEquals(PacketType.Play.Client.USE_ITEM, w.getHandle().getType());

        ServerboundUseItemPacket p = (ServerboundUseItemPacket) w.getHandle().getHandle();

        assertNotNull(p.getHand());
        assertEquals(0, p.getSequence());
        assertEquals(0.0f, p.getYRot(), 1e-4f);
        assertEquals(0.0f, p.getXRot(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundUseItemPacket nmsPacket = new ServerboundUseItemPacket(
                InteractionHand.MAIN_HAND, 3, 45.0f, -10.0f);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemPacket wrapper = new WrappedServerboundUseItemPacket(container);

        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());
        assertEquals(3, wrapper.getSequence());
        assertEquals(45.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(-10.0f, wrapper.getXRot(), 1e-4f);

        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);
        wrapper.setSequence(7);
        wrapper.setYRot(90.0f);
        wrapper.setXRot(5.0f);

        assertEquals(InteractionHand.OFF_HAND, nmsPacket.getHand());
        assertEquals(7, nmsPacket.getSequence());
        assertEquals(90.0f, nmsPacket.getYRot(), 1e-4f);
        assertEquals(5.0f, nmsPacket.getXRot(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
