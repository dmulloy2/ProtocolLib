package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundTickingStatePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundTickingStatePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket(0.75f, false);

        assertEquals(PacketType.Play.Server.TICKING_STATE, w.getHandle().getType());

        ClientboundTickingStatePacket p = (ClientboundTickingStatePacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.tickRate(), 1e-4f);
        assertFalse(p.isFrozen());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket();

        assertEquals(PacketType.Play.Server.TICKING_STATE, w.getHandle().getType());

        ClientboundTickingStatePacket p = (ClientboundTickingStatePacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.tickRate(), 1e-4f);
        assertFalse(p.isFrozen());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTickingStatePacket nmsPacket = new ClientboundTickingStatePacket(0.75f, false);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStatePacket wrapper = new WrappedClientboundTickingStatePacket(container);

        assertEquals(0.75f, wrapper.getTickRate(), 1e-4f);
        assertFalse(wrapper.isFrozen());

        wrapper.setTickRate(0.25f);
        wrapper.setFrozen(true);

        assertEquals(0.25f, nmsPacket.tickRate(), 1e-4f);
        assertTrue(nmsPacket.isFrozen());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStatePacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
