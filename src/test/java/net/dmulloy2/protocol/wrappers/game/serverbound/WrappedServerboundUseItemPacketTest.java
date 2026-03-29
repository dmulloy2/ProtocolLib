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
    void testCreate() {
        WrappedServerboundUseItemPacket w = new WrappedServerboundUseItemPacket();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);
        w.setSequence(2);
        w.setYRot(45.0f);
        w.setXRot(-10.0f);

        assertEquals(PacketType.Play.Client.USE_ITEM, w.getHandle().getType());

        ServerboundUseItemPacket p = (ServerboundUseItemPacket) w.getHandle().getHandle();

        assertEquals(InteractionHand.MAIN_HAND, p.getHand());
        assertEquals(2, p.getSequence());
        assertEquals(45.0f, p.getYRot(), 1e-4f);
        assertEquals(-10.0f, p.getXRot(), 1e-4f);
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundUseItemPacket nmsPacket = new ServerboundUseItemPacket(
                InteractionHand.OFF_HAND, 3, 90.0f, 5.0f
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemPacket wrapper = new WrappedServerboundUseItemPacket(container);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertEquals(3, wrapper.getSequence());
        assertEquals(90.0f, wrapper.getYRot(), 1e-4f);
        assertEquals(5.0f, wrapper.getXRot(), 1e-4f);
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundUseItemPacket nmsPacket = new ServerboundUseItemPacket(
                InteractionHand.MAIN_HAND, 1, 0.0f, 0.0f
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemPacket wrapper = new WrappedServerboundUseItemPacket(container);

        wrapper.setYRot(180.0f);

        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());
        assertEquals(180.0f, wrapper.getYRot(), 1e-4f);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
