package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetExperiencePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket();
        w.setExperienceProgress(0.75f);
        w.setTotalExperience(350);
        w.setExperienceLevel(12);

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.getExperienceProgress(), 1e-4f);
        assertEquals(350,   p.getTotalExperience());
        assertEquals(12,    p.getExperienceLevel());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(
                0.5f, 100, 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        assertEquals(0.5f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(100, wrapper.getTotalExperience());
        assertEquals(5,   wrapper.getExperienceLevel());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(
                0.5f, 100, 5
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        wrapper.setTotalExperience(200);

        assertEquals(0.5f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(200, wrapper.getTotalExperience());
        assertEquals(5,   wrapper.getExperienceLevel());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetExperiencePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
