package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundMerchantOffersPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundMerchantOffersPacket w = new WrappedClientboundMerchantOffersPacket(3, List.of(), 5, 3, false, true);

        assertEquals(PacketType.Play.Server.OPEN_WINDOW_MERCHANT, w.getHandle().getType());

        assertEquals(3, w.getContainerId());
        assertEquals(List.of(), w.getOffers());
        assertEquals(5, w.getVillagerLevel());
        assertEquals(3, w.getVillagerXp());
        assertFalse(w.isShowProgress());
        assertTrue(w.isCanRestock());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundMerchantOffersPacket w = new WrappedClientboundMerchantOffersPacket();

        assertEquals(PacketType.Play.Server.OPEN_WINDOW_MERCHANT, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundMerchantOffersPacket source = new WrappedClientboundMerchantOffersPacket(3, List.of(), 5, 3, false, true);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundMerchantOffersPacket wrapper = new WrappedClientboundMerchantOffersPacket(container);

        assertEquals(3, wrapper.getContainerId());
        assertEquals(List.of(), wrapper.getOffers());
        assertEquals(5, wrapper.getVillagerLevel());
        assertEquals(3, wrapper.getVillagerXp());
        assertFalse(wrapper.isShowProgress());
        assertTrue(wrapper.isCanRestock());

        wrapper.setContainerId(9);
        wrapper.setOffers(List.of());
        wrapper.setVillagerLevel(0);
        wrapper.setVillagerXp(42);
        wrapper.setShowProgress(true);
        wrapper.setCanRestock(false);

        assertEquals(9, wrapper.getContainerId());
        assertEquals(List.of(), wrapper.getOffers());
        assertEquals(0, wrapper.getVillagerLevel());
        assertEquals(42, wrapper.getVillagerXp());
        assertTrue(wrapper.isShowProgress());
        assertFalse(wrapper.isCanRestock());

        assertEquals(9, source.getContainerId());
        assertEquals(List.of(), source.getOffers());
        assertEquals(0, source.getVillagerLevel());
        assertEquals(42, source.getVillagerXp());
        assertTrue(source.isShowProgress());
        assertFalse(source.isCanRestock());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundMerchantOffersPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
