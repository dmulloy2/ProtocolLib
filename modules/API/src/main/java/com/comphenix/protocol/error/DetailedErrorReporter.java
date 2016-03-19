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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.collections.ExpireHashMap;
import com.comphenix.protocol.error.Report.ReportBuilder;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.reflect.PrettyPrinter;
import com.google.common.base.Preconditions;
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
	public static final String DEFAULT_SUPPORT_URL = "https://github.com/dmulloy2/ProtocolLib/issues";

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
	protected static boolean apacheCommonsMissing;
	
	// Whether or not detailed errror reporting is enabled
	protected boolean detailedReporting;
	
	// Map of global objects
	protected Map<String, Object> globalParameters = new HashMap<String, Object>();
	
	// Reports to ignore
	private ExpireHashMap<Report, Boolean> rateLimited = new ExpireHashMap<Report, Boolean>();
	private Object rateLock = new Object();
	
	/**
	 * Create a default error reporting system.
	 * @param plugin - the plugin owner.
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
		this.pluginName = getNameSafely(plugin);
		this.prefix = prefix;
		this.supportURL = supportURL;
		this.maxErrorCount = maxErrorCount;
		this.logger = logger;
	}

	private String getNameSafely(Plugin plugin) {
		try {
			return plugin.getName();
		} catch (LinkageError e) {
			return "ProtocolLib";
		}
	}

	// Attempt to get the logger.
	private static Logger getBukkitLogger() {
		try {
			return Bukkit.getLogger();
		} catch (LinkageError e) {
			return Logger.getLogger("Minecraft");
		}
	}
	
	/**
	 * Determine if we're using detailed error reporting.
	 * @return TRUE if we are, FALSE otherwise.
	 */
	public boolean isDetailedReporting() {
		return detailedReporting;
	}
	
	/**
	 * Set whether or not to use detailed error reporting.
	 * @param detailedReporting - TRUE to enable it, FALSE otherwise.
	 */
	public void setDetailedReporting(boolean detailedReporting) {
		this.detailedReporting = detailedReporting;
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
	
	/**
	 * Report a problem with a given method and plugin, ensuring that we don't exceed the maximum number of error reports.
	 * @param sender - the component that observed this exception.
	 * @param methodName - the method name.
	 * @param error - the error itself.
	 * @return TRUE if the error was printed, FALSE if it was suppressed.
	 */
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
	public void reportDebug(Object sender, ReportBuilder builder) {
		reportDebug(sender, Preconditions.checkNotNull(builder, "builder cannot be NULL").build());
	}
	
	@Override
	public void reportDebug(Object sender, Report report) {
		if (logger.isLoggable(Level.FINE) && canReport(report)) {
			reportLevel(Level.FINE, sender, report);
		}
	}
	
	@Override
	public void reportWarning(Object sender, ReportBuilder reportBuilder) {
		if (reportBuilder == null)
			throw new IllegalArgumentException("reportBuilder cannot be NULL.");
		
		reportWarning(sender, reportBuilder.build());
	}
	
	@Override
	public void reportWarning(Object sender, Report report) {
		if (logger.isLoggable(Level.WARNING) && canReport(report)) {
			reportLevel(Level.WARNING, sender, report);
		}
	}
	
	/**
	 * Determine if we should print the given report.
	 * <p>
	 * The default implementation will check for rate limits.
	 * @param report - the report to check.
	 * @return TRUE if we should print it, FALSE otherwise.
	 */
	protected boolean canReport(Report report) {
		long rateLimit = report.getRateLimit();
		
		// Check for rate limit
		if (rateLimit > 0) {
			synchronized (rateLock) {
				if (rateLimited.containsKey(report)) {
					return false;
				}
				rateLimited.put(report, true, rateLimit, TimeUnit.NANOSECONDS);
			}
		}
		return true;
	}
	
	private void reportLevel(Level level, Object sender, Report report) {
		String message = "[" + pluginName + "] [" + getSenderName(sender) + "] " + report.getReportMessage();
		
		// Print the main warning
		if (report.getException() != null) {
			logger.log(level, message, report.getException());
		} else {
			logger.log(level, message);
			
			// Remember the call stack
			if (detailedReporting) {
				printCallStack(level, logger);
			}
		}
		
		// Parameters?
		if (report.hasCallerParameters()) {
			// Write it
			logger.log(level, printParameters(report.getCallerParameters()));
		}
	}
	
	/**
	 * Retrieve the name of a sender class.
	 * @param sender - sender object.
	 * @return The name of the sender's class.
	 */
	private String getSenderName(Object sender) {
		if (sender != null)
			return ReportType.getSenderClass(sender).getSimpleName();
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
		
		// Secondary rate limit
		if (!canReport(report)) {
			return;
		}
		
		StringWriter text = new StringWriter();
		PrintWriter writer = new PrintWriter(text);

		// Helpful message
		writer.println("[" + pluginName + "] INTERNAL ERROR: " + report.getReportMessage());
	    writer.println("If this problem hasn't already been reported, please open a ticket");
	    writer.println("at " + supportURL + " with the following data:");
	    
	    // Now, let us print important exception information
		writer.println("Stack Trace:");

		if (report.getException() != null) {
			report.getException().printStackTrace(writer);
			
		} else if (detailedReporting) {
			printCallStack(writer);
		}
		
		// Data dump!
		writer.println("Dump:");
		
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

		// And java version
		writer.println("Java Version:");
		writer.println(addPrefix(System.getProperty("java.version"), SECOND_LEVEL_PREFIX));

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
	
	/**
	 * Print the call stack to the given logger.
	 * @param logger - the logger.
	 */
	private void printCallStack(Level level, Logger logger) {
		StringWriter text = new StringWriter();
		printCallStack(new PrintWriter(text));
		
		// Print the exception
		logger.log(level, text.toString());
	}
	
	/**
	 * Print the current call stack.
	 * @param writer - the writer.
	 */
	private void printCallStack(PrintWriter writer) {
		Exception current = new Exception("Not an error! This is the call stack.");
		current.printStackTrace(writer);
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
	public static String getStringDescription(Object value) {
		// We can't only rely on toString.
		if (value == null) {
			return "[NULL]";
		} if (isSimpleType(value) || value instanceof Class<?>) {
			return value.toString();
		} else {
			try {
				if (!apacheCommonsMissing)
					return (ToStringBuilder.reflectionToString(value, ToStringStyle.MULTI_LINE_STYLE, false, null));
			} catch (LinkageError ex) {
				// Apache is probably missing
				apacheCommonsMissing = true;
			} catch (Exception e) {
				// Don't use the error logger to log errors in error logging (that could lead to infinite loops)
				ProtocolLibrary.log(Level.WARNING, "Cannot convert to a String with Apache: " + e.getMessage());
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
	protected static boolean isSimpleType(Object test) {
		return test instanceof String || Primitives.isWrapperType(test.getClass());
	}
	
	/**
	 * Retrieve the current number of errors printed through {@link #reportDetailed(Object, Report)}.
	 * @return Number of errors printed.
	 */
	public int getErrorCount() {
		return internalErrorCount.get();
	}

	/**
	 * Set the number of errors printed.
	 * @param errorCount - new number of errors printed.
	 */
	public void setErrorCount(int errorCount) {
		internalErrorCount.set(errorCount);
	}

	/**
	 * Retrieve the maximum number of errors we can print before we begin suppressing errors.
	 * @return Maximum number of errors.
	 */
	public int getMaxErrorCount() {
		return maxErrorCount;
	}

	/**
	 * Set the maximum number of errors we can print before we begin suppressing errors.
	 * @param maxErrorCount - new max count.
	 */
	public void setMaxErrorCount(int maxErrorCount) {
		this.maxErrorCount = maxErrorCount;
	}
	
	/**
	 * Adds the given global parameter. It will be included in every error report.
	 * <p>
	 * Both key and value must be non-null.
	 * @param key - name of parameter.
	 * @param value - the global parameter itself.
	 */
	public void addGlobalParameter(String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("key cannot be NULL.");
		if (value == null)
			throw new IllegalArgumentException("value cannot be NULL.");
		
		globalParameters.put(key, value);
	}
	
	/**
	 * Retrieve a global parameter by its key.
	 * @param key - key of the parameter to retrieve.
	 * @return The value of the global parameter, or NULL if not found.
	 */
	public Object getGlobalParameter(String key) {
		if (key == null)
			throw new IllegalArgumentException("key cannot be NULL.");
		
		return globalParameters.get(key);
	}
	
	/**
	 * Reset all global parameters.
	 */
	public void clearGlobalParameters() {
		globalParameters.clear();
	}
	
	/**
	 * Retrieve a set of every registered global parameter.
	 * @return Set of all registered global parameters.
	 */
	public Set<String> globalParameters() {
		return globalParameters.keySet();
	}
	
	/**
	 * Retrieve the support URL that will be added to all detailed reports.
	 * @return Support URL.
	 */
	public String getSupportURL() {
		return supportURL;
	}

	/**
	 * Set the support URL that will be added to all detailed reports.
	 * @param supportURL - the new support URL.
	 */
	public void setSupportURL(String supportURL) {
		this.supportURL = supportURL;
	}
	
	/**
	 * Retrieve the prefix to apply to every line in the error reports.
	 * @return Error report prefix.
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * Set the prefix to apply to every line in the error reports.
	 * @param prefix - new prefix.
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	/**
	 * Retrieve the current logger that is used to print all reports.
	 * @return The current logger.
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * Set the current logger that is used to print all reports.
	 * @param logger - new logger.
	 */
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
}
