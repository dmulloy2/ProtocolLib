package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.Report.ReportBuilder;
import com.google.common.base.Joiner;

/**
 * Represents an error reporter that rethrows every exception instead.
 * @author Kristian
 */
public class RethrowErrorReporter implements ErrorReporter {
	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error) {
		throw new RuntimeException("Minimal error by " + sender + " in " + methodName, error);
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
		throw new RuntimeException(
				"Minimal error by " + sender + " in " + methodName + " with " + Joiner.on(",").join(parameters), error);
	}

	@Override
	public void reportDebug(Object sender, Report report) {
		// Do nothing - this is just a debug
	}

	@Override
	public void reportDebug(Object sender, ReportBuilder builder) {
		// As above
	}
	
	@Override
	public void reportWarning(Object sender, ReportBuilder reportBuilder) {
		reportWarning(sender, reportBuilder.build());
	}
	
	@Override
	public void reportWarning(Object sender, Report report) {
		throw new RuntimeException("Warning by " + sender + ": " + report);
	}

	@Override
	public void reportDetailed(Object sender, ReportBuilder reportBuilder) {
		reportDetailed(sender, reportBuilder.build());
	}
	
	@Override
	public void reportDetailed(Object sender, Report report) {
		throw new RuntimeException("Detailed error " + sender + ": " + report, report.getException());
	}
}
