package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundSetActionBarTextPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testCreate() {
        WrappedClientboundSetActionBarTextPacket w = new WrappedClientboundSetActionBarTextPacket();
        w.setText(WrappedChatComponent.fromText("Hello, action bar!"));

        assertEquals(PacketType.Play.Server.SET_ACTION_BAR_TEXT, w.getHandle().getType());

        ClientboundSetActionBarTextPacket p = (ClientboundSetActionBarTextPacket) w.getHandle().getHandle();

        assertNotNull(p.text());
    }

    @Test
    void testReadFromExistingPacket() {
        ClientboundSetActionBarTextPacket nmsPacket = new ClientboundSetActionBarTextPacket(
                Component.literal("Bar text")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetActionBarTextPacket wrapper = new WrappedClientboundSetActionBarTextPacket(container);

        assertTrue(wrapper.getText().getJson().contains("Bar text"));
    }

    @Test
    void testModifyExistingPacket() {
        ClientboundSetActionBarTextPacket nmsPacket = new ClientboundSetActionBarTextPacket(
                Component.literal("original")
        );

        PacketContainer container = PacketContainer.fromPacket(nmsPacket);
        WrappedClientboundSetActionBarTextPacket wrapper = new WrappedClientboundSetActionBarTextPacket(container);

        wrapper.setText(WrappedChatComponent.fromText("updated"));

        assertTrue(wrapper.getText().getJson().contains("updated"));
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundSetActionBarTextPacket(
                        new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
