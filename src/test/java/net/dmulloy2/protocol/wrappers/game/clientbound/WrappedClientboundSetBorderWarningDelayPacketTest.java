package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetBorderWarningDelayPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetBorderWarningDelayPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetBorderWarningDelayPacket w = new WrappedClientboundSetBorderWarningDelayPacket();
        w.setWarningDelay(15);

        assertEquals(PacketType.Play.Server.SET_BORDER_WARNING_DELAY, w.getHandle().getType());

        ClientboundSetBorderWarningDelayPacket p = (ClientboundSetBorderWarningDelayPacket) w.getHandle().getHandle();

        assertEquals(15, p.getWarningDelay());
    }

    @Test
    void testReadFromExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DELAY);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 30);

        WrappedClientboundSetBorderWarningDelayPacket wrapper = new WrappedClientboundSetBorderWarningDelayPacket(container);

        assertEquals(30, wrapper.getWarningDelay());
    }

    @Test
    void testModifyExistingPacket() {
        PacketContainer container = new PacketContainer(PacketType.Play.Server.SET_BORDER_WARNING_DELAY);
        container.getModifier().writeDefaults();
        container.getIntegers().write(0, 30);

        WrappedClientboundSetBorderWarningDelayPacket wrapper = new WrappedClientboundSetBorderWarningDelayPacket(container);
        wrapper.setWarningDelay(60);

        assertEquals(60, wrapper.getWarningDelay());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetBorderWarningDelayPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
