package com.comphenix.protocol.injector.netty.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.utility.MinecraftFields;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.AttributeKey;
import io.netty.util.DefaultAttributeMap;
import io.netty.util.concurrent.GenericFutureListener;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class InjectionFactoryTest {

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testStalePlayerDoesNotCreateInjectorPerPacket() throws Exception {
        // the channel of a player who left: closed, and like on a real server its close listeners run later on the
        // event loop, not on the thread that sends the packet
        FakeChannel channel = new FakeChannel(true);
        Player player = createPlayer();

        try (MockedConstruction<PacketListenerInvoker> invokers = mockConstruction(PacketListenerInvoker.class);
             MockedStatic<MinecraftFields> fields = mockStatic(MinecraftFields.class)) {
            InjectionFactory factory = createFactory(fields, player, channel);

            for (int packet = 0; packet < 3; packet++) {
                assertTrue(factory.fromPlayer(player).isClosed());
                channel.runCloseListeners();
            }

            assertEquals(0, invokers.constructed().size(), "injectors were built for a closed channel");
        }
    }

    @Test
    public void testChannelThatWasNeverConnectedKeepsItsInjector() throws Exception {
        // fake players (NPCs) may use channels that are inactive but never closed; they keep the regular injector
        FakeChannel channel = new FakeChannel(false);
        Player player = createPlayer();

        try (MockedConstruction<PacketListenerInvoker> invokers = mockConstruction(PacketListenerInvoker.class);
             MockedStatic<MinecraftFields> fields = mockStatic(MinecraftFields.class)) {
            InjectionFactory factory = createFactory(fields, player, channel);

            Injector injector = factory.fromPlayer(player);
            assertFalse(injector.isClosed());
            assertSame(injector, factory.fromPlayer(player));
            assertEquals(1, invokers.constructed().size());
        }
    }

    private static Player createPlayer() {
        Player player = mock(Player.class);
        when(player.getName()).thenReturn("Player");
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        return player;
    }

    private static InjectionFactory createFactory(MockedStatic<MinecraftFields> fields, Player player,
            FakeChannel channel) {
        // the real Connection can't be loaded in tests, so the injector gets a stand-in and a mocked invoker
        FakeNetworkManager networkManager = new FakeNetworkManager();
        networkManager.channel = channel.channel;
        fields.when(() -> MinecraftFields.getNetworkManager(player)).thenReturn(networkManager);

        return new InjectionFactory(mock(Plugin.class), mock(ErrorReporter.class), mock(ListenerManager.class));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final class FakeChannel {

        private final Channel channel = mock(Channel.class);
        private final ChannelFuture closeFuture = mock(ChannelFuture.class);
        private final List<GenericFutureListener> closeListeners = new ArrayList<>();

        private FakeChannel(boolean closed) {
            when(this.closeFuture.isDone()).thenReturn(closed);
            when(this.closeFuture.addListener(any())).thenAnswer(invocation -> {
                this.closeListeners.add(invocation.getArgument(0));
                return this.closeFuture;
            });

            DefaultAttributeMap attributes = new DefaultAttributeMap();
            EventLoop eventLoop = mock(EventLoop.class);
            when(eventLoop.inEventLoop()).thenReturn(true);
            when(this.channel.isActive()).thenReturn(false);
            when(this.channel.closeFuture()).thenReturn(this.closeFuture);
            when(this.channel.attr(any())).thenAnswer(invocation -> attributes.attr((AttributeKey) invocation.getArgument(0)));
            when(this.channel.eventLoop()).thenReturn(eventLoop);
            when(this.channel.pipeline()).thenReturn(mock(ChannelPipeline.class));
        }

        private void runCloseListeners() throws Exception {
            List<GenericFutureListener> pending = new ArrayList<>(this.closeListeners);
            this.closeListeners.clear();
            for (GenericFutureListener listener : pending) {
                listener.operationComplete(this.closeFuture);
            }
        }
    }

    private static final class FakeNetworkManager {

        private Channel channel;
    }
}
