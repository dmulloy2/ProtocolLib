package com.comphenix.protocol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.MultipleLinesPrompt.MultipleConversationCanceller;
import com.comphenix.protocol.concurrency.IntegerSet;
import com.comphenix.protocol.error.ErrorReporter;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * A command to apply JavaScript filtering to the packet command.
 * 
 * @author Kristian
 */
public class CommandFilter extends CommandBase {
	public static final ReportType REPORT_FALLBACK_ENGINE = new ReportType("Falling back to the Rhino engine.");
	public static final ReportType REPORT_CANNOT_LOAD_FALLBACK_ENGINE = new ReportType("Could not load Rhino either. Please upgrade your JVM or OS.");
	public static final ReportType REPORT_PACKAGES_UNSUPPORTED_IN_ENGINE = new ReportType("Unable to initialize packages for JavaScript engine.");
	public static final ReportType REPORT_FILTER_REMOVED_FOR_ERROR = new ReportType("Removing filter %s for causing %s.");
	public static final ReportType REPORT_CANNOT_HANDLE_CONVERSATION = new ReportType("Cannot handle conversation.");
	
	public interface FilterFailedHandler{
		/**
		 * Invoked when a given filter has failed.
		 * @param event - the packet event.
		 * @param filter - the filter that failed.
		 * @param ex - the failure.
		 * @returns TRUE to keep processing this filter, FALSE to remove it.
		 */
		public boolean handle(PacketEvent event, Filter filter, Exception ex);
	}
	
	/**
	 * Possible sub commands.
	 * 
	 * @author Kristian
	 */
	private enum SubCommand {
		ADD, REMOVE;
	}
	
	/**
	 * A filter that will be used to process a packet event.
	 * @author Kristian
	 */
	public static class Filter {
		private final String name;
		private final String predicate;
		
		private final IntegerSet ranges;
		
		/**
		 * Construct a new immutable filter.
		 * @param name - the unique name of the filter.
 		 * @param predicate - the JavaScript predicate that will be used to filter packet events.
		 * @param ranges - a list of valid packet ID ranges that this filter applies to.
		 */
		public Filter(String name, String predicate, Set<Integer> packets) {
			this.name = name;
			this.predicate = predicate;
			this.ranges = new IntegerSet(Packets.MAXIMUM_PACKET_ID + 1);
			this.ranges.addAll(packets);
		}
		
		/**
		 * Retrieve the unique name of the filter.
		 * @return Unique name of the filter.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Retrieve the JavaScript predicate that will be used to filter packet events.
		 * @return Predicate itself.
		 */
		public String getPredicate() {
			return predicate;
		}
		
		/**
		 * Retrieve a copy of the set of packets this filter applies to.
		 * @return Set of packets this filter applies to.
		 */
		public Set<Integer> getRanges() {
			return ranges.toSet();
		}
		
		/**
		 * Determine whether or not a packet event needs to be passed to this filter.
		 * @param event - the event to test.
		 * @return TRUE if it does, FALSE otherwise.
		 */
		private boolean isApplicable(PacketEvent event) {
			return ranges.contains(event.getPacketID());
		}
		
		/**
		 * Evaluate the current filter using the provided ScriptEngine as context.
		 * <p>
		 * This context may be modified with additional code.
		 * @param context - the current script context.
		 * @param event - the packet event to evaluate.
		 * @return TRUE to pass this packet event on to the debug listeners, FALSE otherwise.
		 * @throws ScriptException If the compilation failed or the filter is not valid.
		 */
		public boolean evaluate(ScriptEngine context, PacketEvent event) throws ScriptException {
			if (!isApplicable(event))
				return true;
			// Ensure that the predicate has been compiled
			compile(context);
			
			try {
				Object result = ((Invocable) context).invokeFunction(name, event, event.getPacket().getHandle());
				
				if (result instanceof Boolean)
					return (Boolean) result;
				else
					throw new ScriptException("Filter result wasn't a boolean: " + result);
				
			} catch (NoSuchMethodException e) {
				// Must be a fault with the script engine itself
				throw new IllegalStateException("Unable to compile " + name + " into current script engine.", e);
			}
		}
		
		/**
		 * Force the compilation of a specific filter.
		 * @param context - the current script context.
		 * @throws ScriptException If the compilation failed.
		 */
		public void compile(ScriptEngine context) throws ScriptException {
			if (context.get(name) == null) {
				context.eval("var " + name + " = function(event, packet) {\n" + predicate);
			}
		}
		
		/**
		 * Clean up all associated code from this filter in the provided script engine.
		 * @param context - the current script context.
		 */
		public void close(ScriptEngine context) {
			context.put(name, null);
		}
	}
	
	private class CompilationSuccessCanceller implements MultipleConversationCanceller {
		@Override
		public boolean cancelBasedOnInput(ConversationContext context, String in) {
			throw new UnsupportedOperationException("Cannot cancel on the last line alone.");
		}

		@Override
		public void setConversation(Conversation conversation) {
			// Ignore
		}

		@Override
		public boolean cancelBasedOnInput(ConversationContext context, String currentLine, StringBuilder lines, int lineCount) {
			try {
				engine.eval("function(event, packet) {\n" + lines.toString());
				
				// It compiles - accept the filter!
				return true;
			} catch (ScriptException e) {
				// We also have the function() line
				int realLineCount = lineCount + 1;
				
				// Only possible to recover from an error on the last line.
				return e.getLineNumber() < realLineCount;
			}
		}
		
		@Override
		public CompilationSuccessCanceller clone() {
			return new CompilationSuccessCanceller();
		}
	}
	
	/**
	 * Name of this command.
	 */
	public static final String NAME = "filter";
	
	// Default error handler
	private FilterFailedHandler defaultFailedHandler;
	
	// Currently registered filters
	private List<Filter> filters = new ArrayList<Filter>();
	
	// Owner plugin
	private final Plugin plugin;
	
	// Whether or not the command is enabled
	private ProtocolConfig config;
	
	// Script engine
	private ScriptEngine engine;
	
	public CommandFilter(ErrorReporter reporter, Plugin plugin, ProtocolConfig config) {
		super(reporter, CommandBase.PERMISSION_ADMIN, NAME, 2);
		this.plugin = plugin;
		this.config = config;
		
		// Start the engine
		initalizeScript();
	}
	
	private void initalizeScript() {
		try {
			// First attempt
			initializeEngine();
			
			// Oh for ..
			if (!isInitialized()) {
				throw new ScriptException("A JavaScript engine could not be found.");
			}
		} catch (ScriptException e1) {
			// It's not a huge deal
			printPackageWarning(e1);
			
			if (!config.getScriptEngineName().equals("rhino")) {
				reporter.reportWarning(this, Report.newBuilder(REPORT_FALLBACK_ENGINE));
				config.setScriptEngineName("rhino");
				config.saveAll();
				
				try {
					initializeEngine();
					
					if (!isInitialized()) {
						reporter.reportWarning(this, Report.newBuilder(REPORT_CANNOT_LOAD_FALLBACK_ENGINE));
					}
				} catch (ScriptException e2) {
					// And again ..
					printPackageWarning(e2);
				}
			}
		}
	}
	
	private void printPackageWarning(ScriptException e) {
		reporter.reportWarning(this, Report.newBuilder(REPORT_PACKAGES_UNSUPPORTED_IN_ENGINE).error(e));
	}
	
	/**
	 * Initialize the current configured engine.
	 * @throws ScriptException If we are unable to import packages.
	 */
	private void initializeEngine() throws ScriptException {
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByName(config.getScriptEngineName());
		
		// Import useful packages
		if (engine != null) {
			engine.eval("importPackage(org.bukkit);");
			engine.eval("importPackage(com.comphenix.protocol.reflect);");
		}
	}
	
	/**
	 * Determine if the filter engine has been successfully initialized.
	 * @return TRUE if it has, FALSE otherwise.
	 */
	public boolean isInitialized() {
		return engine != null;
	}

	private FilterFailedHandler getDefaultErrorHandler() {
		// No need to create a new object every time
		if (defaultFailedHandler == null) {
			defaultFailedHandler = new FilterFailedHandler() {
				@Override
				public boolean handle(PacketEvent event, Filter filter, Exception ex) {
					reporter.reportMinimal(plugin, "filterEvent(PacketEvent)", ex, event);
					reporter.reportWarning(this, 
							Report.newBuilder(REPORT_FILTER_REMOVED_FOR_ERROR).messageParam(filter.getName(), ex.getClass().getSimpleName())
					);
					return false;
				}
			};
		}
		return defaultFailedHandler;
	}
	
	/**
	 * Determine whether or not to pass the given packet event to the packet listeners.
	 * <p>
	 * Uses a default filter failure handler that simply prints the error message and removes the filter.
	 * @param event - the event.
	 * @return TRUE if we should, FALSE otherwise.
	 */
	public boolean filterEvent(PacketEvent event) {
		return filterEvent(event, getDefaultErrorHandler());
	}
	
	/**
	 * Determine whether or not to pass the given packet event to the packet listeners.
	 * @param event - the event.
	 * @param handler - failure handler.
	 * @return TRUE if we should, FALSE otherwise.
	 * @throws FilterFailedException If one of the filters failed.
	 */
	public boolean filterEvent(PacketEvent event, FilterFailedHandler handler) {
		for (Iterator<Filter> it = filters.iterator(); it.hasNext(); ) {
			Filter filter = it.next();
			
			try {
				if (!filter.evaluate(engine, event)) {
					return false;
				}
			} catch (Exception ex) {
				if (!handler.handle(event, filter, ex)) {
					it.remove();
				}
			}
		}
		// Pass!
		return true;
	}

	/*
	 * Description: Adds or removes a simple packet filter.
       Usage:       /<command> add|remove name [packet IDs]
	 */
	@Override
	protected boolean handleCommand(CommandSender sender, String[] args) {
		if (!config.isDebug()) {
			sender.sendMessage(ChatColor.RED + "Debug mode must be enabled in the configuration first!");
			return true;
		}
		if (!isInitialized()) {
			sender.sendMessage(ChatColor.RED + "JavaScript engine was not present. Filter system is disabled.");
			return true;
		}
		
		final SubCommand command = parseCommand(args, 0);
		final String name = args[1];
		
		switch (command) {
			case ADD:
				// Never overwrite an existing filter
				if (findFilter(name) != null) {
					sender.sendMessage(ChatColor.RED + "Filter " + name + " already exists. Remove it first.");
					return true;
				}
				
				final Set<Integer> packets = parseRanges(args, 2);
				sender.sendMessage("Enter filter program ('}' to complete or CANCEL):");
				
				// Make sure we can use the conversable interface
				if (sender instanceof Conversable) {
					final MultipleLinesPrompt prompt = 
							new MultipleLinesPrompt(new CompilationSuccessCanceller(), "function(event, packet) {");
					
					new ConversationFactory(plugin).
						withFirstPrompt(prompt).
						withEscapeSequence("CANCEL").
						withLocalEcho(false).
						addConversationAbandonedListener(new ConversationAbandonedListener() {
							@Override
							public void conversationAbandoned(ConversationAbandonedEvent event) {
								try {
									final Conversable whom = event.getContext().getForWhom();
									
									if (event.gracefulExit()) {
										String predicate = prompt.removeAccumulatedInput(event.getContext());
										Filter filter = new Filter(name, predicate, packets);
	
										// Print the last line as well
										whom.sendRawMessage(prompt.getPromptText(event.getContext()));
										
										try {
											// Force early compilation
											filter.compile(engine);
											
											filters.add(filter);
											whom.sendRawMessage(ChatColor.GOLD + "Added filter " + name);
										} catch (ScriptException e) {
											e.printStackTrace();
											whom.sendRawMessage(ChatColor.GOLD + "Compilation error: " + e.getMessage());
										}
									} else {
										// Too bad
										whom.sendRawMessage(ChatColor.RED + "Cancelled filter.");
									}
								} catch (Exception e) {
									reporter.reportDetailed(this, 
											Report.newBuilder(REPORT_CANNOT_HANDLE_CONVERSATION).error(e).callerParam(event)
									);
								}
							}
						}).
						buildConversation((Conversable) sender).
						begin();
				} else {
					sender.sendMessage(ChatColor.RED + "Only console and players are supported!");
				}
				
				break;
				
			case REMOVE:
				Filter filter = findFilter(name);
				
				// See if it exists before we remove it
				if (filter != null) {
					filter.close(engine);
					filters.remove(filter);
					sender.sendMessage(ChatColor.GOLD + "Removed filter " + name);
				} else {
					sender.sendMessage(ChatColor.RED + "Unable to find a filter by the name " + name);
				}
				break;
		}
		
		return true;
	}
	
	private Set<Integer> parseRanges(String[] args, int start) {
		List<Range<Integer>> ranges = RangeParser.getRanges(args, 2, args.length - 1, Ranges.closed(0, 255));
		Set<Integer> flatten = new HashSet<Integer>();
		
		if (ranges.isEmpty()) {
			// Use every packet ID
			ranges.add(Ranges.closed(0, 255));
		}
		
		// Finally, flatten it all
		for (Range<Integer> range : ranges) {
			flatten.addAll(range.asSet(DiscreteDomains.integers()));
		}
		return flatten;
	}
	
	/**
	 * Lookup a filter by its name.
	 * @param name - the filter name.
	 * @return The filter, or NULL if not found.
	 */
	private Filter findFilter(String name) {
		// We'll just use a linear scan for now - we don't expect that many filters
		for (Filter filter : filters) {
			if (filter.getName().equalsIgnoreCase(name)) {
				return filter;
			}
		}
		return null;
	}
	
	private SubCommand parseCommand(String[] args, int index) {
		String text = args[index].toUpperCase();
		
		try {
			return SubCommand.valueOf(text);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(text + " is not a valid sub command. Must be add or remove.", e);
		}
	}
}
