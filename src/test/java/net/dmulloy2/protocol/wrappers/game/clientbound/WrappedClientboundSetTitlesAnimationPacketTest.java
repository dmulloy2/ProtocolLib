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
    void testCreate() {
        WrappedClientboundSetTitlesAnimationPacket w = new WrappedClientboundSetTitlesAnimationPacket();
        w.setFadeIn(10);
        w.setStay(70);
        w.setFadeOut(20);

        assertEquals(PacketType.Play.Server.SET_TITLES_ANIMATION, w.getHandle().getType());

        ClientboundSetTitlesAnimationPacket p = (ClientboundSetTitlesAnimationPacket) w.getHandle().getHandle();

        assertEquals(10, p.getFadeIn());
        assertEquals(70, p.getStay());
        assertEquals(20, p.getFadeOut());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetTitlesAnimationPacket nmsPacket = new ClientboundSetTitlesAnimationPacket(5, 40, 10);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitlesAnimationPacket wrapper = new WrappedClientboundSetTitlesAnimationPacket(container);

        assertEquals(5, wrapper.getFadeIn());
        assertEquals(40, wrapper.getStay());
        assertEquals(10, wrapper.getFadeOut());
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetTitlesAnimationPacket nmsPacket = new ClientboundSetTitlesAnimationPacket(5, 40, 10);

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetTitlesAnimationPacket wrapper = new WrappedClientboundSetTitlesAnimationPacket(container);

        wrapper.setStay(100);

        assertEquals(5, wrapper.getFadeIn());
        assertEquals(100, wrapper.getStay());
        assertEquals(10, wrapper.getFadeOut());
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetTitlesAnimationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
