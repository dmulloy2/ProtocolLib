package net.dmulloy2.protocol.wrappers.configuration.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundUpdateEnabledFeaturesPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testAllArgsCreate() {
        Set<MinecraftKey> features = Set.of(new MinecraftKey("minecraft", "vanilla"));
        WrappedClientboundUpdateEnabledFeaturesPacket w = new WrappedClientboundUpdateEnabledFeaturesPacket(features);

        assertEquals(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES, w.getHandle().getType());

        assertEquals(features, w.getFeatures());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundUpdateEnabledFeaturesPacket w = new WrappedClientboundUpdateEnabledFeaturesPacket();

        assertEquals(PacketType.Configuration.Server.UPDATE_ENABLED_FEATURES, w.getHandle().getType());
    }

    @Test
    void testModifyExistingPacket() {
        Set<MinecraftKey> features = Set.of(new MinecraftKey("minecraft", "vanilla"));
        WrappedClientboundUpdateEnabledFeaturesPacket source = new WrappedClientboundUpdateEnabledFeaturesPacket(features);
        Object nmsPacket = source.getHandle().getHandle();
        PacketContainer container = new PacketContainer(WrappedClientboundUpdateEnabledFeaturesPacket.TYPE, nmsPacket);
        WrappedClientboundUpdateEnabledFeaturesPacket wrapper = new WrappedClientboundUpdateEnabledFeaturesPacket(container);

        assertEquals(features, wrapper.getFeatures());

        Set<MinecraftKey> updated = Set.of(new MinecraftKey("minecraft", "update_1_21"));
        wrapper.setFeatures(updated);

        assertEquals(updated, wrapper.getFeatures());
        assertEquals(updated, source.getFeatures());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundUpdateEnabledFeaturesPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
