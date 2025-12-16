package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_21_R7.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrappedStreamCodecTests {
    @BeforeAll
    public static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testWithItemStack() {
        StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetSlotPacket> nmsCodec = ClientboundContainerSetSlotPacket.STREAM_CODEC;
        WrappedStreamCodec codec = new WrappedStreamCodec(nmsCodec);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
        ClientboundContainerSetSlotPacket packet = new ClientboundContainerSetSlotPacket(1, 2, 3,
            CraftItemStack.asNMSCopy(new ItemStack(Material.GOLDEN_SHOVEL)));

        codec.encode(buf, packet);
        ClientboundContainerSetSlotPacket roundTrip = (ClientboundContainerSetSlotPacket) codec.decode(buf);

        assertEquals(Material.GOLDEN_SHOVEL, CraftItemStack.asBukkitCopy(roundTrip.getItem()).getType());
    }

    @Test
    public void testWithStandardSerializer() {
        StreamCodec<FriendlyByteBuf, ClientboundOpenBookPacket> nmsCodec = ClientboundOpenBookPacket.STREAM_CODEC;
        WrappedStreamCodec codec = new WrappedStreamCodec(nmsCodec);

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        ClientboundOpenBookPacket packet = new ClientboundOpenBookPacket(InteractionHand.OFF_HAND);

        codec.encode(buf, packet);
        ClientboundOpenBookPacket roundTrip = (ClientboundOpenBookPacket) codec.decode(buf);

        assertEquals(InteractionHand.OFF_HAND, roundTrip.getHand());
    }
}
