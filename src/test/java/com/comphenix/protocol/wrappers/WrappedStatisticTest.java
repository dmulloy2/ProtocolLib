package com.comphenix.protocol.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WrappedStatisticTest {

    @BeforeAll
    static void initializeBukkit() {
        BukkitInitialization.initializeAll();
    }

    @Test
    void testStatisticMap() {
        Stat<Identifier> statistic = Stats.CUSTOM.get(Stats.JUMP);
        Object2IntMap<Stat<?>> statistics = new Object2IntOpenHashMap<>();
        statistics.put(statistic, 12);

        PacketContainer packet = new PacketContainer(
                PacketType.Play.Server.STATISTIC,
                new ClientboundAwardStatsPacket(statistics));
        Map<WrappedStatistic, Integer> wrappedStatistics = packet.getStatisticMaps().read(0);
        WrappedStatistic wrappedStatistic = wrappedStatistics.keySet().iterator().next();

        assertEquals(statistic.getName(), wrappedStatistic.getName());
        assertEquals(12, wrappedStatistics.get(wrappedStatistic));
    }
}
