/*
 * Updater for Bukkit.
 *
 * This class provides the means to safely and easily update a plugin, or check to see if it is updated using dev.bukkit.org
 */

// Somewhat modified by aadnk.
package com.comphenix.protocol.metrics;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * Check dev.bukkit.org to find updates for a given plugin, and download the updates if needed.
 * <p/>
 * <b>VERY, VERY IMPORTANT</b>: Because there are no standards for adding auto-update toggles in your plugin's config, this system provides NO CHECK WITH YOUR CONFIG to make sure the user has allowed auto-updating.
 * <br>
 * It is a <b>BUKKIT POLICY</b> that you include a boolean value in your config that prevents the auto-updater from running <b>AT ALL</b>.
 * <br>
 * If you fail to include this option in your config, your plugin will be <b>REJECTED</b> when you attempt to submit it to dev.bukkit.org.
 * <p/>
 * An example of a good configuration option would be something similar to 'auto-update: true' - if this value is set to false you may NOT run the auto-updater.
 * <br>
 * If you are unsure about these rules, please read the plugin submission guidelines: http://goo.gl/8iU5l
 *
 * @author Gravity
 * @version 2.0
 */

public class Updater {
	public static final ReportType REPORT_CANNOT_UPDATE_PLUGIN = new ReportType("Cannot update ProtocolLib.");
	
    private Plugin plugin;
    private UpdateType type;
    private String versionName;
    private String versionLink;
    private String versionType;
    private String versionGameVersion;
	private String versionFileName;
    
    private boolean announce; // Whether to announce file downloads

    private URL url; // Connecting to RSS
    private File file; // The plugin's file
    private Thread thread; // Updater thread

    private int id = -1; // Project's Curse ID
    private String apiKey = null; // BukkitDev ServerMods API key
    private static final String TITLE_VALUE = "name"; 			// Gets remote file's title
    private static final String LINK_VALUE = "downloadUrl"; 	// Gets remote file's download link
    private static final String TYPE_VALUE = "releaseType"; 	// Gets remote file's release type
    private static final String VERSION_VALUE = "gameVersion";  // Gets remote file's build version
	private static final Object FILE_NAME = "fileName";			// Gets remote file's name
    private static final String QUERY = "/servermods/files?projectIds="; // Path to GET
    private static final String HOST = "https://api.curseforge.com"; // Slugs will be appended to this to get to the project's RSS feed

    private static final String[] NO_UPDATE_TAG = { "-DEV", "-PRE", "-SNAPSHOT" }; // If the version number contains one of these, don't update.
    private static final int BYTE_SIZE = 1024; // Used for downloading files

    private YamlConfiguration config; // Config file
    private String updateFolder;// The folder that downloads will be placed in
    private Updater.UpdateResult result = Updater.UpdateResult.SUCCESS; // Used for determining the outcome of the update process
    private List<Runnable> listeners = new CopyOnWriteArrayList<Runnable>();
    
    /**
     * Gives the dev the result of the update process. Can be obtained by called getResult().
     */
    public enum UpdateResult {
        /**
         * The updater found an update, and has readied it to be loaded the next time the server restarts/reloads.
         */
        SUCCESS("The updater found an update, and has readied it to be loaded the next time the server restarts/reloads."),
        
        /**
         * The updater did not find an update, and nothing was downloaded.
         */
        NO_UPDATE("The updater did not find an update, and nothing was downloaded."),
        
        /**
         * The server administrator has disabled the updating system
         */
        DISABLED("The server administrator has disabled the updating system"),
        
        /**
         * The updater found an update, but was unable to download it.
         */
        FAIL_DOWNLOAD("The updater found an update, but was unable to download it."),
        
        /**
         * For some reason, the updater was unable to contact dev.bukkit.org to download the file.
         */
        FAIL_DBO("For some reason, the updater was unable to contact dev.bukkit.org to download the file.")
        ,
        /**
         * When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'.
         */
        FAIL_NOVERSION("When running the version check, the file on DBO did not contain the a version in the format 'vVersion' such as 'v1.0'."),
        
        /**
         * The id provided by the plugin running the updater was invalid and doesn't exist on DBO.
         */
        FAIL_BADID("The id provided by the plugin running the updater was invalid and doesn't exist on DBO."),
        
        /**
         * The server administrator has improperly configured their API key in the configuration
         */
        FAIL_APIKEY("The server administrator has improperly configured their API key in the configuration"),
        
        /**
         * The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.
         */
        UPDATE_AVAILABLE("The updater found an update, but because of the UpdateType being set to NO_DOWNLOAD, it wasn't downloaded.");
        
        private final String description;
        
        private UpdateResult(String description) {
            this.description = description;
        }
        
        @Override
        public String toString() {
        	return description;
        }
    }

    /**
     * Allows the dev to specify the type of update that will be run.
     */
    public enum UpdateType {
        /**
         * Run a version check, and then if the file is out of date, download the newest version.
         */
        DEFAULT,
        /**
         * Don't run a version check, just find the latest update and download it.
         */
        NO_VERSION_CHECK,
        /**
         * Get information about the version and the download size, but don't actually download anything.
         */
        NO_DOWNLOAD
    }

    /**
     * Initialize the updater.
     * <p>
     * Call {@link #start()} to actually start looking (and downloading) updates.
     *
     * @param plugin   The plugin that is checking for an update.
     * @param id       The dev.bukkit.org id of the project
     * @param file     The file that the plugin is running from, get this by doing this.getFile() from within your main class.
     * @param type     Specify the type of update this will be. See {@link UpdateType}
     * @param announce True if the program should announce the progress of new updates in console
     */
    public Updater(Plugin plugin, int id, File file, UpdateType type, boolean announce) {
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        this.id = id;
        this.updateFolder = plugin.getServer().getUpdateFolder();

        final File pluginFile = plugin.getDataFolder().getParentFile();
        final File updaterFile = new File(pluginFile, "Updater");
        final File updaterConfigFile = new File(updaterFile, "config.yml");

        if (!updaterFile.exists()) {
            updaterFile.mkdir();
        }
        if (!updaterConfigFile.exists()) {
            try {
                updaterConfigFile.createNewFile();
            } catch (final IOException e) {
                plugin.getLogger().severe("The updater could not create a configuration in " + updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(updaterConfigFile);

        this.config.options().header("This configuration file affects all plugins using the Updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )" + '\n'
                + "If you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below." + '\n'
                + "Some updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
        this.config.addDefault("api-key", "PUT_API_KEY_HERE");
        this.config.addDefault("disable", false);

        if (this.config.get("api-key", null) == null) {
            this.config.options().copyDefaults(true);
            try {
                this.config.save(updaterConfigFile);
            } catch (final IOException e) {
                plugin.getLogger().severe("The updater could not save the configuration in " + updaterFile.getAbsolutePath());
                e.printStackTrace();
            }
        }

        if (this.config.getBoolean("disable")) {
            this.result = UpdateResult.DISABLED;
            return;
        }

        String key = this.config.getString("api-key");
        if (key.equalsIgnoreCase("PUT_API_KEY_HERE") || key.equals("")) {
            key = null;
        }

        this.apiKey = key;

        try {
            this.url = new URL(Updater.HOST + Updater.QUERY + id);
        } catch (final MalformedURLException e) {
            plugin.getLogger().severe("The project ID provided for updating, " + id + " is invalid.");
            this.result = UpdateResult.FAIL_BADID;
            e.printStackTrace();
        }
    }
    
    // aadnk - decouple the thread start and the constructor.
    /**
     * Begin looking for updates.
     * @param type - the update type.
     */
    public void start(UpdateType type) {
    	waitForThread();
    	
    	this.type = type;
        this.thread = new Thread(new UpdateRunnable());
        this.thread.start();
    }

    /**
     * Add a listener to be executed when we have determined if an update is available.
     * <p>
     * The listener will be executed on the main thread.
     * @param listener - the listener to add.
     */
    public void addListener(Runnable listener) {
    	listeners.add(Preconditions.checkNotNull(listener, "listener cannot be NULL"));
    }
    
    /**
     * Remove a listener.
     * @param listener - the listener to remove.
     * @return TRUE if the listener was removed, FALSE otherwise.
     */
    public boolean removeListener(Runnable listener) {
    	return listeners.remove(listener);
    }
    
    /**
     * Get the result of the update process.
     */
    public Updater.UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }

    /**
     * Get the latest version's release type (release, beta, or alpha).
     */
    public String getLatestType() {
        this.waitForThread();
        return this.versionType;
    }

    /**
     * Get the latest version's game version.
     */
    public String getLatestGameVersion() {
        this.waitForThread();
        return this.versionGameVersion;
    }

    /**
     * Get the latest version's name.
     */
    public String getLatestName() {
        this.waitForThread();
        return this.versionName;
    }

    /**
     * Get the latest version's file link.
     */
    public String getLatestFileLink() {
        this.waitForThread();
        return this.versionLink;
    }

    /**
     * As the result of Updater output depends on the thread's completion, it is necessary to wait for the thread to finish
     * before allowing anyone to check the result.
     */
    private void waitForThread() {
        if ((this.thread != null) && this.thread.isAlive()) {
            try {
                this.thread.join();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Determine if we are already checking for an update.
     * @return TRUE if we are, FALSE otherwise.
     */
    public boolean isChecking() {
    	return this.thread != null && this.thread.isAlive();
    }
    
    /**
     * Save an update from dev.bukkit.org into the server's update folder.
     */
    private void saveFile(File folder, String file, String u) {
        if (!folder.exists()) {
            folder.mkdir();
        }
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            // Download the file
            final URL url = new URL(u);
            final int fileLength = url.openConnection().getContentLength();
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(folder.getAbsolutePath() + "/" + file);

            final byte[] data = new byte[Updater.BYTE_SIZE];
            int count;
            if (this.announce) {
                this.plugin.getLogger().info("About to download a new update: " + this.versionName);
            }
            long downloaded = 0;
            while ((count = in.read(data, 0, Updater.BYTE_SIZE)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                final int percent = (int) ((downloaded * 100) / fileLength);
                if (this.announce && ((percent % 10) == 0)) {
                    this.plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }
            //Just a quick check to make sure we didn't leave any files from last time...
            for (final File xFile : new File(this.plugin.getDataFolder().getParent(), this.updateFolder).listFiles()) {
                if (xFile.getName().endsWith(".zip")) {
                    xFile.delete();
                }
            }
            // Check to see if it's a zip file, if it is, unzip it.
            final File dFile = new File(folder.getAbsolutePath() + "/" + file);
            if (dFile.getName().endsWith(".zip")) {
                // Unzip
                this.unzip(dFile.getCanonicalPath());
            }
            if (this.announce) {
                this.plugin.getLogger().info("Finished updating.");
            }
        } catch (final Exception ex) {
            this.plugin.getLogger().warning("The auto-updater tried to download a new update, but was unsuccessful.");
            this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (fout != null) {
                    fout.close();
                }
            } catch (final Exception ex) {
            }
        }
    }

    /**
     * Part of Zip-File-Extractor, modified by Gravity for use with Bukkit
     */
    private void unzip(String file) {
        try {
            final File fSourceZip = new File(file);
            final String zipPath = file.substring(0, file.length() - 4);
            ZipFile zipFile = new ZipFile(fSourceZip);
            Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = e.nextElement();
                File destinationFilePath = new File(zipPath, entry.getName());
                destinationFilePath.getParentFile().mkdirs();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    final byte buffer[] = new byte[Updater.BYTE_SIZE];
                    final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos, Updater.BYTE_SIZE);
                    while ((b = bis.read(buffer, 0, Updater.BYTE_SIZE)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    final String name = destinationFilePath.getName();
                    if (name.endsWith(".jar") && this.pluginFile(name)) {
                        destinationFilePath.renameTo(new File(this.plugin.getDataFolder().getParent(), this.updateFolder + "/" + name));
                    }
                }
                entry = null;
                destinationFilePath = null;
            }
            e = null;
            zipFile.close();
            zipFile = null;

            // Move any plugin data folders that were included to the right place, Bukkit won't do this for us.
            for (final File dFile : new File(zipPath).listFiles()) {
                if (dFile.isDirectory()) {
                    if (this.pluginFile(dFile.getName())) {
                        final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName()); // Get current dir
                        final File[] contents = oFile.listFiles(); // List of existing files in the current dir
                        for (final File cFile : dFile.listFiles()) // Loop through all the files in the new dir
                        {
                            boolean found = false;
                            for (final File xFile : contents) // Loop through contents to see if it exists
                            {
                                if (xFile.getName().equals(cFile.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Move the new file into the current dir
                                cFile.renameTo(new File(oFile.getCanonicalFile() + "/" + cFile.getName()));
                            } else {
                                // This file already exists, so we don't need it anymore.
                                cFile.delete();
                            }
                        }
                    }
                }
                dFile.delete();
            }
            new File(zipPath).delete();
            fSourceZip.delete();
        } catch (final IOException ex) {
            this.plugin.getLogger().warning("The auto-updater tried to unzip a new update file, but was unsuccessful.");
            this.result = Updater.UpdateResult.FAIL_DOWNLOAD;
            ex.printStackTrace();
        }
        new File(file).delete();
    }

    /**
     * Check if the name of a jar is one of the plugins currently installed, used for extracting the correct files out of a zip.
     */
    private boolean pluginFile(String name) {
        for (final File file : new File("plugins").listFiles()) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check to see if the program should continue by evaluation whether the plugin is already updated, or shouldn't be updated
     */
    private boolean versionCheck(String title) {
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            final String version = this.plugin.getDescription().getVersion();
            final String[] splitTitle = title.split(" ");
            
            // aadnk - expect a Maven-style version format (ARTIFACT 1.2.3-SNAPSHOT).
			if (splitTitle.length == 2) {
				// Get the newest file's version number
                final String remoteVersion = splitTitle[1].split("-")[0]; 
                
                // Parse the version
                MinecraftVersion parsedRemote = new MinecraftVersion(remoteVersion);
                MinecraftVersion parsedCurrent = new MinecraftVersion(plugin.getDescription().getVersion());

                // The remote version has to be greater
                if (this.hasTag(version) || parsedRemote.compareTo(parsedCurrent) <= 0) {
                    // We already have the latest version, or this build is tagged for no-update
                    this.result = Updater.UpdateResult.NO_UPDATE;
                    return false;
                }
            } else {
                // The file's name did not contain the string 'vVersion'
                final String authorInfo = this.plugin.getDescription().getAuthors().size() == 0 ? "" : " (" + this.plugin.getDescription().getAuthors().get(0) + ")";
                this.plugin.getLogger().warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
                this.plugin.getLogger().warning("File versions should follow the format 'PluginName VERSION[-SNAPSHOT]'");
                this.plugin.getLogger().warning("Please notify the author of this error.");
                this.result = Updater.UpdateResult.FAIL_NOVERSION;
                return false;
            }
        }
        return true;
    }

    /**
     * Evaluate whether the version number is marked showing that it should not be updated by this program
     */
    private boolean hasTag(String version) {
        for (final String string : Updater.NO_UPDATE_TAG) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }

    private boolean read() {
        try {
            final URLConnection conn = this.url.openConnection();
            conn.setConnectTimeout(5000);

            if (this.apiKey != null) {
                conn.addRequestProperty("X-API-Key", this.apiKey);
            }
            conn.addRequestProperty("User-Agent", "Updater (by Gravity)");
            conn.setDoOutput(true);

            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();

            final JSONArray array = (JSONArray) JSONValue.parse(response);

            if (array.size() == 0) {
                this.plugin.getLogger().warning("The updater could not find any files for the project id " + this.id);
                this.result = UpdateResult.FAIL_BADID;
                return false;
            }

            final JSONObject jsonObject = (JSONObject) array.get(array.size() - 1);
            this.versionFileName = (String) jsonObject.get(Updater.FILE_NAME);
			this.versionName = (String) jsonObject.get(Updater.TITLE_VALUE);
            this.versionLink = (String) jsonObject.get(Updater.LINK_VALUE);
            this.versionType = (String) jsonObject.get(Updater.TYPE_VALUE);
            this.versionGameVersion = (String) jsonObject.get(Updater.VERSION_VALUE);

            return true;
        } catch (final IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                this.plugin.getLogger().warning("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
                this.plugin.getLogger().warning("Please double-check your configuration to ensure it is correct.");
                this.result = UpdateResult.FAIL_APIKEY;
            } else {
                this.plugin.getLogger().warning("The updater could not contact dev.bukkit.org for updating.");
                this.plugin.getLogger().warning("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                this.result = UpdateResult.FAIL_DBO;
            }
            e.printStackTrace();
            return false;
        }
    }

    // aadnk - added listeners
    private class UpdateRunnable implements Runnable {
        @Override
        public void run() {
        	try {
	            if (Updater.this.url != null) {
	                // Obtain the results of the project's file feed
	                if (Updater.this.read()) {
	                    if (Updater.this.versionCheck(Updater.this.versionName)) {
	                        performUpdate();
	                    }
	                }
	            }
        	} catch (Exception e) {
        		// Any generic error will be handled here
				ProtocolLibrary.getErrorReporter().reportDetailed(
					Updater.this, Report.newBuilder(REPORT_CANNOT_UPDATE_PLUGIN).error(e).callerParam(this));
	            
        	} finally {
        		// Invoke the listeners on the main thread
        		for (Runnable listener : listeners) {
        			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, listener);
        		}
        	}
        }

		private void performUpdate() {
			if ((Updater.this.versionLink != null) && (Updater.this.type != UpdateType.NO_DOWNLOAD)) {
			    final File pluginFolder = plugin.getDataFolder().getParentFile();
				File destinationFolder = new File(pluginFolder, updateFolder);
			    String name = Updater.this.file.getName(); 
			    
			    // If it's a zip file, it shouldn't be downloaded as the plugin's name
			    if (Updater.this.versionLink.endsWith(".zip")) {
			        name = versionFileName;
			    }	    
				Updater.this.saveFile(
			    	destinationFolder, 
			    	name, 
			    	Updater.this.versionLink
			    );
			} else {
			    Updater.this.result = UpdateResult.UPDATE_AVAILABLE;
			}
		}
    }
}
