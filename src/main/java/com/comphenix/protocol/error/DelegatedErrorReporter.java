package com.comphenix.protocol.error;

import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.error.Report.ReportBuilder;

/**
 * Construct an error reporter that delegates to another error reporter.
 * @author Kristian
 */
public class DelegatedErrorReporter implements ErrorReporter {
	private final ErrorReporter delegated;

	/**
	 * Construct a new error reporter that forwards all reports to a given reporter.
	 * @param delegated - the delegated reporter.
	 */
	public DelegatedErrorReporter(ErrorReporter delegated) {
		this.delegated = delegated;
	}
	
	/**
	 * Retrieve the underlying error reporter.
	 * @return Underlying error reporter.
	 */
	public ErrorReporter getDelegated() {
		return delegated;
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error) {
		delegated.reportMinimal(sender, methodName, error);
	}

	@Override
	public void reportMinimal(Plugin sender, String methodName, Throwable error, Object... parameters) {
		delegated.reportMinimal(sender, methodName, error, parameters);
	}

	@Override
	public void reportDebug(Object sender, Report report) {
		Report transformed = filterReport(sender, report, false);
		
		if (transformed != null) {
			delegated.reportDebug(sender, transformed);
		}
	}
	
	@Override
	public void reportWarning(Object sender, Report report) {
		Report transformed = filterReport(sender, report, false);
		
		if (transformed != null) {
			delegated.reportWarning(sender, transformed);
		}
	}

	@Override
	public void reportDetailed(Object sender, Report report) {
		Report transformed = filterReport(sender, report, true);
		
		if (transformed != null) {
			delegated.reportDetailed(sender, transformed);
		}
	}

	/**
	 * Invoked before an error report is passed on to the underlying error reporter.
	 * <p>
	 * To cancel a report, return NULL.
	 * @param sender - the sender instance or class.
	 * @param report - the error report.
	 * @param detailed - whether or not the report will be displayed in detail.
	 * @return The report to pass on, or NULL to cancel it.
	 */
	protected Report filterReport(Object sender, Report report, boolean detailed) {
		return report;
	}
	
	@Override
	public void reportWarning(Object sender, ReportBuilder reportBuilder) {
		reportWarning(sender, reportBuilder.build());
	}
	
	@Override
	public void reportDetailed(Object sender, ReportBuilder reportBuilder) {
		reportDetailed(sender, reportBuilder.build());
	}
	
	@Override
	public void reportDebug(Object sender, ReportBuilder builder) {
		reportDebug(sender, builder.build());
	}
}
