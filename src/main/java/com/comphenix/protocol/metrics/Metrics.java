package com.comphenix.protocol.metrics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.zip.GZIPOutputStream;

/**
 * bStats collects some data for plugin authors.
 *
 * Check out https://bStats.org/ to learn more about bStats!
 */
public class Metrics {

    static {
        // You can use the property to disable the check in your test environment
        if (System.getProperty("bstats.relocatecheck") == null || !System.getProperty("bstats.relocatecheck").equals("false")) {
            // Maven's Relocate is clever and changes strings, too. So we have to use this little "trick" ... :D
            final String defaultPackage = new String(
                    new byte[]{'o', 'r', 'g', '.', 'b', 's', 't', 'a', 't', 's', '.', 'b', 'u', 'k', 'k', 'i', 't'});
            final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
            // We want to make sure nobody just copy & pastes the example and use the wrong package names
            if (Metrics.class.getPackage().getName().equals(defaultPackage) || Metrics.class.getPackage().getName().equals(examplePackage)) {
                throw new IllegalStateException("bStats Metrics class has not been relocated correctly!");
            }
        }
    }

    // The version of this bStats class
    public static final int B_STATS_VERSION = 1;

    // The url to which the data is sent
    private static final String URL = "https://bStats.org/submitData/bukkit";

    // Should failed requests be logged?
    private static boolean logFailedRequests;

    // The uuid of the server
    private static String serverUUID;

    // The plugin
    private final Plugin plugin;

    // A list with all custom charts
    private final List<CustomChart> charts = new ArrayList<>();

    /**
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public Metrics(Plugin plugin) {
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin cannot be null!");
        }
        this.plugin = plugin;

        // Get the config file
        File bStatsFolder = new File(plugin.getDataFolder().getParentFile(), "bStats");
        File configFile = new File(bStatsFolder, "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Check if the config file exists
        if (!config.isSet("serverUuid")) {

            // Add default values
            config.addDefault("enabled", true);
            // Every server gets it's unique random id.
            config.addDefault("serverUuid", UUID.randomUUID().toString());
            // Should failed request be logged?
            config.addDefault("logFailedRequests", false);

            // Inform the server owners about bStats
            config.options().header(
                    "bStats collects some data for plugin authors like how many servers are using their plugins.\n" +
                            "To honor their work, you should not disable it.\n" +
                            "This has nearly no effect on the server performance!\n" +
                            "Check out https://bStats.org/ to learn more :)"
            ).copyDefaults(true);
            try {
                config.save(configFile);
            } catch (IOException ignored) { }
        }

        // Load the data
        serverUUID = config.getString("serverUuid");
        logFailedRequests = config.getBoolean("logFailedRequests", false);
        if (config.getBoolean("enabled", true)) {
            boolean found = false;
            // Search for all other bStats Metrics classes to see if we are the first one
            for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
                try {
                    service.getField("B_STATS_VERSION"); // Our identifier :)
                    found = true; // We aren't the first
                    break;
                } catch (NoSuchFieldException ignored) { }
            }
            // Register our service
            Bukkit.getServicesManager().register(Metrics.class, this, plugin, ServicePriority.Normal);
            if (!found) {
                // We are the first!
                startSubmitting();
            }
        }
    }

    void logFailedRequests(boolean value) {
        logFailedRequests = value;
    }

    /**
     * Adds a custom chart.
     *
     * @param chart The chart to add.
     */
    public void addCustomChart(CustomChart chart) {
        if (chart == null) {
            throw new IllegalArgumentException("Chart cannot be null!");
        }
        charts.add(chart);
    }

    /**
     * Starts the Scheduler which submits our data every 30 minutes.
     */
    private void startSubmitting() {
        final Timer timer = new Timer(true); // We use a timer cause the Bukkit scheduler is affected by server lags
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!plugin.isEnabled()) { // Plugin was disabled
                    timer.cancel();
                    return;
                }
                // Nevertheless we want our code to run in the Bukkit main thread, so we have to use the Bukkit scheduler
                // Don't be afraid! The connection to the bStats server is still async, only the stats collection is sync ;)
                Bukkit.getScheduler().runTask(plugin, Metrics.this::submitData);
            }
        }, 1000L * 60 * 5, 1000L * 60 * 30);
        // Submit the data every 30 minutes, first time after 5 minutes to give other plugins enough time to start
        // WARNING: Changing the frequency has no effect but your plugin WILL be blocked/deleted!
        // WARNING: Just don't do it!
    }

    /**
     * Gets the plugin specific data.
     * This method is called using Reflection.
     *
     * @return The plugin specific data.
     */
    public JSONObject getPluginData() {
        JSONObject data = new JSONObject();

        String pluginName = plugin.getDescription().getName();
        String pluginVersion = Statistics.getVersion(); // ProtocolLib - remove build number

        data.put("pluginName", pluginName); // Append the name of the plugin
        data.put("pluginVersion", pluginVersion); // Append the version of the plugin
        JSONArray customCharts = new JSONArray();
        for (CustomChart customChart : charts) {
            // Add the data of the custom charts
            JSONObject chart = customChart.getRequestJsonObject();
            if (chart == null) { // If the chart is null, we skip it
                continue;
            }
            customCharts.add(chart);
        }
        data.put("customCharts", customCharts);

        return data;
    }

    /**
     * Gets the server specific data.
     *
     * @return The server specific data.
     */
    private JSONObject getServerData() {
        // Minecraft specific data
        int playerAmount;
        try {
            // Around MC 1.8 the return type was changed to a collection from an array,
            // This fixes java.lang.NoSuchMethodError: org.bukkit.Bukkit.getOnlinePlayers()Ljava/util/Collection;
            Method onlinePlayersMethod = Class.forName("org.bukkit.Server").getMethod("getOnlinePlayers");
            playerAmount = onlinePlayersMethod.getReturnType().equals(Collection.class)
                    ? ((Collection<?>) onlinePlayersMethod.invoke(Bukkit.getServer())).size()
                    : ((Player[]) onlinePlayersMethod.invoke(Bukkit.getServer())).length;
        } catch (Exception e) {
            playerAmount = Bukkit.getOnlinePlayers().size(); // Just use the new method if the Reflection failed
        }
        int onlineMode = Bukkit.getOnlineMode() ? 1 : 0;
        String bukkitVersion = org.bukkit.Bukkit.getVersion();
        bukkitVersion = bukkitVersion.substring(bukkitVersion.indexOf("MC: ") + 4, bukkitVersion.length() - 1);

        // OS/Java specific data
        String javaVersion = System.getProperty("java.version");
        String osName = System.getProperty("os.name");
        String osArch = System.getProperty("os.arch");
        String osVersion = System.getProperty("os.version");
        int coreCount = Runtime.getRuntime().availableProcessors();

        JSONObject data = new JSONObject();

        data.put("serverUUID", serverUUID);

        data.put("playerAmount", playerAmount);
        data.put("onlineMode", onlineMode);
        data.put("bukkitVersion", bukkitVersion);

        data.put("javaVersion", javaVersion);
        data.put("osName", osName);
        data.put("osArch", osArch);
        data.put("osVersion", osVersion);
        data.put("coreCount", coreCount);

        return data;
    }

    /**
     * Collects the data and sends it afterwards.
     */
    private void submitData() {
        final JSONObject data = getServerData();

        JSONArray pluginData = new JSONArray();
        // Search for all other bStats Metrics classes to get their plugin data
        for (Class<?> service : Bukkit.getServicesManager().getKnownServices()) {
            try {
                service.getField("B_STATS_VERSION"); // Our identifier :)

                for (RegisteredServiceProvider<?> provider : Bukkit.getServicesManager().getRegistrations(service)) {
                    try {
                        pluginData.add(provider.getService().getMethod("getPluginData").invoke(provider.getProvider()));
                    } catch (Throwable ignored) { }
                }
            } catch (Throwable ignored) { }
        }

        data.put("plugins", pluginData);

        // Create a new thread for the connection to the bStats server
        new Thread(() -> {
            try {
                // Send the data
                sendData(data);
            } catch (Exception e) {
                // Something went wrong! :(
                if (logFailedRequests) {
                    plugin.getLogger().log(Level.WARNING, "Could not submit plugin stats of " + plugin.getName(), e);
                }
            }
        }).start();
    }

    /**
     * Sends the data to the bStats server.
     *
     * @param data The data to send.
     * @throws Exception If the request failed.
     */
    private static void sendData(JSONObject data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null!");
        }
        if (Bukkit.isPrimaryThread()) {
            throw new IllegalAccessException("This method must not be called from the main thread!");
        }
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL).openConnection();

        // Compress the data to save bandwidth
        byte[] compressedData = compress(data.toString());

        // Add headers
        connection.setRequestMethod("POST");
        connection.addRequestProperty("Accept", "application/json");
        connection.addRequestProperty("Connection", "close");
        connection.addRequestProperty("Content-Encoding", "gzip"); // We gzip our request
        connection.addRequestProperty("Content-Length", String.valueOf(compressedData.length));
        connection.setRequestProperty("Content-Type", "application/json"); // We send our data in JSON format
        connection.setRequestProperty("User-Agent", "MC-Server/" + B_STATS_VERSION);

        // Send data
        connection.setDoOutput(true);
        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.write(compressedData);
        outputStream.flush();
        outputStream.close();

        connection.getInputStream().close(); // We don't care about the response - Just send our data :)
    }

    /**
     * Gzips the given String.
     *
     * @param str The string to gzip.
     * @return The gzipped String.
     * @throws IOException If the compression failed.
     */
    private static byte[] compress(final String str) throws IOException {
        if (str == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(outputStream);
        gzip.write(str.getBytes(StandardCharsets.UTF_8));
        gzip.close();
        return outputStream.toByteArray();
    }

    /**
     * Represents a custom chart.
     */
    public static abstract class CustomChart {

        // The id of the chart
        final String chartId;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         */
        CustomChart(String chartId) {
            if (chartId == null || chartId.isEmpty()) {
                throw new IllegalArgumentException("ChartId cannot be null or empty!");
            }
            this.chartId = chartId;
        }

        private JSONObject getRequestJsonObject() {
            JSONObject chart = new JSONObject();
            chart.put("chartId", chartId);
            try {
                JSONObject data = getChartData();
                if (data == null) {
                    // If the data is null we don't send the chart.
                    return null;
                }
                chart.put("data", data);
            } catch (Throwable t) {
                if (logFailedRequests) {
                    Bukkit.getLogger().log(Level.WARNING, "Failed to get data for custom chart with id " + chartId, t);
                }
                return null;
            }
            return chart;
        }

        protected abstract JSONObject getChartData() throws Exception;

    }

    /**
     * Represents a custom simple pie.
     */
    public static class SimplePie extends CustomChart {

        private final Callable<String> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SimplePie(String chartId, Callable<String> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            String value = callable.call();
            if (value == null || value.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            data.put("value", value);
            return data;
        }
    }

    /**
     * Represents a custom advanced pie.
     */
    public static class AdvancedPie extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public AdvancedPie(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    /**
     * Represents a custom drilldown pie.
     */
    public static class DrilldownPie extends CustomChart {

        private final Callable<Map<String, Map<String, Integer>>> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public DrilldownPie(String chartId, Callable<Map<String, Map<String, Integer>>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        public JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            Map<String, Map<String, Integer>> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean reallyAllSkipped = true;
            for (Map.Entry<String, Map<String, Integer>> entryValues : map.entrySet()) {
                JSONObject value = new JSONObject();
                boolean allSkipped = true;
                for (Map.Entry<String, Integer> valueEntry : map.get(entryValues.getKey()).entrySet()) {
                    value.put(valueEntry.getKey(), valueEntry.getValue());
                    allSkipped = false;
                }
                if (!allSkipped) {
                    reallyAllSkipped = false;
                    values.put(entryValues.getKey(), value);
                }
            }
            if (reallyAllSkipped) {
                // Null = skip the chart
                return null;
            }
            data.put("values", values);
            return data;
        }
    }

    /**
     * Represents a custom single line chart.
     */
    public static class SingleLineChart extends CustomChart {

        private final Callable<Integer> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SingleLineChart(String chartId, Callable<Integer> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            int value = callable.call();
            if (value == 0) {
                // Null = skip the chart
                return null;
            }
            data.put("value", value);
            return data;
        }

    }

    /**
     * Represents a custom multi line chart.
     */
    public static class MultiLineChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public MultiLineChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                if (entry.getValue() == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                values.put(entry.getKey(), entry.getValue());
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.put("values", values);
            return data;
        }

    }

    /**
     * Represents a custom simple bar chart.
     */
    public static class SimpleBarChart extends CustomChart {

        private final Callable<Map<String, Integer>> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public SimpleBarChart(String chartId, Callable<Map<String, Integer>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            Map<String, Integer> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                JSONArray categoryValues = new JSONArray();
                categoryValues.add(entry.getValue());
                values.put(entry.getKey(), categoryValues);
            }
            data.put("values", values);
            return data;
        }

    }

    /**
     * Represents a custom advanced bar chart.
     */
    public static class AdvancedBarChart extends CustomChart {

        private final Callable<Map<String, int[]>> callable;

        /**
         * Class constructor.
         *
         * @param chartId The id of the chart.
         * @param callable The callable which is used to request the chart data.
         */
        public AdvancedBarChart(String chartId, Callable<Map<String, int[]>> callable) {
            super(chartId);
            this.callable = callable;
        }

        @Override
        protected JSONObject getChartData() throws Exception {
            JSONObject data = new JSONObject();
            JSONObject values = new JSONObject();
            Map<String, int[]> map = callable.call();
            if (map == null || map.isEmpty()) {
                // Null = skip the chart
                return null;
            }
            boolean allSkipped = true;
            for (Map.Entry<String, int[]> entry : map.entrySet()) {
                if (entry.getValue().length == 0) {
                    continue; // Skip this invalid
                }
                allSkipped = false;
                JSONArray categoryValues = new JSONArray();
                for (int categoryValue : entry.getValue()) {
                    categoryValues.add(categoryValue);
                }
                values.put(entry.getKey(), categoryValues);
            }
            if (allSkipped) {
                // Null = skip the chart
                return null;
            }
            data.put("values", values);
            return data;
        }

    }
}
