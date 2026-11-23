package com.comphenix.protocol.events;

import java.util.function.Function;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * New packets must contain zero vectors instead of null, otherwise they cannot be read or sent.
 */
public class NewPacketVectorTest {

    private static final Vector ZERO = new Vector(0, 0, 0);

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testEntityVelocity() {
        assertVector(PacketType.Play.Server.ENTITY_VELOCITY, packet -> packet.getVectors().read(0));
    }

    @Test
    public void testSpawnEntity() {
        assertVector(PacketType.Play.Server.SPAWN_ENTITY, packet -> packet.getVectors().read(0));
    }

    @Test
    public void testExplosion() {
        // the other defaults of a new explosion, such as its particle, cannot be encoded yet
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        assertEquals(ZERO, packet.getVectors().read(0));
    }

    @Test
    public void testUseEntity() {
        assertVector(PacketType.Play.Client.USE_ENTITY, packet -> packet.getVectors().read(0));
    }

    @Test
    public void testUseItemOn() {
        assertVector(PacketType.Play.Client.USE_ITEM_ON, packet -> packet.getMovingBlockPositions().read(0).getPosVector());
    }

    @Test
    public void testPlayerPosition() {
        assertPositionMoveRotation(PacketType.Play.Server.POSITION);
    }

    @Test
    public void testEntityTeleport() {
        assertPositionMoveRotation(PacketType.Play.Server.ENTITY_TELEPORT);
    }

    private static void assertPositionMoveRotation(PacketType type) {
        assertVector(type, packet -> packet.getPositionMoveRotation().read(0).getPosition());
        assertVector(type, packet -> packet.getPositionMoveRotation().read(0).getDeltaMovement());
    }

    private static void assertVector(PacketType type, Function<PacketContainer, Vector> vector) {
        PacketContainer packet = new PacketContainer(type);
        assertEquals(ZERO, vector.apply(packet));

        WrappedStreamCodec codec = PacketRegistry.getStreamCodec(type.getPacketClass());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
        codec.encode(buffer, packet.getHandle());

        PacketContainer decoded = PacketContainer.fromPacket(codec.decode(buffer));
        assertEquals(ZERO, vector.apply(decoded));
    }
}
