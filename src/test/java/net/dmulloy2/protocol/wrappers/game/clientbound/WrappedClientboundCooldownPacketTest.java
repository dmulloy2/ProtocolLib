package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCooldownPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundCooldownPacket w = new WrappedClientboundCooldownPacket(new MinecraftKey("minecraft", "stone"), 7);

        assertEquals(PacketType.Play.Server.SET_COOLDOWN, w.getHandle().getType());

        assertEquals(new MinecraftKey("minecraft", "stone"), w.getItem());
        assertEquals(7, w.getTicks());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundCooldownPacket w = new WrappedClientboundCooldownPacket();

        assertEquals(PacketType.Play.Server.SET_COOLDOWN, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        WrappedClientboundCooldownPacket source = new WrappedClientboundCooldownPacket(new MinecraftKey("minecraft", "stone"), 7);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCooldownPacket wrapper = new WrappedClientboundCooldownPacket(container);

        assertEquals(new MinecraftKey("minecraft", "stone"), wrapper.getItem());
        assertEquals(7, wrapper.getTicks());

        wrapper.setItem(new MinecraftKey("minecraft", "sand"));
        wrapper.setTicks(-5);

        assertEquals(new MinecraftKey("minecraft", "sand"), wrapper.getItem());
        assertEquals(-5, wrapper.getTicks());

        assertEquals(new MinecraftKey("minecraft", "sand"), source.getItem());
        assertEquals(-5, source.getTicks());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCooldownPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
