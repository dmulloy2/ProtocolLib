package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundClientInformationPacket.MainHand;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundClientInformationPacket.ParticleStatus;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundClientInformationPacket.WrappedClientInformation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundClientInformationPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundClientInformationPacket w = new WrappedServerboundClientInformationPacket();
        assertEquals(PacketType.Play.Client.SETTINGS, w.getHandle().getType());
    }

    @Test
    void testSetAndGetInformation() {
        WrappedServerboundClientInformationPacket w = new WrappedServerboundClientInformationPacket();

        WrappedClientInformation info = new WrappedClientInformation();
        info.language = "de_de";
        info.viewDistance = 12;
        info.chatVisibility = EnumWrappers.ChatVisibility.SYSTEM;
        info.chatColors = false;
        info.modelCustomisation = 63;
        info.mainHand = MainHand.LEFT;
        info.textFilteringEnabled = true;
        info.allowsListing = false;
        info.particleStatus = ParticleStatus.MINIMAL;

        w.setInformation(info);

        WrappedClientInformation result = w.getInformation();
        assertEquals("de_de", result.language);
        assertEquals(12, result.viewDistance);
        assertEquals(EnumWrappers.ChatVisibility.SYSTEM, result.chatVisibility);
        assertFalse(result.chatColors);
        assertEquals(63, result.modelCustomisation);
        assertEquals(MainHand.LEFT, result.mainHand);
        assertTrue(result.textFilteringEnabled);
        assertFalse(result.allowsListing);
        assertEquals(ParticleStatus.MINIMAL, result.particleStatus);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientInformationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
