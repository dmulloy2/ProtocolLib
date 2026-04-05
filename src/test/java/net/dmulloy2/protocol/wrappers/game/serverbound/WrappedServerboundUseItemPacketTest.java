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
        WrappedServerboundUseItemPacket w = new WrappedServerboundUseItemPacket(EnumWrappers.Hand.OFF_HAND, 7, -3.0f, 0.75f);

        assertEquals(PacketType.Play.Client.USE_ITEM, w.getHandle().getType());

        ServerboundUseItemPacket p = (ServerboundUseItemPacket) w.getHandle().getHandle();

        assertEquals(InteractionHand.OFF_HAND, p.getHand());
        assertEquals(7, p.getSequence());
        assertEquals(-3.0f, p.getYRot(), 1e-4f);
        assertEquals(0.75f, p.getXRot(), 1e-4f);
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundUseItemPacket w = new WrappedServerboundUseItemPacket();

        assertEquals(PacketType.Play.Client.USE_ITEM, w.getHandle().getType());

        ServerboundUseItemPacket p = (ServerboundUseItemPacket) w.getHandle().getHandle();

        assertEquals(0, p.getSequence());
        assertEquals(0.0f, p.getYRot(), 1e-4f);
        assertEquals(0.0f, p.getXRot(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundUseItemPacket nmsPacket = new ServerboundUseItemPacket(InteractionHand.OFF_HAND, 7, -3.0f, 0.75f);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemPacket wrapper = new WrappedServerboundUseItemPacket(container);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertEquals(7, wrapper.getSequence());
        assertEquals(-3.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(0.75f, wrapper.getXRot(), 1e-4f);

        wrapper.setHand(EnumWrappers.Hand.MAIN_HAND);
        wrapper.setSequence(-5);
        wrapper.setYRot(1.0f);
        wrapper.setXRot(10.5f);

        assertEquals(InteractionHand.MAIN_HAND, nmsPacket.getHand());
        assertEquals(-5, nmsPacket.getSequence());
        assertEquals(1.0f, nmsPacket.getYRot(), 1e-4f);
        assertEquals(10.5f, nmsPacket.getXRot(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
