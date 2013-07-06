package com.comphenix.protocol.error;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.reflect.fuzzy.FuzzyFieldContract;

/**
 * Represents a strongly-typed report. Subclasses should be immutable.
 * <p>
 * By convention, a report must be declared as a static field publicly accessible from the sender class.
 * @author Kristian
 */
public class ReportType {
	private final String errorFormat;
	
	// Used to store the report name
	protected String reportName;
	
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
	 * Retrieve the class of the given sender.
	 * <p>
	 * If the sender is already a Class, we return it.
	 * @param sender - the sender to look up.
	 * @return The class of the sender.
	 */
	public static Class<?> getSenderClass(Object sender) {
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NUll.");
		else if (sender instanceof Class<?>)
			return (Class<?>) sender;
		else
			return sender.getClass();
	}
	
	/**
	 * Retrieve the full canonical name of a given report type.
	 * <p>
	 * Note that the sender may be a class (for static callers), in which 
	 * case it will be used directly instead of its getClass() method.
	 * <p>
	 * It is thus not advisable for class classes to report reports.
	 * @param sender - the sender, or its class.
	 * @param type - the report type.
	 * @return The full canonical name.
	 */
	public static String getReportName(Object sender, ReportType type) {
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NUll.");
		return getReportName(getSenderClass(sender), type);
	}
	
	/**
	 * Retrieve the full canonical name of a given report type.
	 * <p>
	 * This is in the format <i>canonical_name_of_class#report_type</i>
	 * @param clazz - the sender class.
	 * @param type - the report instance.
	 * @return The full canonical name.
	 */
	private static String getReportName(Class<?> sender, ReportType type) {
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NUll.");
		
		// Whether or not we need to retrieve the report name again
		if (type.reportName == null) {
			for (Field field : getReportFields(sender)) {
				try {
					field.setAccessible(true);
					
					if (field.get(null) == type) {
						// We got the right field!
						return type.reportName = field.getDeclaringClass().getCanonicalName() + "#" + field.getName();
					}
				} catch (IllegalAccessException e) {
					throw new FieldAccessException("Unable to read field " + field, e);
				}
			}
			throw new IllegalArgumentException("Cannot find report name for " + type);
		}
		return type.reportName;
	}
	
	/**
	 * Retrieve all publicly associated reports.
	 * @param sender - sender class.
	 * @return All associated reports.
	 */
	public static ReportType[] getReports(Class<?> sender) {
		if (sender == null)
			throw new IllegalArgumentException("sender cannot be NULL.");
		List<ReportType> result = new ArrayList<ReportType>();
		
		// Go through all the fields
		for (Field field : getReportFields(sender)) {
			try {
				field.setAccessible(true);
				result.add((ReportType) field.get(null));
			} catch (IllegalAccessException e) {
				throw new FieldAccessException("Unable to read field " + field, e);
			}
		}
		return result.toArray(new ReportType[0]);
	}
	
	/**
	 * Retrieve all publicly associated report fields.
	 * @param clazz - sender class.
	 * @return All associated report fields.
	 */
	private static List<Field> getReportFields(Class<?> clazz) {
		return FuzzyReflection.fromClass(clazz).getFieldList(
				FuzzyFieldContract.newBuilder().
					requireModifier(Modifier.STATIC).
					typeDerivedOf(ReportType.class).
					build()
		);
	}
}
