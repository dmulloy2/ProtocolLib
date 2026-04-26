package net.dmulloy2.protocol.wrappers.game.serverbound;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MinecraftKey;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import net.dmulloy2.protocol.wrappers.game.serverbound.WrappedServerboundTestInstanceBlockActionPacket.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WrappedServerboundTestInstanceBlockActionPacketTest {

    @BeforeAll
    static void beforeAll() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testNoArgsCreate() {
        WrappedServerboundTestInstanceBlockActionPacket w = new WrappedServerboundTestInstanceBlockActionPacket();
        assertEquals(PacketType.Play.Client.TEST_INSTANCE_BLOCK_ACTION, w.getHandle().getType());
    }

    @Test
    void testSetAndGetPosAndAction() {
        WrappedServerboundTestInstanceBlockActionPacket w = new WrappedServerboundTestInstanceBlockActionPacket();

        w.setPos(new BlockPosition(10, 64, 20));
        w.setAction(Action.QUERY);

        assertEquals(new BlockPosition(10, 64, 20), w.getPos());
        assertEquals(Action.QUERY, w.getAction());
    }

    @Test
    void testSetAndGetData() {
        WrappedServerboundTestInstanceBlockActionPacket w = new WrappedServerboundTestInstanceBlockActionPacket();

        WrappedData data = new WrappedData();
        data.test = Optional.of(new MinecraftKey("my_test"));
        data.size = new BlockPosition(8, 4, 8);
        data.rotation = Rotation.CLOCKWISE_90;
        data.ignoreEntities = true;
        data.status = Status.RUNNING;
        data.errorMessage = Optional.of(WrappedChatComponent.fromText("oh no"));

        w.setData(data);

        WrappedData round = w.getData();
        assertEquals(Optional.of(new MinecraftKey("my_test")), round.test);
        assertEquals(new BlockPosition(8, 4, 8), round.size);
        assertEquals(Rotation.CLOCKWISE_90, round.rotation);
        assertTrue(round.ignoreEntities);
        assertEquals(Status.RUNNING, round.status);
        assertTrue(round.errorMessage.isPresent());
        assertEquals(WrappedChatComponent.fromText("oh no"), round.errorMessage.get());
    }

    @Test
    void testSetAndGetDataEmptyOptionals() {
        WrappedServerboundTestInstanceBlockActionPacket w = new WrappedServerboundTestInstanceBlockActionPacket();

        WrappedData data = new WrappedData();
        data.test = Optional.empty();
        data.size = new BlockPosition(1, 1, 1);
        data.rotation = Rotation.NONE;
        data.ignoreEntities = false;
        data.status = Status.CLEARED;
        data.errorMessage = Optional.empty();

        w.setData(data);

        WrappedData round = w.getData();
        assertFalse(round.test.isPresent());
        assertFalse(round.errorMessage.isPresent());
        assertEquals(Rotation.NONE, round.rotation);
        assertEquals(Status.CLEARED, round.status);
    }

    @Test
    void testWrongPacketTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new WrappedServerboundTestInstanceBlockActionPacket(new PacketContainer(PacketType.Play.Client.CHAT)));
    }
}
