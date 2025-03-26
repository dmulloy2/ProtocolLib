package dev.protocollib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import dev.protocollib.api.ProtocolLib;
import dev.protocollib.api.listener.PacketListenerBundleBehavior;
import dev.protocollib.api.listener.PacketListenerPriority;
import dev.protocollib.api.packet.MutablePacketContainer;
import dev.protocollib.api.packet.PacketContainer;
import dev.protocollib.api.packet.PacketTypes;
import dev.protocollib.api.reflect.MutableGenericAccessor;

public class Example {
 
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static ProtocolLib protocolLib;
    private static Plugin plugin;

    public static class ParticleData {
        public String s;
        public int i;
    }

    public static class ParticlePacket {
        public int duration;
        public String name;
        public ParticleData data;
    }

    static {
        MutablePacketContainer packet = null;
        Class<?> clazz = MutableGenericAccessor.class;
        
        packet.accessor().update(a -> {
            a.update(ParticleData.class, 0, b -> {
                b.set(int.class, 0, -1);
            });
        });

        packet.accessor().update(accessor -> {
            
            accessor.setObject(clazz, 1, "");
            accessor.set(Integer.class, 1, 1235);
            accessor.set(String.class, 1, "world");

            accessor.update(Object.class, 1, a -> {
               a.update(Object.class, 1, b -> {
                  b.set(int.class, 2, -1); 
               });
            });

            accessor.update(Object.class, 1, mutableAccessor -> {
                mutableAccessor.set(int.class, 1, -1);
            });

            accessor.update(ParticleData.class, 1, (pd) -> {
                pd.i++;
                return pd;
            });

            accessor.update(ParticleData.class, 1, (pd) -> {
                pd.set(int.class, 1, pd.get(int.class, 1) + 1);
            });
        });
    }

    // ========================
    //  Packet Listeners
    // ========================
    
    static void registerListeners() {
        // sync read-only listener
        protocolLib
            .createListener(plugin)
            .types(PacketTypes.Game.LEVEL_CHUNK)
            .priority(PacketListenerPriority.LOW)
            .includeCanceledPackets()
            .bundleBehavior(PacketListenerBundleBehavior.SKIP_OUTSIDE_BUNDLE)
            .registerSync((packet, context) -> {
                Chunk chunk = Chunk.from(packet);
                // Sync processing
            });

        // async modify packet on netty thread
        protocolLib
            .createListener(plugin)
            .types(PacketTypes.Game.LEVEL_CHUNK)
            .mutable()
            .registerAsync((packet, context) -> {
                Chunk chunk = Chunk.from(packet);
                
                // do processing here ...
                // write changes to packet ...
                
                context.addTransmissionListener(chunk::markSent);
                context.resumeProcessing();
            });

        // async modify packet on app thread-pool
        protocolLib
            .createListener(plugin)
            .types(PacketTypes.Game.LEVEL_CHUNK)
            .mutable()
            .registerAsync((packet, context) -> {
                Chunk chunk = Chunk.from(packet);

                EXECUTOR.execute(() -> {
                    
                    // do heavy processing here ...
                    // write changes to packet ...
                    
                    context.addTransmissionListener(chunk::markSent);
                    context.resumeProcessing();
                });
            });
    }

    // ========================
    //  Packet Sending
    // ========================
    
    static void sendPackets(Player player, Chunk chunk) {
        // full packet operation
        protocolLib
            .connection(player)
            .packetOperation()
            .skipProcessing()
            .postTransmission(chunk::markSent)
            .send(chunk.packet());

        // connection shortcut
        protocolLib
            .connection(player)
            .sendPacket(chunk.packet());

        // direct API shortcut
        protocolLib
            .sendPacket(player, chunk.packet());
    }

    // ========================
    //  Chunk Implementation
    // ========================
    
    static class Chunk {
        static Chunk from(PacketContainer packet) {
            return new Chunk();
        }
        
        PacketContainer packet() { return null; }
        void markSent() {}
    }
}