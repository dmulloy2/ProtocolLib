package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundInitializeBorderPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundInitializeBorderPacket w = new WrappedClientboundInitializeBorderPacket();
        w.setNewCenterX(100.0);
        w.setNewCenterZ(-200.0);
        w.setOldSize(60000.0);
        w.setNewSize(30000.0);
        w.setLerpTime(5000L);
        w.setNewAbsoluteMaxSize(29999984);
        w.setWarningBlocks(5);
        w.setWarningTime(15);

        assertEquals(PacketType.Play.Server.INITIALIZE_BORDER, w.getHandle().getType());

        ClientboundInitializeBorderPacket p = (ClientboundInitializeBorderPacket) w.getHandle().getHandle();

        assertEquals(100.0,      p.getNewCenterX(),   1e-9);
        assertEquals(-200.0,     p.getNewCenterZ(),   1e-9);
        assertEquals(60000.0,    p.getOldSize(),      1e-9);
        assertEquals(30000.0,    p.getNewSize(),      1e-9);
        assertEquals(5000L,      p.getLerpTime());
        assertEquals(29999984,   p.getNewAbsoluteMaxSize());
        assertEquals(5,          p.getWarningBlocks());
        assertEquals(15,         p.getWarningTime());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedClientboundInitializeBorderPacket src = new WrappedClientboundInitializeBorderPacket();
        src.setNewCenterX(50.0);
        src.setNewCenterZ(75.0);
        src.setOldSize(1000.0);
        src.setNewSize(500.0);
        src.setLerpTime(200L);
        src.setNewAbsoluteMaxSize(29999984);
        src.setWarningBlocks(3);
        src.setWarningTime(10);

        WrappedClientboundInitializeBorderPacket wrapper =
                new WrappedClientboundInitializeBorderPacket(src.getHandle());

        assertEquals(50.0,  wrapper.getNewCenterX(),  1e-9);
        assertEquals(75.0,  wrapper.getNewCenterZ(),  1e-9);
        assertEquals(1000.0, wrapper.getOldSize(),    1e-9);
        assertEquals(500.0,  wrapper.getNewSize(),    1e-9);
        assertEquals(200L,   wrapper.getLerpTime());
        assertEquals(29999984, wrapper.getNewAbsoluteMaxSize());
        assertEquals(3,      wrapper.getWarningBlocks());
        assertEquals(10,     wrapper.getWarningTime());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundInitializeBorderPacket w = new WrappedClientboundInitializeBorderPacket();
        w.setNewCenterX(0.0);
        w.setNewCenterZ(0.0);
        w.setOldSize(1000.0);
        w.setNewSize(1000.0);
        w.setLerpTime(0L);
        w.setNewAbsoluteMaxSize(29999984);
        w.setWarningBlocks(0);
        w.setWarningTime(0);

        w.setNewSize(500.0);
        w.setLerpTime(1000L);

        assertEquals(500.0,  w.getNewSize(),  1e-9);
        assertEquals(1000L,  w.getLerpTime());
        assertEquals(1000.0, w.getOldSize(),  1e-9);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundInitializeBorderPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
