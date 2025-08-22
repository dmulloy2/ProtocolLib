package com.comphenix.protocol.injector.netty.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.injector.ListenerManager;
import com.comphenix.protocol.injector.netty.Injector;
import com.comphenix.protocol.injector.netty.channel.InjectionFactory;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;
import com.comphenix.protocol.reflect.fuzzy.FuzzyMethodContract;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.Pair;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class NetworkManagerInjector {

    private static final String INBOUND_INJECT_HANDLER_NAME = "protocol_lib_inbound_inject";

    // all list fields which we've overridden and need to revert to a non-proxying list afterwards
    private final Set<Pair<Object, FieldAccessor>> overriddenLists = new HashSet<>();

    private final ErrorReporter errorReporter;
    private final InjectionFactory injectionFactory;

    // netty handler
    private final InjectionChannelInitializer pipelineInjectorHandler;

    // status of this injector
    private boolean closed = false;
    private boolean injected = false;

    public NetworkManagerInjector(Plugin plugin, ListenerManager listenerManager, ErrorReporter reporter) {
        this.errorReporter = reporter;
        this.injectionFactory = new InjectionFactory(plugin, reporter, listenerManager);

        // hooking netty handlers
        InjectionChannelInboundHandler injectionHandler = new InjectionChannelInboundHandler(
                this.errorReporter,
                this.injectionFactory);
        this.pipelineInjectorHandler = new InjectionChannelInitializer(INBOUND_INJECT_HANDLER_NAME, injectionHandler);
    }

    public Injector getInjector(Player player) {
    	return this.injectionFactory.fromPlayer(player);
    }

    public Injector getInjector(Channel channel) {
        return injectionFactory.fromChannel(channel);
    }

    @SuppressWarnings("unchecked")
    public void inject() {
        if (this.closed || this.injected) {
            return;
        }

        // get all "server connections" defined in the minecraft server class
        FuzzyReflection server = FuzzyReflection.fromClass(MinecraftReflection.getMinecraftServerClass());
        List<Method> serverConnectionGetter = server.getMethodList(FuzzyMethodContract.newBuilder()
                .parameterCount(0)
                .banModifier(Modifier.STATIC)
                .returnTypeExact(MinecraftReflection.getServerConnectionClass())
                .build());

        // get the first available server connection
        Object serverInstance = server.getSingleton();
        Object serverConnection = null;

        for (Method method : serverConnectionGetter) {
            try {
                // use all methods until the first one actually returns a server connection instance
                serverConnection = method.invoke(serverInstance);
                if (serverConnection != null) {
                    break;
                }
            } catch (Exception exception) {
                ProtocolLogger.debug("Exception invoking getter for server connection " + method, exception);
            }
        }

        // check if we got the server connection to use
        if (serverConnection == null) {
            throw new IllegalStateException("Unable to retrieve ServerConnection instance from MinecraftServer");
        }

        FuzzyReflection serverConnectionFuzzy = FuzzyReflection.fromObject(serverConnection, true);
        List<Field> listFields = serverConnectionFuzzy.getFieldList(FuzzyFieldContract.newBuilder()
                .typeDerivedOf(List.class)
                .banModifier(Modifier.STATIC)
                .build());

        // loop over all fields which we need to override and try to do so if needed
        for (Field field : listFields) {
            // ensure that the generic type of the field is actually a channel future, rather than guessing
            // by peeking objects from the list
            if (field.getGenericType().getTypeName().contains(ChannelFuture.class.getName())) {
                // we can only guess if we need to override it, but it looks like we should.
                // we now need the old value of the field to wrap it into a new collection
                FieldAccessor accessor = Accessors.getFieldAccessor(field);
                List<Object> value = (List<Object>) accessor.get(serverConnection);

                // mark down that we've overridden the field
                this.overriddenLists.add(new Pair<>(serverConnection, accessor));

                // we need to synchronize accesses to the list ourselves, see Collections.SynchronizedCollection
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (value) {
                    // override the list field with our list
                    List<Object> newList = new ListeningList(value, this.pipelineInjectorHandler);
                    accessor.set(serverConnection, newList);
                }
            }
        }

        // mark as injected
        this.injected = true;
    }

    public void close() {
        if (this.closed || !this.injected) {
            return;
        }

        // change the state first to prevent further injections / closes
        this.closed = true;
        this.injected = false;

        // undo changes we did to any field
        for (Pair<Object, FieldAccessor> list : this.overriddenLists) {
            // get the value of the field we've overridden, if it is no longer a ListeningList someone probably jumped in
            // and replaced the field himself - we are out safely as the other person needs to clean the mess...
            Object currentFieldValue = list.getSecond().get(list.getFirst());
            if (currentFieldValue instanceof ListeningList) {
                // just reset to the list we wrapped originally
                ListeningList ourList = (ListeningList) currentFieldValue;
                List<Object> original = ourList.getOriginal();
                synchronized (original) {
                    // revert the injection from all values of the list
                    ourList.unProcessAll();
                    list.getSecond().set(list.getFirst(), original);
                }
            }
        }

        // clear up
        this.overriddenLists.clear();
        this.injectionFactory.close();
    }
}
