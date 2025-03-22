package dev.protocollib;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import dev.protocollib.api.ProtocolLib;
import dev.protocollib.api.listener.*;
import dev.protocollib.api.packet.*;
import java.util.concurrent.*;

public class Example {
 
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static ProtocolLib protocolLib;
    private static Plugin plugin;

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