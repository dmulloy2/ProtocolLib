package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.InteractionHand;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundOpenBookPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundOpenBookPacket w = new WrappedClientboundOpenBookPacket();
        w.setHand(EnumWrappers.Hand.MAIN_HAND);

        assertEquals(PacketType.Play.Server.OPEN_BOOK, w.getHandle().getType());

        ClientboundOpenBookPacket p = (ClientboundOpenBookPacket) w.getHandle().getHandle();

        assertEquals(InteractionHand.MAIN_HAND, p.getHand());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundOpenBookPacket nmsPacket = new ClientboundOpenBookPacket(InteractionHand.OFF_HAND);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenBookPacket wrapper = new WrappedClientboundOpenBookPacket(container);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundOpenBookPacket nmsPacket = new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundOpenBookPacket wrapper = new WrappedClientboundOpenBookPacket(container);

        wrapper.setHand(EnumWrappers.Hand.OFF_HAND);

        assertEquals(EnumWrappers.Hand.OFF_HAND, wrapper.getHand());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundOpenBookPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
