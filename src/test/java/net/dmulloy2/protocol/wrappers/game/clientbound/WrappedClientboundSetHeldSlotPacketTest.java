package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetHeldSlotPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket();
        w.setSlot(4);

        assertEquals(PacketType.Play.Server.HELD_ITEM_SLOT, w.getHandle().getType());

        ClientboundSetHeldSlotPacket p = (ClientboundSetHeldSlotPacket) w.getHandle().getHandle();

        assertEquals(4, p.slot());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetHeldSlotPacket nmsPacket = new ClientboundSetHeldSlotPacket(7);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHeldSlotPacket wrapper = new WrappedClientboundSetHeldSlotPacket(container);

        assertEquals(7, wrapper.getSlot());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetHeldSlotPacket nmsPacket = new ClientboundSetHeldSlotPacket(3);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHeldSlotPacket wrapper = new WrappedClientboundSetHeldSlotPacket(container);

        wrapper.setSlot(8);

        assertEquals(8, wrapper.getSlot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetHeldSlotPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
