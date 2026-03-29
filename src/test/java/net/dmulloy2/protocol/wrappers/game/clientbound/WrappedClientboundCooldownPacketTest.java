package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundCooldownPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundCooldownPacket w = new WrappedClientboundCooldownPacket();
        MinecraftKey key = new MinecraftKey("minecraft", "ender_pearl");
        w.setItem(key);
        w.setTicks(20);

        assertEquals(PacketType.Play.Server.SET_COOLDOWN, w.getHandle().getType());

        ClientboundCooldownPacket p = (ClientboundCooldownPacket) w.getHandle().getHandle();

        assertEquals(20, p.duration());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundCooldownPacket nmsPacket = new ClientboundCooldownPacket(
                Identifier.fromNamespaceAndPath("minecraft", "bow"), 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCooldownPacket wrapper = new WrappedClientboundCooldownPacket(container);

        assertEquals(new MinecraftKey("minecraft", "bow"), wrapper.getItem());
        assertEquals(5, wrapper.getTicks());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundCooldownPacket nmsPacket = new ClientboundCooldownPacket(
                Identifier.fromNamespaceAndPath("minecraft", "bow"), 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundCooldownPacket wrapper = new WrappedClientboundCooldownPacket(container);

        wrapper.setTicks(40);

        assertEquals(new MinecraftKey("minecraft", "bow"), wrapper.getItem());
        assertEquals(40, wrapper.getTicks());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundCooldownPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
