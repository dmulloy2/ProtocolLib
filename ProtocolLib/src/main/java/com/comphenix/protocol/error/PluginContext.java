package com.comphenix.protocol.error;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.CodeSource;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

public final class PluginContext {
	// Determine plugin folder
	private static File pluginFolder;
	
	private PluginContext() {
		// Not constructable
	}
	
	/**
	 * Retrieve the name of the plugin that called the last method(s) in the exception.
	 * @param ex - the exception.
	 * @return The name of the plugin, or NULL.
	 */
	public static String getPluginCaller(Exception ex) {
		StackTraceElement[] elements = ex.getStackTrace();
		String current = getPluginName(elements[0]);
		
		for (int i = 1; i < elements.length; i++) {
			String caller = getPluginName(elements[i]);
			
			if (caller != null && !caller.equals(current)) {
				return caller;
			}
		}
		return null;
	}
	
	/**
	 * Lookup the plugin that this method invocation belongs to, and return its file name.
	 * @param element - the method invocation.
	 * @return Plugin name, or NULL if not found.
	 * 
	 */
	public static String getPluginName(StackTraceElement element) {
		try {
			if (Bukkit.getServer() == null)
				return null;
			CodeSource codeSource = Class.forName(element.getClassName()).getProtectionDomain().getCodeSource();

			if (codeSource != null) {
				String encoding = codeSource.getLocation().getPath();
				File path = new File(URLDecoder.decode(encoding, "UTF-8"));
				File plugins = getPluginFolder();
				
				if (plugins != null && folderContains(plugins, path)) {
					return path.getName();
				}
			}
			return null; // Cannot find it
			
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Cannot lookup plugin name.", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Cannot lookup plugin name.", e);
		}
	}
	
	/**
	 * Determine if a folder contains the given file.
	 * @param folder - the folder.
	 * @param file - the file.
	 * @return TRUE if it does, FALSE otherwise.
	 */
	private static boolean folderContains(File folder, File file) {
		Preconditions.checkNotNull(folder, "folder cannot be NULL");
		Preconditions.checkNotNull(file, "file cannot be NULL");
		
		// Get absolute versions
		folder = folder.getAbsoluteFile();
		file = file.getAbsoluteFile();

		while (file != null) {
		    if (folder.equals(file))
		        return true;
		    file = file.getParentFile();
		}
		return false;
	}
	
	/**
	 * Retrieve the folder that contains every plugin on the server.
	 * @return Folder with every plugin, or NULL if Bukkit has not been initialized yet.
	 */
	private static File getPluginFolder() {
		File folder = pluginFolder;
		
		if (folder == null && Bukkit.getServer() != null) { 
			Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
			
			if (plugins.length > 0) {
				folder = plugins[0].getDataFolder().getParentFile();
				pluginFolder = folder;
			}
		}
		return folder;
	}
}
