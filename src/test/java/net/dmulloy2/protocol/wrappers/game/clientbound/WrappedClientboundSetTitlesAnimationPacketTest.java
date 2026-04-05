package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetTitlesAnimationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }



    @Test
    void testAllArgsCreate() {
        WrappedClientboundSetTitlesAnimationPacket w = new WrappedClientboundSetTitlesAnimationPacket(3, 7, 5);

        assertEquals(PacketType.Play.Server.SET_TITLES_ANIMATION, w.getHandle().getType());

        ClientboundSetTitlesAnimationPacket p = (ClientboundSetTitlesAnimationPacket) w.getHandle().getHandle();

        assertEquals(3, p.getFadeIn());
        assertEquals(7, p.getStay());
        assertEquals(5, p.getFadeOut());
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundSetTitlesAnimationPacket w = new WrappedClientboundSetTitlesAnimationPacket();

        assertEquals(PacketType.Play.Server.SET_TITLES_ANIMATION, w.getHandle().getType());

        ClientboundSetTitlesAnimationPacket p = (ClientboundSetTitlesAnimationPacket) w.getHandle().getHandle();

        assertEquals(0, p.getFadeIn());
        assertEquals(0, p.getStay());
        assertEquals(0, p.getFadeOut());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetTitlesAnimationPacket nmsPacket = new ClientboundSetTitlesAnimationPacket(3, 7, 5);
        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitlesAnimationPacket wrapper = new WrappedClientboundSetTitlesAnimationPacket(container);

        assertEquals(3, wrapper.getFadeIn());
        assertEquals(7, wrapper.getStay());
        assertEquals(5, wrapper.getFadeOut());

        wrapper.setFadeIn(9);
        wrapper.setStay(-5);
        wrapper.setFadeOut(0);

        assertEquals(9, nmsPacket.getFadeIn());
        assertEquals(-5, nmsPacket.getStay());
        assertEquals(0, nmsPacket.getFadeOut());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTitlesAnimationPacket(
                        new PacketContainer(PacketType.Play.Server.EXPERIENCE)));
    }
}
