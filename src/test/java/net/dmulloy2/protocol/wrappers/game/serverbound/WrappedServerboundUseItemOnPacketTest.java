package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundUseItemOnPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket(EnumWrappers.Hand.MAIN_HAND, 5);

        assertEquals(PacketType.Play.Client.USE_ITEM_ON, w.getHandle().getType());

        assertEquals(EnumWrappers.Hand.MAIN_HAND, w.getHand());
        assertEquals(5, w.getSequence());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundUseItemOnPacket w = new WrappedServerboundUseItemOnPacket();

        assertEquals(PacketType.Play.Client.USE_ITEM_ON, w.getHandle().getType());

        assertNotNull(w.getHand());
        assertEquals(0, w.getSequence());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundUseItemOnPacket src = new WrappedServerboundUseItemOnPacket(EnumWrappers.Hand.MAIN_HAND, 5);
        ServerboundUseItemOnPacket nmsPacket = (ServerboundUseItemOnPacket) src.getHandle().getHandle();

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundUseItemOnPacket wrapper = new WrappedServerboundUseItemOnPacket(container);

        assertEquals(EnumWrappers.Hand.MAIN_HAND, wrapper.getHand());
        assertEquals(5, wrapper.getSequence());

        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);
        wrapper.setSequence(12);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
        assertEquals(12, wrapper.getSequence());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundUseItemOnPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
