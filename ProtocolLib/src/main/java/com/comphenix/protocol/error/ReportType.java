package com.comphenix.protocol.error;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.reflect.FieldAccessException;

/**
 * Represents a strongly-typed report. Subclasses should be immutable.
 * <p>
 * By convention, a report must be declared as a static field publicly accessible from the sender class.
 * @author Kristian
 */
public class ReportType {
	private final String errorFormat;

	/**
	 * Construct a new report type.
	 * @param errorFormat - string used to format the underlying report.
	 */
	public ReportType(String errorFormat) {
		this.errorFormat = errorFormat;
	}
	
	/**
	 * Convert the given report to a string, using the provided parameters.
	 * @param parameters - parameters to insert, or NULL to insert nothing.
	 * @return The full report in string format.
	 */
	public String getMessage(Object[] parameters) {
		if (parameters == null || parameters.length == 0)
			return toString();
		else
			return String.format(errorFormat, parameters);
	}
	
	@Override
	public String toString() {
		return errorFormat;
	}
	
	/**
	 * Retrieve all publicly associated reports.
	 * @param clazz - sender class.
	 * @return All associated reports.
	 */
	public static ReportType[] getReports(Class<?> clazz) {
		if (clazz == null)
			throw new IllegalArgumentException("clazz cannot be NULL.");
		List<ReportType> result = new ArrayList<ReportType>();
		
		for (Field field : clazz.getFields()) {
			if (Modifier.isStatic(field.getModifiers()) && 
				ReportType.class.isAssignableFrom(field.getDeclaringClass())) {
				try {
					result.add((ReportType) field.get(null));
				} catch (IllegalAccessException e) {
					throw new FieldAccessException("Unable to access field.", e);
				}
			}
		}
		return result.toArray(new ReportType[0]);
	}
}
