package com.comphenix.protocol.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that this API and its descendants are only valid on Spigot.
 * @author Kristian
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PACKAGE,
	     ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD})
public @interface Spigot {
	/**
	 * The minimum build number of Spigot where this is valid.
	 * @return The minimum build.
	 */
	int minimumBuild();

	/**
	 * The maximum build number of Spigot where this is valid, or Integer.MAX_VALUE if not set.
	 * @return The maximum build number.
	 */
	int maximumBuild() default Integer.MAX_VALUE;
}