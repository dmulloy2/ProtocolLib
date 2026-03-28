package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDistancePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderWarningDistancePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetBorderWarningDistancePacket w = new WrappedClientboundSetBorderWarningDistancePacket();
        w.setWarningDistance(5);

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE, w.getHandle().getType());

        ClientboundSetBorderWarningDistancePacket p = (ClientboundSetBorderWarningDistancePacket) w.getHandle().getHandle();

        assertEquals(5, p.getWarningBlocks());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 20);

        WrappedClientboundSetBorderWarningDistancePacket wrapper = new WrappedClientboundSetBorderWarningDistancePacket(container);

        assertEquals(20, wrapper.getWarningDistance());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DISTANCE);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 20);

        WrappedClientboundSetBorderWarningDistancePacket wrapper = new WrappedClientboundSetBorderWarningDistancePacket(container);
        wrapper.setWarningDistance(50);

        assertEquals(50, wrapper.getWarningDistance());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderWarningDistancePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
