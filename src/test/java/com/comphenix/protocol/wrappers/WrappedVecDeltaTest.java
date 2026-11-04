package com.comphenix.protocol.wrappers;

import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.VecDelta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedVecDeltaTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testLinear() {
        WrappedVecDelta delta = WrappedVecDelta.linear((short) 1, (short) -2, (short) 4096);

        assertFalse(delta.isStepped());
        assertEquals(1, delta.getDeltaX());
        assertEquals(-2, delta.getDeltaY());
        assertEquals(4096, delta.getDeltaZ());
        assertThrows(IllegalStateException.class, delta::getSteps);

        assertEquals(new VecDelta.Linear((short) 1, (short) -2, (short) 4096), delta.getHandle());
    }

    @Test
    public void testStepped() {
        List<WrappedVecDelta.Step> steps = List.of(
                new WrappedVecDelta.Step((short) 10, (short) 0, (short) -10, 1),
                new WrappedVecDelta.Step((short) 5, (short) 6, (short) 7, 3));
        WrappedVecDelta delta = WrappedVecDelta.stepped(steps);

        assertTrue(delta.isStepped());
        assertEquals(steps, delta.getSteps());
        assertThrows(IllegalStateException.class, delta::getDeltaX);

        VecDelta handle = (VecDelta) delta.getHandle();
        assertEquals(2, handle.stepCount());
        assertTrue(handle.hasDeltaX());
    }

    @Test
    public void testSteppedRequiresSteps() {
        assertThrows(IllegalArgumentException.class, () -> WrappedVecDelta.stepped(List.of()));
    }

    @Test
    public void testEquality() {
        WrappedVecDelta first = WrappedVecDelta.linear((short) 1, (short) 2, (short) 3);
        WrappedVecDelta second = WrappedVecDelta.fromHandle(new VecDelta.Linear((short) 1, (short) 2, (short) 3));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, WrappedVecDelta.linear((short) 1, (short) 2, (short) 4));
    }

    @Test
    public void testRelativeMovePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        packet.getIntegers().write(0, 123);
        packet.getBooleans().write(0, true);

        WrappedVecDelta delta = WrappedVecDelta.linear((short) 100, (short) 200, (short) -300);
        packet.getVecDeltas().write(0, delta);

        assertEquals(1, packet.getVecDeltas().size());
        assertEquals(delta, packet.getVecDeltas().read(0));
        assertEquals(123, packet.getIntegers().read(0));
        assertTrue(packet.getBooleans().read(0));
    }

    @Test
    public void testSteppedRelativeMovePacketRoundTrip() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
        packet.getIntegers().write(0, 7);
        packet.getBooleans().write(0, true);

        WrappedVecDelta delta = WrappedVecDelta.stepped(List.of(
                new WrappedVecDelta.Step((short) 1, (short) 2, (short) 3, 1),
                new WrappedVecDelta.Step((short) -4, (short) 5, (short) -6, 2)));
        packet.getVecDeltas().write(0, delta);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundMoveEntityPacket.Pos.STREAM_CODEC.encode(buf, (ClientboundMoveEntityPacket.Pos) packet.getHandle());
        ClientboundMoveEntityPacket.Pos decoded = ClientboundMoveEntityPacket.Pos.STREAM_CODEC.decode(buf);

        PacketContainer roundTrip = PacketContainer.fromPacket(decoded);
        assertEquals(delta.getSteps(), roundTrip.getVecDeltas().read(0).getSteps());
        assertEquals(7, roundTrip.getIntegers().read(0));
        assertTrue(roundTrip.getBooleans().read(0));
    }

    @Test
    public void testNoShortsOnRelativeMovePacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
        assertEquals(0, packet.getShorts().size());
        assertEquals(1, packet.getVecDeltas().size());
    }
}
