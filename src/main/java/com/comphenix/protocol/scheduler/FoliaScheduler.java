package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class FoliaScheduler implements ProtocolScheduler {
    private final Object foliaRegionScheduler;
    private final MethodAccessor runAtFixedRate;
    private final MethodAccessor runDelayed;
    private final MethodAccessor execute;
    private final MethodAccessor cancel;

    private final Object foliaAsyncScheduler;
    private final MethodAccessor executeAsync;

    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;

        MethodAccessor getScheduler = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getGlobalRegionScheduler");
        this.foliaRegionScheduler = getScheduler.invoke(Bukkit.getServer());

        this.runAtFixedRate = Accessors.getMethodAccessor(foliaRegionScheduler.getClass(), "runAtFixedRate", Plugin.class,
                Consumer.class, long.class, long.class);
        this.execute = Accessors.getMethodAccessor(foliaRegionScheduler.getClass(), "run", Plugin.class, Consumer.class);
        this.runDelayed = Accessors.getMethodAccessor(foliaRegionScheduler.getClass(), "runDelayed", Plugin.class, Consumer.class, long.class);

        MethodAccessor getAsyncScheduler = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getAsyncScheduler");
        foliaAsyncScheduler = getAsyncScheduler.invoke(Bukkit.getServer());

        this.executeAsync = Accessors.getMethodAccessor(foliaAsyncScheduler.getClass(), "runNow", Plugin.class, Consumer.class);

        Class<?> taskClass = MinecraftReflection.getLibraryClass("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
        this.cancel = Accessors.getMethodAccessor(taskClass, "cancel");
    }

    @Override
    public Task scheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        Object taskHandle = runAtFixedRate.invoke(foliaRegionScheduler, plugin, (Consumer<Object>)(t -> task.run()), delay, period);
        return new FoliaTask(cancel, taskHandle);
    }

    @Override
    public Task runTask(Runnable task) {
        Object taskHandle = execute.invoke(foliaRegionScheduler, plugin, (Consumer<Object>)(t -> task.run()));
        return new FoliaTask(cancel, taskHandle);
    }

    @Override
    public Task scheduleSyncDelayedTask(Runnable task, long delay) {
        Object taskHandle = runDelayed.invoke(foliaRegionScheduler, plugin, (Consumer<Object>)(t -> task.run()), delay);
        return new FoliaTask(cancel, taskHandle);
    }

    @Override
    public Task runTaskAsync(Runnable task) {
    	Object taskHandle = executeAsync.invoke(foliaAsyncScheduler, plugin, (Consumer<Object>)(t -> task.run()));
    	return new FoliaTask(cancel, taskHandle);
    }
}
