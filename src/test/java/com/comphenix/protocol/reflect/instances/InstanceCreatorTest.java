package com.comphenix.protocol.reflect.instances;

import java.util.List;
import java.util.Optional;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedStreamCodec;

import io.netty.buffer.Unpooled;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.bukkit.craftbukkit.CraftRegistry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InstanceCreatorTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testPrefersConstructorWithDefaultForEveryParameter() {
        // Runnable has no default value and String does. The two classes declare the same constructors in opposite
        // order: HotSpot lists the constructors of a class without methods in declaration order, so one of them lists
        // the constructor without a default value first and the other one lists it last.
        assertEquals("", ((CompleteFirst) InstanceCreator.forClass(CompleteFirst.class).get()).value);
        assertEquals("", ((CompleteLast) InstanceCreator.forClass(CompleteLast.class).get()).value);
    }

    @Test
    public void testPrefersEmptyCollectionOverMadeUpElement() {
        // both constructors receive a value, but only the list is empty; see testPrefersConstructorWithDefaultForEveryParameter
        // for the order of the constructors
        assertEquals(List.of(), ((EmptyFirst) InstanceCreator.forClass(EmptyFirst.class).get()).values);
        assertEquals(List.of(), ((EmptyLast) InstanceCreator.forClass(EmptyLast.class).get()).values);
    }

    @Test
    public void testPlayerInfoPacket() {
        // Paper adds a constructor taking a single entry, which receives a made-up entry where one can be created
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
        assertEquals(List.of(), packet.getPlayerInfoDataLists().read(0));
        assertEquals(List.of(), packet.deepClone().getPlayerInfoDataLists().read(0));
    }

    @Test
    public void testMapPacket() {
        // the record constructor receives empty optionals, the other one an empty collection of decorations and a patch
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.MAP);
        assertEquals(Optional.empty(), packet.getModifier().read(3));
        assertEquals(Optional.empty(), packet.getModifier().read(4));
    }

    @Test
    public void testSystemChatPacket() {
        // the packet also has constructors taking a Minecraft or an Adventure component, which have no default value
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        WrappedChatComponent content = packet.getChatComponents().read(0);
        assertNotNull(content);

        PacketContainer decoded = roundTrip(packet);
        assertEquals(content, decoded.getChatComponents().read(0));
        assertFalse(decoded.getBooleans().read(0));
    }

    @Test
    public void testEntityDestroyPacket() {
        // the packet also has a constructor taking an IntList, which has no default value
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        assertEquals(List.of(), packet.getIntLists().read(0));

        assertEquals(List.of(), roundTrip(packet).getIntLists().read(0));
    }

    private static PacketContainer roundTrip(PacketContainer packet) {
        WrappedStreamCodec codec = PacketRegistry.getStreamCodec(packet.getType().getPacketClass());
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), CraftRegistry.getMinecraftRegistry());
        codec.encode(buffer, packet.getHandle());
        return PacketContainer.fromPacket(codec.decode(buffer));
    }

    public static final class CompleteFirst {
        public final Object value;

        public CompleteFirst(String value) {
            this.value = value;
        }

        public CompleteFirst(Runnable value) {
            this.value = value;
        }
    }

    public static final class Element {
    }

    public static final class EmptyFirst {
        public final List<?> values;

        public EmptyFirst(List<?> values) {
            this.values = values;
        }

        public EmptyFirst(Element value) {
            this.values = List.of(value);
        }
    }

    public static final class EmptyLast {
        public final List<?> values;

        public EmptyLast(Element value) {
            this.values = List.of(value);
        }

        public EmptyLast(List<?> values) {
            this.values = values;
        }
    }

    public static final class CompleteLast {
        public final Object value;

        public CompleteLast(Runnable value) {
            this.value = value;
        }

        public CompleteLast(String value) {
            this.value = value;
        }
    }
}
