package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateAdvancementsPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundUpdateAdvancementsPacket w = new WrappedClientboundUpdateAdvancementsPacket(true, false);

        assertEquals(PacketType.Play.Server.ADVANCEMENTS, w.getHandle().getType());

        assertTrue(w.isShouldReset());
        assertFalse(w.isShouldShowAdvancements());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateAdvancementsPacket w = new WrappedClientboundUpdateAdvancementsPacket();

        assertEquals(PacketType.Play.Server.ADVANCEMENTS, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundUpdateAdvancementsPacket source = new WrappedClientboundUpdateAdvancementsPacket(true, false);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundUpdateAdvancementsPacket wrapper = new WrappedClientboundUpdateAdvancementsPacket(container);

        assertTrue(wrapper.isShouldReset());
        assertFalse(wrapper.isShouldShowAdvancements());

        wrapper.setShouldReset(false);
        wrapper.setShouldShowAdvancements(true);

        assertFalse(wrapper.isShouldReset());
        assertTrue(wrapper.isShouldShowAdvancements());

        assertFalse(source.isShouldReset());
        assertTrue(source.isShouldShowAdvancements());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateAdvancementsPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
