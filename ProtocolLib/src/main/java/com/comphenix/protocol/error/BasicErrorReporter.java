package com.comphenix.protocol.error;

import java.io.PrintStream;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.Report.ReportBuilder;
import com.comphenix.protocol.reflect.PrettyPrinter;

/**
 * Represents a basic error reporter that prints error reports to the standard error stream.
 * <p>
 * Note that this implementation doesn't distinguish between {@link #reportWarning(Object, Report)} 
 * and {@link #reportDetailed(Object, Report)} - they both have the exact same behavior.
 * @author Kristian
 */
public class BasicErrorReporter implements ErrorReporter {
	private final PrintStream output;
	
	/**
	 * Construct a new basic error reporter that prints directly the standard error stream.
	 */
	public BasicErrorReporter() {
		 this(System.err);
	}
	
	/**
	 * Construct a error reporter that prints to the given output stream.
	 * @param output - the output stream.
	 */
	public BasicErrorReporter(PrintStream output) {
		this.output = output;
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error) {
		output.println("Unhandled exception occured in " +  methodName + " for " + sender.getName());
		error.printStackTrace(output);
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
		reportMinimal(sender, methodName, error);
		
		// Also print parameters
		printParameters(parameters);
	}

	@Override
	public void reportDebug(Object sender, Report report) {
		// We just have to swallow it
	}

	@Override
	public void reportDebug(Object sender, ReportBuilder builder) {
		// As above
	}
	
	@Override
	public void reportWarning(Object sender, Report report) {
		// Basic warning
		output.println("[" + sender.getClass().getSimpleName() + "] " + report.getReportMessage());
		
		if (report.getException() != null) {
			report.getException().printStackTrace(output);
		}
		printParameters(report.getCallerParameters());
	}

	@Override
	public void reportWarning(Object sender, ReportBuilder reportBuilder) {
		reportWarning(sender, reportBuilder.build());
	}

	@Override
	public void reportDetailed(Object sender, Report report) {
		// No difference from warning
		reportWarning(sender, report);
	}

	@Override
	public void reportDetailed(Object sender, ReportBuilder reportBuilder) {
		reportWarning(sender, reportBuilder);
	}
	
	/**
	 * Print the given parameters to the standard error stream.
	 * @param parameters - the output parameters.
	 */
	private void printParameters(Object[] parameters) {
		if (parameters != null && parameters.length > 0) {
			output.println("Parameters: ");
			
			try {
				for (Object parameter : parameters) {
					if (parameter == null)
						output.println("[NULL]");
					else
						output.println(PrettyPrinter.printObject(parameter));
				}
			} catch (IllegalAccessException e) {
				// Damn it
				e.printStackTrace();
			}
		}
	}
}
