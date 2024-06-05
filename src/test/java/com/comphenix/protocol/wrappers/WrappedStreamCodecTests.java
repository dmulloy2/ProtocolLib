package com.comphenix.protocol.wrappers;

import com.comphenix.protocol.BukkitInitialization;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketDataSerializer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.PacketPlayOutOpenBook;
import net.minecraft.network.protocol.game.PacketPlayOutSetSlot;
import net.minecraft.world.EnumHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R4.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;
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
        StreamCodec<RegistryFriendlyByteBuf, PacketPlayOutSetSlot> nmsCodec = PacketPlayOutSetSlot.a;
        WrappedStreamCodec codec = new WrappedStreamCodec(nmsCodec);

        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
        PacketPlayOutSetSlot packet = new PacketPlayOutSetSlot(1, 2, 3, CraftItemStack.asNMSCopy(new ItemStack(Material.GOLDEN_SHOVEL)));

        codec.encode(buf, packet);
        PacketPlayOutSetSlot roundTrip = (PacketPlayOutSetSlot) codec.decode(buf);

        assertEquals(Material.GOLDEN_SHOVEL, CraftItemStack.asBukkitCopy(roundTrip.f()).getType());
    }

    @Test
    public void testWithStandardSerializer() {
        StreamCodec<PacketDataSerializer, PacketPlayOutOpenBook> nmsCodec = PacketPlayOutOpenBook.a;
        WrappedStreamCodec codec = new WrappedStreamCodec(nmsCodec);

        PacketDataSerializer buf = new PacketDataSerializer(Unpooled.buffer());
        PacketPlayOutOpenBook packet = new PacketPlayOutOpenBook(EnumHand.a);

        codec.encode(buf, packet);
        PacketPlayOutOpenBook roundTrip = (PacketPlayOutOpenBook) codec.decode(buf);

        assertEquals(EnumHand.a, roundTrip.b());
    }
}
