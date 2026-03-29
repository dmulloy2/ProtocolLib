package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSwingPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundSwingPacket w = new WrappedServerboundSwingPacket();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);

        assertEquals(PacketType.Play.Client.ARM_ANIMATION, w.getHandle().getType());

        ServerboundSwingPacket p = (ServerboundSwingPacket) w.getHandle().getHandle();

        assertEquals(InteractionHand.MAIN_HAND, p.getHand());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundSwingPacket nmsPacket = new ServerboundSwingPacket(InteractionHand.OFF_HAND);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSwingPacket wrapper = new WrappedServerboundSwingPacket(container);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSwingPacket nmsPacket = new ServerboundSwingPacket(InteractionHand.MAIN_HAND);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSwingPacket wrapper = new WrappedServerboundSwingPacket(container);

        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSwingPacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
