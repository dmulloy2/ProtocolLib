package com.comphenix.protocol.error;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

/**
 * Represents a error or warning report.
 * 
 * @author Kristian
 */
public class Report {	
	private final ReportType type;
	private final Throwable exception;
	private final Object[] messageParameters;
	private final Object[] callerParameters;
	
	// Used to rate limit reports that are similar
	private final long rateLimit;
	
	/**
	 * Must be constructed through the factory method in Report.
	 */
	public static class ReportBuilder {
		private ReportType type;
		private Throwable exception;
		private Object[] messageParameters;
		private Object[] callerParameters;
		private long rateLimit;
	
		private ReportBuilder() {
			// Don't allow
		}
		
		/**
		 * Set the current report type. Cannot be NULL.
		 * @param type - report type.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder type(ReportType type) {
			if (type == null)
				throw new IllegalArgumentException("Report type cannot be set to NULL.");
			this.type = type;
			return this;
		}
		
		/**
		 * Set the current exception that occurred.
		 * @param exception - exception that occurred.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder error(@Nullable Throwable exception) {
			this.exception = exception;
			return this;
		}
		
		/**
		 * Set the message parameters that are used to construct a message text.
		 * @param messageParameters - parameters for the report type.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder messageParam(@Nullable Object... messageParameters) {
			this.messageParameters = messageParameters;
			return this;
		}
		
		/**
		 * Set the parameters in the caller method. This is optional.
		 * @param callerParameters - parameters of the caller method.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder callerParam(@Nullable Object... callerParameters) {
			this.callerParameters = callerParameters;
			return this;
		}
		
		/**
		 * Set the minimum number of nanoseconds to wait until a report of equal type and parameters 
		 * is allowed to be printed again.
		 * @param rateLimit - number of nanoseconds, or 0 to disable. Cannot be negative.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder rateLimit(long rateLimit) {
			if (rateLimit < 0)
				throw new IllegalArgumentException("Rate limit cannot be less than zero.");
			this.rateLimit = rateLimit;
			return this;
		}
		
		/**
		 * Set the minimum time to wait until a report of equal type and parameters is allowed to be printed again.
		 * @param rateLimit - the time, or 0 to disable. Cannot be negative.
		 * @param rateUnit - the unit of the rate limit.
		 * @return This builder, for chaining.
		 */
		public ReportBuilder rateLimit(long rateLimit, TimeUnit rateUnit) {
			return rateLimit(TimeUnit.NANOSECONDS.convert(rateLimit, rateUnit));
		}
		
		/**
		 * Construct a new report with the provided input.
		 * @return A new report.
		 */
		public Report build() {
			return new Report(type, exception, messageParameters, callerParameters, rateLimit);
		}
	}
	
	/**
	 * Construct a new report builder.
	 * @param type - the initial report type.
	 * @return Report builder.
	 */
	public static ReportBuilder newBuilder(ReportType type) {
		return new ReportBuilder().type(type);
	}
	
	/**
	 * Construct a new report with the given type and parameters.
	 * @param exception - exception that occured in the caller method.
	 * @param type - the report type that will be used to construct the message.
	 * @param messageParameters - parameters used to construct the report message.
	 * @param callerParameters - parameters from the caller method.
	 */ 
	protected Report(ReportType type, @Nullable Throwable exception, 
			@Nullable Object[] messageParameters, @Nullable Object[] callerParameters) {
		this(type, exception, messageParameters, callerParameters, 0);
	}

	/**
	 * Construct a new report with the given type and parameters.
	 * @param exception - exception that occurred in the caller method.
	 * @param type - the report type that will be used to construct the message.
	 * @param messageParameters - parameters used to construct the report message.
	 * @param callerParameters - parameters from the caller method.
	 * @param rateLimit - minimum number of nanoseconds to wait until a report of equal type and parameters is allowed to be printed again.
	 */ 
	protected Report(ReportType type, @Nullable Throwable exception, 
			@Nullable Object[] messageParameters, @Nullable Object[] callerParameters, long rateLimit) {
		if (type == null)
			throw new IllegalArgumentException("type cannot be NULL.");
		this.type = type;
		this.exception = exception;
		this.messageParameters = messageParameters;
		this.callerParameters = callerParameters;
		this.rateLimit = rateLimit;
	}
	
	/**
	 * Format the current report type with the provided message parameters.
	 * @return The formated report message.
	 */
	public String getReportMessage() {
		return type.getMessage(messageParameters);
	}
	
	/**
	 * Retrieve the message parameters that will be used to construc the report message.
	 * <p<
	 * This should not be confused with the method parameters of the caller method.
	 * @return Message parameters.
	 */
	public Object[] getMessageParameters() {
		return messageParameters;
	}
	
	/**
	 * Retrieve the parameters of the caller method. Optional - may be NULL.
	 * @return Parameters or the caller method.
	 */
	public Object[] getCallerParameters() {
		return callerParameters;
	}
	
	/**
	 * Retrieve the report type.
	 * @return Report type.
	 */
	public ReportType getType() {
		return type;
	}
	
	/**
	 * Retrieve the associated exception, or NULL if not found.
	 * @return Associated exception, or NULL.
	 */
	public Throwable getException() {
		return exception;
	}
	
	/**
	 * Determine if we have any message parameters.
	 * @return TRUE if there are any message parameters, FALSE otherwise.
	 */
	public boolean hasMessageParameters() {
		return messageParameters != null && messageParameters.length > 0;
	}
	
	/**
	 * Determine if we have any caller parameters.
	 * @return TRUE if there are any caller parameters, FALSE otherwise.
	 */
	public boolean hasCallerParameters() {
		return callerParameters != null && callerParameters.length > 0;
	}

	/**
	 * Retrieve desired  minimum number of nanoseconds until a report of the same type and parameters should be reprinted.
	 * <p>
	 * Note that this may be ignored or modified by the error reporter. Zero indicates no rate limit.
	 * @return The number of nanoseconds. Never negative.
	 */
	public long getRateLimit() {
		return rateLimit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(callerParameters);
		result = prime * result + Arrays.hashCode(messageParameters);
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof Report) {
			Report other = (Report) obj;
			return type == other.type && 
				   Arrays.equals(callerParameters, other.callerParameters) && 
				   Arrays.equals(messageParameters, other.messageParameters);
		}
		return false;
	}
}
