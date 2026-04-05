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
    void testAllArgsCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket(0.75f, 7, 5);

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.75f, p.getExperienceProgress(), 1e-4f);
        assertEquals(7, p.getTotalExperience());
        assertEquals(5, p.getExperienceLevel());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetExperiencePacket w = new WrappedClientboundSetExperiencePacket();

        assertEquals(PacketType.Play.Server.EXPERIENCE, w.getHandle().getType());

        ClientboundSetExperiencePacket p = (ClientboundSetExperiencePacket) w.getHandle().getHandle();

        assertEquals(0.0f, p.getExperienceProgress(), 1e-4f);
        assertEquals(0, p.getTotalExperience());
        assertEquals(0, p.getExperienceLevel());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetExperiencePacket nmsPacket = new ClientboundSetExperiencePacket(0.75f, 7, 5);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetExperiencePacket wrapper = new WrappedClientboundSetExperiencePacket(container);

        assertEquals(0.75f, wrapper.getExperienceProgress(), 1e-4f);
        assertEquals(7, wrapper.getTotalExperience());
        assertEquals(5, wrapper.getExperienceLevel());

        wrapper.setExperienceProgress(0.25f);
        wrapper.setTotalExperience(-5);
        wrapper.setExperienceLevel(0);

        assertEquals(0.25f, nmsPacket.getExperienceProgress(), 1e-4f);
        assertEquals(-5, nmsPacket.getTotalExperience());
        assertEquals(0, nmsPacket.getExperienceLevel());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetExperiencePacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
