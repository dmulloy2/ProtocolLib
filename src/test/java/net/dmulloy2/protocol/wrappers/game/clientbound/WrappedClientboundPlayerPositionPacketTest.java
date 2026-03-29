package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundPlayerPositionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedPositionMoveRotation pmr = new WrappedPositionMoveRotation();
        pmr.setX(1.0); pmr.setY(64.0); pmr.setZ(-3.0);
        pmr.setYaw(90.0f); pmr.setPitch(0.0f);

        Set<EnumWrappers.RelativeArgument> relatives = EnumSet.of(EnumWrappers.RelativeArgument.X);

        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket();
        w.setId(42);
        w.setChange(pmr);
        w.setRelatives(relatives);

        assertEquals(PacketType.Play.Server.POSITION, w.getHandle().getType());

        ClientboundPlayerPositionPacket p = (ClientboundPlayerPositionPacket) w.getHandle().getHandle();

        assertEquals(42, p.id());
        assertNotNull(p.change());
        assertNotNull(p.relatives());
    }

    @Test
    void testReadFromExistingPacket() {
        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket();
        w.setId(7);

        WrappedPositionMoveRotation pmr = new WrappedPositionMoveRotation();
        pmr.setX(10.0); pmr.setY(65.0); pmr.setZ(20.0);
        pmr.setYaw(45.0f); pmr.setPitch(-10.0f);
        w.setChange(pmr);
        w.setRelatives(EnumSet.noneOf(EnumWrappers.RelativeArgument.class));

        PacketContainer container = w.getHandle();
        WrappedClientboundPlayerPositionPacket wrapper = new WrappedClientboundPlayerPositionPacket(container);

        assertEquals(7, wrapper.getId());
        assertNotNull(wrapper.getChange());
        assertNotNull(wrapper.getRelatives());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundPlayerPositionPacket w = new WrappedClientboundPlayerPositionPacket();
        w.setId(1);

        WrappedPositionMoveRotation pmr = new WrappedPositionMoveRotation();
        pmr.setX(0); pmr.setY(64); pmr.setZ(0);
        w.setChange(pmr);
        w.setRelatives(EnumSet.noneOf(EnumWrappers.RelativeArgument.class));

        w.setId(99);

        assertEquals(99, w.getId());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundPlayerPositionPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
