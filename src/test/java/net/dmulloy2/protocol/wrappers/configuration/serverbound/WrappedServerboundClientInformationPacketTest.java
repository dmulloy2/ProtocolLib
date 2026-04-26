package net.dmulloy2.protocol.wrappers.configuration.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import net.dmulloy2.protocol.wrappers.configuration.serverbound.WrappedServerboundClientInformationPacket.MainHand;
import net.dmulloy2.protocol.wrappers.configuration.serverbound.WrappedServerboundClientInformationPacket.ParticleStatus;
import net.dmulloy2.protocol.wrappers.configuration.serverbound.WrappedServerboundClientInformationPacket.WrappedClientInformation;
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
        assertEquals(PacketType.Configuration.Client.CLIENT_INFORMATION, w.getHandle().getType());
    }

    @Test
    void testSetAndGetInformation() {
        WrappedServerboundClientInformationPacket w = new WrappedServerboundClientInformationPacket();

        WrappedClientInformation info = new WrappedClientInformation();
        info.language = "en_us";
        info.viewDistance = 8;
        info.chatVisibility = EnumWrappers.ChatVisibility.FULL;
        info.chatColors = true;
        info.modelCustomisation = 127;
        info.mainHand = MainHand.RIGHT;
        info.textFilteringEnabled = false;
        info.allowsListing = true;
        info.particleStatus = ParticleStatus.ALL;

        w.setInformation(info);

        WrappedClientInformation result = w.getInformation();
        assertEquals("en_us", result.language);
        assertEquals(8, result.viewDistance);
        assertEquals(EnumWrappers.ChatVisibility.FULL, result.chatVisibility);
        assertTrue(result.chatColors);
        assertEquals(127, result.modelCustomisation);
        assertEquals(MainHand.RIGHT, result.mainHand);
        assertFalse(result.textFilteringEnabled);
        assertTrue(result.allowsListing);
        assertEquals(ParticleStatus.ALL, result.particleStatus);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundClientInformationPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
