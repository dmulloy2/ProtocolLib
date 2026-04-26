package net.dmulloy2.protocol.wrappers.game.clientbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class WrappedClientboundRecipeBookRemovePacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedClientboundRecipeBookRemovePacket w = new WrappedClientboundRecipeBookRemovePacket();
        assertEquals(PacketType.Play.Server.RECIPE_BOOK_REMOVE, w.getHandle().getType());
    }

    @Test
    void testGetRecipeIndicesEmptyByDefault() {
        WrappedClientboundRecipeBookRemovePacket w = new WrappedClientboundRecipeBookRemovePacket();
        List<Integer> indices = w.getRecipeIndices();
        assertNotNull(indices);
    }

    @Test
    void testSetAndGetRecipeIndices() {
        WrappedClientboundRecipeBookRemovePacket w = new WrappedClientboundRecipeBookRemovePacket();
        w.setRecipeIndices(List.of(1, 42, 100));

        List<Integer> indices = w.getRecipeIndices();
        assertEquals(List.of(1, 42, 100), indices);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedClientboundRecipeBookRemovePacket(new PacketContainer(PacketType.Play.Server.CHAT)));
    }
}
