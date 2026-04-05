package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundResourcePackPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedServerboundResourcePackPacket w = new WrappedServerboundResourcePackPacket(UUID.fromString("12345678-1234-1234-1234-123456789abc"), EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED);

        assertEquals(PacketType.Configuration.Client.RESOURCE_PACK_ACK, w.getHandle().getType());

        assertEquals(UUID.fromString("12345678-1234-1234-1234-123456789abc"), w.getId());
        assertEquals(EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED, w.getAction());
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundResourcePackPacket w = new WrappedServerboundResourcePackPacket();

        assertEquals(PacketType.Configuration.Client.RESOURCE_PACK_ACK, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedServerboundResourcePackPacket source = new WrappedServerboundResourcePackPacket(UUID.fromString("12345678-1234-1234-1234-123456789abc"), EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedServerboundResourcePackPacket.TYPE, nmsPacket);
        WrappedServerboundResourcePackPacket wrapper = new WrappedServerboundResourcePackPacket(container);

        assertEquals(UUID.fromString("12345678-1234-1234-1234-123456789abc"), wrapper.getId());
        assertEquals(EnumWrappers.ResourcePackStatus.SUCCESSFULLY_LOADED, wrapper.getAction());

        wrapper.setId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"));
        wrapper.setAction(EnumWrappers.ResourcePackStatus.DECLINED);

        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), wrapper.getId());
        assertEquals(EnumWrappers.ResourcePackStatus.DECLINED, wrapper.getAction());

        assertEquals(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"), source.getId());
        assertEquals(EnumWrappers.ResourcePackStatus.DECLINED, source.getAction());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundResourcePackPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
