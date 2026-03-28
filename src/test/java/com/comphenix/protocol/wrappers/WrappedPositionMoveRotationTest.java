package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WrappedPositionMoveRotationTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testCreateAndRead() {
        Vector position = new Vector(1.5, 64.0, -3.5);
        Vector delta = new Vector(0.1, -0.05, 0.2);
        float yRot = 45.0f;
        float xRot = -10.0f;

        WrappedPositionMoveRotation created = WrappedPositionMoveRotation.create(position, delta, yRot, xRot);

        assertEquals(position.getX(), created.getPosition().getX(), 1e-6);
        assertEquals(position.getY(), created.getPosition().getY(), 1e-6);
        assertEquals(position.getZ(), created.getPosition().getZ(), 1e-6);

        assertEquals(delta.getX(), created.getDeltaMovement().getX(), 1e-6);
        assertEquals(delta.getY(), created.getDeltaMovement().getY(), 1e-6);
        assertEquals(delta.getZ(), created.getDeltaMovement().getZ(), 1e-6);

        assertEquals(yRot, created.getYRot(), 1e-6f);
        assertEquals(xRot, created.getXRot(), 1e-6f);
    }

    @Test
    public void testEntityPositionSyncPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_POSITION_SYNC);

        Vector position = new Vector(10.0, 70.0, -5.0);
        Vector delta = new Vector(0.0, 0.0, 0.0);
        float yRot = 90.0f;
        float xRot = 0.0f;

        packet.getPositionMoveRotation().write(0,
                WrappedPositionMoveRotation.create(position, delta, yRot, xRot));

        WrappedPositionMoveRotation result = packet.getPositionMoveRotation().read(0);

        assertNotNull(result);

        assertEquals(position.getX(), result.getPosition().getX(), 1e-6);
        assertEquals(position.getY(), result.getPosition().getY(), 1e-6);
        assertEquals(position.getZ(), result.getPosition().getZ(), 1e-6);

        assertEquals(delta.getX(), result.getDeltaMovement().getX(), 1e-6);
        assertEquals(delta.getY(), result.getDeltaMovement().getY(), 1e-6);
        assertEquals(delta.getZ(), result.getDeltaMovement().getZ(), 1e-6);

        assertEquals(yRot, result.getYRot(), 1e-6f);
        assertEquals(xRot, result.getXRot(), 1e-6f);
    }

    @Test
    public void testEntityTeleportPacket() {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

        Vector position = new Vector(-100.5, 200.0, 300.75);
        Vector delta = new Vector(0.0, 0.0, 0.0);
        float yRot = 180.0f;
        float xRot = 30.0f;

        packet.getPositionMoveRotation().write(0,
                WrappedPositionMoveRotation.create(position, delta, yRot, xRot));

        WrappedPositionMoveRotation result = packet.getPositionMoveRotation().read(0);

        assertNotNull(result);

        assertEquals(position.getX(), result.getPosition().getX(), 1e-6);
        assertEquals(position.getY(), result.getPosition().getY(), 1e-6);
        assertEquals(position.getZ(), result.getPosition().getZ(), 1e-6);

        assertEquals(delta.getX(), result.getDeltaMovement().getX(), 1e-6);
        assertEquals(delta.getY(), result.getDeltaMovement().getY(), 1e-6);
        assertEquals(delta.getZ(), result.getDeltaMovement().getZ(), 1e-6);

        assertEquals(yRot, result.getYRot(), 1e-6f);
        assertEquals(xRot, result.getXRot(), 1e-6f);
    }

    @Test
    public void testFromHandle() {
        PositionMoveRotation nmsHandle = new PositionMoveRotation(
                new Vec3(5.0, 65.0, 5.0),
                new Vec3(0.0, -0.1, 0.0),
                270.0f,
                -45.0f);

        WrappedPositionMoveRotation wrapper = WrappedPositionMoveRotation.fromHandle(nmsHandle);

        assertEquals(5.0,   wrapper.getPosition().getX(), 1e-6);
        assertEquals(65.0,  wrapper.getPosition().getY(), 1e-6);
        assertEquals(5.0,   wrapper.getPosition().getZ(), 1e-6);

        assertEquals(0.0,  wrapper.getDeltaMovement().getX(), 1e-6);
        assertEquals(-0.1, wrapper.getDeltaMovement().getY(), 1e-6);
        assertEquals(0.0,  wrapper.getDeltaMovement().getZ(), 1e-6);

        assertEquals(270.0f, wrapper.getYRot(), 1e-6f);
        assertEquals(-45.0f, wrapper.getXRot(), 1e-6f);
    }
}
