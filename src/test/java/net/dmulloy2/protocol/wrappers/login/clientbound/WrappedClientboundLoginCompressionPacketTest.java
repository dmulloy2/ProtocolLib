package net.dmulloy2.protocol.wrappers.login.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.login.ClientboundLoginCompressionPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundLoginCompressionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundLoginCompressionPacket w = new WrappedClientboundLoginCompressionPacket(3);

        assertEquals(PacketType.Login.Server.SET_COMPRESSION, w.getHandle().getType());

        ClientboundLoginCompressionPacket p = (ClientboundLoginCompressionPacket) w.getHandle().getHandle();

        assertEquals(3, p.getCompressionThreshold());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundLoginCompressionPacket w = new WrappedClientboundLoginCompressionPacket();

        assertEquals(PacketType.Login.Server.SET_COMPRESSION, w.getHandle().getType());

        ClientboundLoginCompressionPacket p = (ClientboundLoginCompressionPacket) w.getHandle().getHandle();

        assertEquals(0, p.getCompressionThreshold());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundLoginCompressionPacket nmsPacket = new ClientboundLoginCompressionPacket(3);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundLoginCompressionPacket wrapper = new WrappedClientboundLoginCompressionPacket(container);

        assertEquals(3, wrapper.getCompressionThreshold());

        wrapper.setCompressionThreshold(9);

        assertEquals(9, nmsPacket.getCompressionThreshold());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundLoginCompressionPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
