/**
 * (c) 2016 dmulloy2
 */
package com.comphenix.protocol.injector;

import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.wrappers.CustomPacketPayloadWrapper;
import com.comphenix.protocol.wrappers.MinecraftKey;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author dmulloy2
 */
public class WirePacketTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    // @Test
    public void testPackets() {
        List<String> failures = new ArrayList<>();

        for (PacketType type : PacketType.values()) {
            if (type.isDeprecated()) {
                continue;
            }

            try {
                PacketContainer packet = new PacketContainer(type);
                WirePacket wire = WirePacket.fromPacket(packet);
                WirePacket handle = WirePacket.fromPacket(packet.getHandle());
                assertEquals(wire, handle);
            } catch (Exception ex) {
                failures.add(type + " :: " + ex.getMessage());
                System.out.println(type);
                ex.printStackTrace();
            }
        }

        assertEquals(failures, new ArrayList<>());
    }

    @Test
    public void testBytesFromClientboundPacket() {
        ClientboundOpenBookPacket handle = new ClientboundOpenBookPacket(InteractionHand.OFF_HAND);
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.OPEN_BOOK, handle);

        assertArrayEquals(new byte[] {1}, WirePacket.bytesFromPacket(packet));
    }

    @Test
    public void testBytesFromServerboundPacket() {
        ServerboundKeepAlivePacket handle = new ServerboundKeepAlivePacket(0x0102030405060708L);
        PacketContainer packet = new PacketContainer(PacketType.Play.Client.KEEP_ALIVE, handle);

        assertArrayEquals(new byte[] {1, 2, 3, 4, 5, 6, 7, 8}, WirePacket.bytesFromPacket(packet));
    }

    @Test
    public void testBytesFromPacketWithItemStack() {
        ClientboundContainerSetSlotPacket handle = new ClientboundContainerSetSlotPacket(1, 2, 3,
                CraftItemStack.asNMSCopy(new ItemStack(Material.GOLDEN_SHOVEL)));
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SET_SLOT, handle);

        // the item stack can only be written to a buffer which knows the registries
        RegistryFriendlyByteBuf expected = new RegistryFriendlyByteBuf(Unpooled.buffer(),
                CraftRegistry.getMinecraftRegistry());
        ClientboundContainerSetSlotPacket.STREAM_CODEC.encode(expected, handle);

        assertArrayEquals(ByteBufUtil.getBytes(expected), WirePacket.bytesFromPacket(packet));
    }

    @Test
    public void testFromPacketHandle() {
        ClientboundOpenBookPacket handle = new ClientboundOpenBookPacket(InteractionHand.MAIN_HAND);
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.OPEN_BOOK, handle);

        WirePacket wire = WirePacket.fromPacket(handle);

        assertEquals(PacketType.Play.Server.OPEN_BOOK.getCurrentId(), wire.getId());
        assertArrayEquals(new byte[] {0}, wire.getBytes());
        assertEquals(WirePacket.fromPacket(packet), wire);
    }

    @Test
    public void testBytesFromCustomPayloadPacket() {
        byte[] data = {0x00, 0x01, 0x05, 0x07};
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CUSTOM_PAYLOAD);
        packet.getCustomPacketPayloads().write(0,
                new CustomPacketPayloadWrapper(data, new MinecraftKey("protocollib", "test")));

        FriendlyByteBuf expected = new FriendlyByteBuf(Unpooled.buffer());
        expected.writeUtf("protocollib:test");
        expected.writeBytes(data);

        assertArrayEquals(ByteBufUtil.getBytes(expected), WirePacket.bytesFromPacket(packet));
        // the packet has to stay writable, it is still sent after it was logged
        assertArrayEquals(ByteBufUtil.getBytes(expected), WirePacket.bytesFromPacket(packet));
        assertArrayEquals(data, packet.getCustomPacketPayloads().read(0).getPayload());
    }

    @Test
    public void testSerialization() {
        int id = 42;
        byte[] array = {1, 3, 7, 21, 88, 67, 8};

        WirePacket packet = new WirePacket(id, array);

        ByteBuf buf = packet.serialize();

        int backId = WirePacket.readVarInt(buf);
        byte[] backArray = new byte[buf.readableBytes()];
        buf.readBytes(backArray);

        assertEquals(id, backId);
        assertArrayEquals(array, backArray);
    }
}