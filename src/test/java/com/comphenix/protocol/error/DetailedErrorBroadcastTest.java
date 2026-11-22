package com.comphenix.protocol.error;

import java.util.logging.Logger;

import com.comphenix.protocol.BukkitInitialization;
import com.comphenix.protocol.ProtocolConfig;
import com.comphenix.protocol.ProtocolLibrary;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class DetailedErrorBroadcastTest {

    private static final ReportType REPORT_TEST = new ReportType("Test error %s");

    @BeforeAll
    public static void beforeClass() {
        BukkitInitialization.initializeAll();
    }

    @Test
    public void testBurstOfDetailedErrorsIsRateLimitedInChat() {
        ProtocolConfig config = mock(ProtocolConfig.class);
        when(config.isChatWarnings()).thenReturn(true);
        Plugin plugin = mock(Plugin.class);
        when(plugin.getName()).thenReturn("ProtocolLib");

        DetailedErrorReporter reporter = new DetailedErrorReporter(plugin, "  ", "https://example.invalid",
                DetailedErrorReporter.DEFAULT_MAX_ERROR_COUNT, mock(Logger.class));

        try (MockedStatic<ProtocolLibrary> protocolLibrary = mockStatic(ProtocolLibrary.class, CALLS_REAL_METHODS)) {
            protocolLibrary.when(ProtocolLibrary::getConfig).thenReturn(config);
            clearInvocations(Bukkit.getServer());

            for (int i = 0; i < 30; i++) {
                reporter.reportDetailed(this, Report.newBuilder(REPORT_TEST).messageParam(i)
                        .error(new IllegalStateException("error " + i)));
            }
        }

        long broadcasts = mockingDetails(Bukkit.getServer()).getInvocations().stream()
                .filter(invocation -> invocation.getMethod().getName().equals("broadcast"))
                .count();
        assertTrue(broadcasts <= 5, "chat messages for a burst of 30 detailed errors: " + broadcasts);
    }
}
