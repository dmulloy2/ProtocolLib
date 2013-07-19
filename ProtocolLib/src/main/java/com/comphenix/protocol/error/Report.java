package com.comphenix.protocol.error;

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
	
	/**
	 * Must be constructed through the factory method in Report.
	 */
	public static class ReportBuilder {
		private ReportType type;
		private Throwable exception;
		private Object[] messageParameters;
		private Object[] callerParameters;
	
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
		 * Set the current exception that occured.
		 * @param exception - exception that occured.
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
		 * Construct a new report with the provided input.
		 * @return A new report.
		 */
		public Report build() {
			return new Report(type, exception, messageParameters, callerParameters);
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
	protected Report(ReportType type, @Nullable Throwable exception, @Nullable Object[] messageParameters, @Nullable Object[] callerParameters) {
		if (type == null)
			throw new IllegalArgumentException("type cannot be NULL.");
		this.type = type;
		this.exception = exception;
		this.messageParameters = messageParameters;
		this.callerParameters = callerParameters;
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
}
