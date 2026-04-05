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
    void testAllArgsCreate() {
        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket(3);

        assertEquals(PacketType.Play.Server.HELD_ITEM_SLOT, w.getHandle().getType());

        ClientboundSetHeldSlotPacket p = (ClientboundSetHeldSlotPacket) w.getHandle().getHandle();

        assertEquals(3, p.slot());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetHeldSlotPacket w = new WrappedClientboundSetHeldSlotPacket();

        assertEquals(PacketType.Play.Server.HELD_ITEM_SLOT, w.getHandle().getType());

        ClientboundSetHeldSlotPacket p = (ClientboundSetHeldSlotPacket) w.getHandle().getHandle();

        assertEquals(0, p.slot());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetHeldSlotPacket nmsPacket = new ClientboundSetHeldSlotPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetHeldSlotPacket wrapper = new WrappedClientboundSetHeldSlotPacket(container);

        assertEquals(3, wrapper.getSlot());

        wrapper.setSlot(9);

        assertEquals(9, nmsPacket.slot());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetHeldSlotPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
