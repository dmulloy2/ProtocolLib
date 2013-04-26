/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the 
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 *  02111-1307 USA
 */

package com.comphenix.protocol.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.Report.ReportBuilder;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.google.common.primitives.Primitives;

/**
 * Internal class used to handle exceptions.
 * 
 * @author Kristian
 */
public class DetailedErrorReporter implements ErrorReporter {
	/**
	 * Report format for printing the current exception count. 
	 */
	public static final ReportType REPORT_EXCEPTION_COUNT = new ReportType("Internal exception count: %s!");
	
	public static final String SECOND_LEVEL_PREFIX = "  ";
	public static final String DEFAULT_PREFIX = "  ";
	public static final String DEFAULT_SUPPORT_URL = "http://dev.bukkit.org/server-mods/protocollib/";

	// Users that are informed about errors in the chat
	public static final String ERROR_PERMISSION = "protocol.info";
	
	// We don't want to spam the server
	public static final int DEFAULT_MAX_ERROR_COUNT = 20;
	
	// Prevent spam per plugin too
	private ConcurrentMap<String, AtomicInteger> warningCount = new ConcurrentHashMap<String, AtomicInteger>();
	
	protected String prefix;
	protected String supportURL;
	
	protected AtomicInteger internalErrorCount = new AtomicInteger();
	
	protected int maxErrorCount;
	protected Logger logger;
	
	protected WeakReference<Plugin> pluginReference;
	protected String pluginName;
	
	// Whether or not Apache Commons is not present
	protected boolean apacheCommonsMissing;
	
	// Map of global objects
	protected Map<String, Object> globalParameters = new HashMap<String, Object>();

	/**
	 * Create a default error reporting system.
	 */
	public DetailedErrorReporter(Plugin plugin) {
		this(plugin, DEFAULT_PREFIX, DEFAULT_SUPPORT_URL);
	}
	
	/**
	 * Create a central error reporting system.
	 * @param plugin - the plugin owner.
	 * @param prefix - default line prefix.
	 * @param supportURL - URL to report the error.
	 */
	public DetailedErrorReporter(Plugin plugin, String prefix, String supportURL) {
		this(plugin, prefix, supportURL, DEFAULT_MAX_ERROR_COUNT, getBukkitLogger());
	}

	/**
	 * Create a central error reporting system.
	 * @param plugin - the plugin owner.
	 * @param prefix - default line prefix.
	 * @param supportURL - URL to report the error.
	 * @param maxErrorCount - number of errors to print before giving up.
	 * @param logger - current logger.
	 */
	public DetailedErrorReporter(Plugin plugin, String prefix, String supportURL, int maxErrorCount, Logger logger) {
		if (plugin == null)
			throw new IllegalArgumentException("Plugin cannot be NULL.");
		
		this.pluginReference = new WeakReference<Plugin>(plugin);
		this.pluginName = plugin.getName();
		this.prefix = prefix;
		this.supportURL = supportURL;
		this.maxErrorCount = maxErrorCount;
		this.logger = logger;
	}

	// Attempt to get the logger.
	private static Logger getBukkitLogger() {
		try {
			return Bukkit.getLogger();
		} catch (Throwable e) {
			return Logger.getLogger("Minecraft");
		}
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
		if (reportMinimalNoSpam(sender, methodName, error)) {
			// Print parameters, if they are given
			if (parameters != null && parameters.length > 0) {
				logger.log(Level.SEVERE, printParameters(parameters));
			}
		}
	}
	
	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error) {
		reportMinimalNoSpam(sender, methodName, error);
	}
	
	public boolean reportMinimalNoSpam(Plugin sender, String methodName, Throwable error) {
		String pluginName = PacketAdapter.getPluginName(sender);
		AtomicInteger counter = warningCount.get(pluginName);
		
		// Thread safe pattern
		if (counter == null) {
			AtomicInteger created = new AtomicInteger();
			counter = warningCount.putIfAbsent(pluginName, created);
			
			if (counter == null) {
				counter = created;
			}
		}
		
		final int errorCount = counter.incrementAndGet();
		
		// See if we should print the full error
		if (errorCount < getMaxErrorCount()) {
			logger.log(Level.SEVERE, "[" + pluginName + "] Unhandled exception occured in " +
					 methodName + " for " + pluginName, error);
			return true;
			
		} else {
			// Nope - only print the error count occationally
			if (isPowerOfTwo(errorCount)) {
				logger.log(Level.SEVERE, "[" + pluginName + "] Unhandled exception number " + errorCount + " occured in " +
						 methodName + " for " + pluginName, error);
			}
			return false;
		}
	}
	
	/**
	 * Determine if a given number is a power of two.
	 * <p>
	 * That is, if there exists an N such that 2^N = number.
	 * @param number - the number to check.
	 * @return TRUE if the given number is a power of two, FALSE otherwise.
	 */
	private boolean isPowerOfTwo(int number) {
	    return (number & (number - 1)) == 0;
	}
	
	@Override
	public void reportWarning(Object sender, ReportBuilder reportBuilder) {
		if (reportBuilder == null)
			throw new IllegalArgumentException("reportBuilder cannot be NULL.");
		
		reportWarning(sender, reportBuilder.build());
	}
	
	@Override
	public void reportWarning(Object sender, Report report) {
		String message = "[" + pluginName + "] [" + getSenderName(sender) + "] " + report.getReportMessage();
		
		// Print the main warning
		if (report.getException() != null) {
			logger.log(Level.WARNING, message, report.getException());
		} else {
			logger.log(Level.WARNING, message);
		}
		
		// Parameters?
		if (report.hasCallerParameters()) {
			// Write it
			logger.log(Level.WARNING, printParameters(report.getCallerParameters()));
		}
	}
	
	/**
	 * Retrieve the name of a sender class.
	 * @param sender - sender object.
	 * @return The name of the sender's class.
	 */
	private String getSenderName(Object sender) {
		if (sender != null)
			return sender.getClass().getSimpleName();
		else
			return "NULL";
	}
	
	@Override
	public void reportDetailed(Object sender, ReportBuilder reportBuilder) {
		reportDetailed(sender, reportBuilder.build());
	}
	
	@Override
	public void reportDetailed(Object sender, Report report) {
		final Plugin plugin = pluginReference.get();
		final int errorCount = internalErrorCount.incrementAndGet();
		
		// Do not overtly spam the server!
		if (errorCount > getMaxErrorCount()) {
			// Only allow the error count at rare occations
			if (isPowerOfTwo(errorCount)) {
				// Permit it - but print the number of exceptions first
				reportWarning(this, Report.newBuilder(REPORT_EXCEPTION_COUNT).messageParam(errorCount).build());
			} else {
				// NEVER SPAM THE CONSOLE
				return;
			}
		}
		
		StringWriter text = new StringWriter();
		PrintWriter writer = new PrintWriter(text);

		// Helpful message
		writer.println("[" + pluginName + "] INTERNAL ERROR: " + report.getReportMessage());
	    writer.println("If this problem hasn't already been reported, please open a ticket");
	    writer.println("at " + supportURL + " with the following data:");
	    
	    // Now, let us print important exception information
		writer.println("          ===== STACK TRACE =====");

		if (report.getException() != null) {
			report.getException().printStackTrace(writer);
		}
		
		// Data dump!
		writer.println("          ===== DUMP =====");
		
		// Relevant parameters
		if (report.hasCallerParameters()) {
			printParameters(writer, report.getCallerParameters());
		}
		
		// Global parameters
		for (String param : globalParameters()) {
			writer.println(SECOND_LEVEL_PREFIX + param + ":");
			writer.println(addPrefix(getStringDescription(getGlobalParameter(param)), 
					SECOND_LEVEL_PREFIX + SECOND_LEVEL_PREFIX));
		}
		
		// Now, for the sender itself
		writer.println("Sender:");
		writer.println(addPrefix(getStringDescription(sender), SECOND_LEVEL_PREFIX));
		
		// And plugin
		if (plugin != null) {
			writer.println("Version:");
			writer.println(addPrefix(plugin.toString(), SECOND_LEVEL_PREFIX));
		}
		
		// Add the server version too
		if (Bukkit.getServer() != null) {
			writer.println("Server:");
			writer.println(addPrefix(Bukkit.getServer().getVersion(), SECOND_LEVEL_PREFIX));
			
			// Inform of this occurrence
			if (ERROR_PERMISSION != null) {	
				Bukkit.getServer().broadcast(
						String.format("Error %s (%s) occured in %s.", report.getReportMessage(), report.getException(), sender),
						ERROR_PERMISSION
				);
			}
		}
		
		// Make sure it is reported
		logger.severe(addPrefix(text.toString(), prefix));
	}
	
	private String printParameters(Object... parameters) {
		StringWriter writer = new StringWriter();
		
		// Print and retrieve the string buffer
		printParameters(new PrintWriter(writer), parameters);
		return writer.toString();
	}

	private void printParameters(PrintWriter writer, Object[] parameters) {
		writer.println("Parameters: ");
		
		// We *really* want to get as much information as possible
		for (Object param : parameters) {
			writer.println(addPrefix(getStringDescription(param), SECOND_LEVEL_PREFIX));
		}
	}
	
	/**
	 * Adds the given prefix to every line in the text.
	 * @param text - text to modify.
	 * @param prefix - prefix added to every line in the text.
	 * @return The modified text.
	 */
	protected String addPrefix(String text, String prefix) {
		return text.replaceAll("(?m)^", prefix);
	}
	
	/**
	 * Retrieve a string representation of the given object.
	 * @param value - object to convert.
	 * @return String representation.
	 */
	protected String getStringDescription(Object value) {
		
		// We can't only rely on toString.
		if (value == null) {
			return "[NULL]";
		} if (isSimpleType(value)) {
			return value.toString();
		} else {
			try {
				if (!apacheCommonsMissing)
					return (ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE, false, null));
			} catch (Throwable ex) {
				// Apache is probably missing
				apacheCommonsMissing = true;
			}
			
			// Use our custom object printer instead
			try {
				return PrettyPrinter.printObject(value, value.getClass(), Object.class);
			} catch (IllegalAccessException e) {
				return "[Error: " + e.getMessage() + "]";
			}
		}
	}
	
	/**
	 * Determine if the given object is a wrapper for a primitive/simple type or not.
	 * @param test - the object to test.
	 * @return TRUE if this object is simple enough to simply be printed, FALSE othewise.
	 */
	protected boolean isSimpleType(Object test) {
		return test instanceof String || Primitives.isWrapperType(test.getClass());
	}
	
	public int getErrorCount() {
		return internalErrorCount.get();
	}

	public void setErrorCount(int errorCount) {
		internalErrorCount.set(errorCount);
	}

	public int getMaxErrorCount() {
		return maxErrorCount;
	}

	public void setMaxErrorCount(int maxErrorCount) {
		this.maxErrorCount = maxErrorCount;
	}
	
	/**
	 * Adds the given global parameter. It will be included in every error report.
	 * @param key - name of parameter.
	 * @param value - the global parameter itself.
	 */
	public void addGlobalParameter(String key, Object value) {
		globalParameters.put(key, value);
	}
	
	public Object getGlobalParameter(String key) {
		return globalParameters.get(key);
	}
	
	public void clearGlobalParameters() {
		globalParameters.clear();
	}
	
	public Set<String> globalParameters() {
		return globalParameters.keySet();
	}
	
	public String getSupportURL() {
		return supportURL;
	}

	public void setSupportURL(String supportURL) {
		this.supportURL = supportURL;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
