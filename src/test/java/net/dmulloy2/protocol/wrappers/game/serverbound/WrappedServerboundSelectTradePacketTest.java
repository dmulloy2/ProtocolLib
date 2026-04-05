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
    void testAllArgsCreate() {
        WrappedServerboundSelectTradePacket w = new WrappedServerboundSelectTradePacket(3);

        assertEquals(PacketType.Play.Client.TR_SEL, w.getHandle().getType());

        ServerboundSelectTradePacket p = (ServerboundSelectTradePacket) w.getHandle().getHandle();

        assertEquals(3, p.getItem());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundSelectTradePacket w = new WrappedServerboundSelectTradePacket();

        assertEquals(PacketType.Play.Client.TR_SEL, w.getHandle().getType());

        ServerboundSelectTradePacket p = (ServerboundSelectTradePacket) w.getHandle().getHandle();

        assertEquals(0, p.getItem());
    }

    @Test
    void testModifyExistingPacket() {
        ServerboundSelectTradePacket nmsPacket = new ServerboundSelectTradePacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedServerboundSelectTradePacket wrapper = new WrappedServerboundSelectTradePacket(container);

        assertEquals(3, wrapper.getItem());

        wrapper.setItem(9);

        assertEquals(9, nmsPacket.getItem());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundSelectTradePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
