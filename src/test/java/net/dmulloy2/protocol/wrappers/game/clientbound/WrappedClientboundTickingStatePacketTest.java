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
    void testCreate() {
        WrappedClientboundTickingStatePacket w = new WrappedClientboundTickingStatePacket();
        w.setTickRate(20.0f);
        w.setFrozen(true);

        assertEquals(PacketType.Play.Server.TICKING_STATE, w.getHandle().getType());

        ClientboundTickingStatePacket p = (ClientboundTickingStatePacket) w.getHandle().getHandle();

        assertEquals(20.0f, p.tickRate(), 1e-4f);
        assertTrue(p.isFrozen());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundTickingStatePacket nmsPacket = new ClientboundTickingStatePacket(4.0f, false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStatePacket wrapper = new WrappedClientboundTickingStatePacket(container);

        assertEquals(4.0f, wrapper.getTickRate(), 1e-4f);
        assertFalse(wrapper.isFrozen());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundTickingStatePacket nmsPacket = new ClientboundTickingStatePacket(20.0f, false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundTickingStatePacket wrapper = new WrappedClientboundTickingStatePacket(container);

        wrapper.setFrozen(true);

        assertEquals(20.0f, wrapper.getTickRate(), 1e-4f);
        assertTrue(wrapper.isFrozen());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundTickingStatePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
