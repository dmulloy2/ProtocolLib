package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

public class FoliaScheduler implements ProtocolScheduler {
    private final Object foliaScheduler;
    private final MethodAccessor runAtFixedRate;
    private final MethodAccessor runDelayed;
    private final MethodAccessor execute;
    private final MethodAccessor cancel;
    private final Plugin plugin;

    public FoliaScheduler(Plugin plugin) {
        this.plugin = plugin;

        MethodAccessor getScheduler = Accessors.getMethodAccessor(Bukkit.getServer().getClass(), "getGlobalRegionScheduler");
        this.foliaScheduler = getScheduler.invoke(Bukkit.getServer());

        this.runAtFixedRate = Accessors.getMethodAccessor(foliaScheduler.getClass(), "runAtFixedRate", Plugin.class,
                Consumer.class, long.class, long.class);
        this.execute = Accessors.getMethodAccessor(foliaScheduler.getClass(), "run", Plugin.class, Runnable.class);
        this.runDelayed = Accessors.getMethodAccessor(foliaScheduler.getClass(), "runDelayed", Plugin.class, Runnable.class, long.class);

        Class<?> taskClass = MinecraftReflection.getLibraryClass("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
        this.cancel = Accessors.getMethodAccessor(taskClass, "cancel");
    }

    @Override
    public Task scheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        Object taskHandle = runAtFixedRate.invoke(foliaScheduler, plugin, (Consumer<Object>)(t -> task.run()), delay, period);
        return new FoliaTask(cancel, taskHandle);
    }

    @Override
    public Task runTask(Runnable task) {
        Object taskHandle = execute.invoke(foliaScheduler, plugin, (Consumer<Object>)(t -> task.run()));
        return new FoliaTask(cancel, taskHandle);
    }

    @Override
    public Task scheduleSyncDelayedTask(Runnable task, long delay) {
        Object taskHandle = runDelayed.invoke(foliaScheduler, plugin, (Consumer<Object>)(t -> task.run()), delay);
        return new FoliaTask(cancel, taskHandle);
    }
}
