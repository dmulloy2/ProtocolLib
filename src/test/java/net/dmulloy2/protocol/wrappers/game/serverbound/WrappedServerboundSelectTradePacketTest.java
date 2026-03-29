package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundSelectTradePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedServerboundSelectTradePacket w = new WrappedServerboundSelectTradePacket();
        w.setItem(2);

        assertEquals(PacketType.Play.Client.TR_SEL, w.getHandle().getType());

        ServerboundSelectTradePacket p = (ServerboundSelectTradePacket) w.getHandle().getHandle();

        assertEquals(2, p.getItem());
    }

    @Test
    void testReadFromExistingPacket() {
        ServerboundSelectTradePacket nmsPacket = new ServerboundSelectTradePacket(4);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSelectTradePacket wrapper = new WrappedServerboundSelectTradePacket(container);

        assertEquals(4, wrapper.getItem());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSelectTradePacket nmsPacket = new ServerboundSelectTradePacket(4);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSelectTradePacket wrapper = new WrappedServerboundSelectTradePacket(container);

        wrapper.setItem(0);

        assertEquals(0, wrapper.getItem());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSelectTradePacket(
                        new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
