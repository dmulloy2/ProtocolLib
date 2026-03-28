package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundClearTitlesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundClearTitlesPacket w = new WrappedClientboundClearTitlesPacket();
        w.setResetTimes(true);

        assertEquals(PacketType.Play.Server.CLEAR_TITLES, w.getHandle().getType());

        ClientboundClearTitlesPacket p = (ClientboundClearTitlesPacket) w.getHandle().getHandle();

        assertTrue(p.shouldResetTimes());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundClearTitlesPacket nmsPacket = new ClientboundClearTitlesPacket(false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundClearTitlesPacket wrapper = new WrappedClientboundClearTitlesPacket(container);

        assertFalse(wrapper.isResetTimes());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundClearTitlesPacket nmsPacket = new ClientboundClearTitlesPacket(false);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundClearTitlesPacket wrapper = new WrappedClientboundClearTitlesPacket(container);

        wrapper.setResetTimes(true);

        assertTrue(wrapper.isResetTimes());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundClearTitlesPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
