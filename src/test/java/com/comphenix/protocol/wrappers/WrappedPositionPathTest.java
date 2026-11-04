package com.comphenix.protocol.wrappers;

import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.world.entity.PositionPath;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedPositionPathTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testLinear() {
        WrappedPositionPath path = WrappedPositionPath.linear(new Vector(1.5, 64.0, -3.5));

        assertFalse(path.isStepped());
        assertEquals(new Vector(1.5, 64.0, -3.5), path.getEndPosition());
        assertThrows(IllegalStateException.class, path::getSteps);

        assertEquals(PositionPath.of(new Vec3(1.5, 64.0, -3.5)), path.getHandle());
    }

    @Test
    public void testStepped() {
        List<WrappedPositionPath.Step> steps = List.of(
                new WrappedPositionPath.Step(new Vector(1, 2, 3), 1),
                new WrappedPositionPath.Step(new Vector(4, 5, 6), 3));
        WrappedPositionPath path = WrappedPositionPath.stepped(steps);

        assertTrue(path.isStepped());
        assertEquals(steps, path.getSteps());
        assertEquals(new Vector(4, 5, 6), path.getEndPosition());
    }

    @Test
    public void testSteppedRequiresSteps() {
        assertThrows(IllegalArgumentException.class, () -> WrappedPositionPath.stepped(List.of()));
    }

    @Test
    public void testStepIsImmutable() {
        Vector position = new Vector(1, 2, 3);
        WrappedPositionPath.Step step = new WrappedPositionPath.Step(position, 1);

        position.setX(10);
        step.getPosition().setY(20);

        assertEquals(new Vector(1, 2, 3), step.getPosition());
    }

    @Test
    public void testEquality() {
        WrappedPositionPath first = WrappedPositionPath.linear(new Vector(1, 2, 3));
        WrappedPositionPath second = WrappedPositionPath.fromHandle(PositionPath.of(new Vec3(1, 2, 3)));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, WrappedPositionPath.linear(new Vector(1, 2, 4)));
    }

    @Test
    public void testEntityPositionSyncPacketRoundTrip() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_POSITION_SYNC);
        packet.getIntegers().write(0, 42);
        packet.getFloat().write(0, 90.0f).write(1, -15.0f);
        packet.getBooleans().write(0, true);

        WrappedPositionPath path = WrappedPositionPath.stepped(List.of(
                new WrappedPositionPath.Step(new Vector(10.0, 70.0, -5.0), 1),
                new WrappedPositionPath.Step(new Vector(10.5, 70.0, -5.5), 2)));
        packet.getPositionPaths().write(0, path);
        assertEquals(path, packet.getPositionPaths().read(0));

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundEntityPositionSyncPacket.STREAM_CODEC.encode(buf, (ClientboundEntityPositionSyncPacket) packet.getHandle());
        ClientboundEntityPositionSyncPacket decoded = ClientboundEntityPositionSyncPacket.STREAM_CODEC.decode(buf);

        PacketContainer roundTrip = PacketContainer.fromPacket(decoded);
        assertEquals(path, roundTrip.getPositionPaths().read(0));
        assertEquals(42, roundTrip.getIntegers().read(0));
        assertEquals(90.0f, roundTrip.getFloat().read(0), 1e-6f);
        assertEquals(-15.0f, roundTrip.getFloat().read(1), 1e-6f);
        assertTrue(roundTrip.getBooleans().read(0));
    }
}
