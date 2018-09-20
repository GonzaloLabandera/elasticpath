/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.commons.converter;

/**
 * Converts a string to a java type.
 *
 * @param <T> the type the string will be converted to.
 */
public interface StringToTypeConverter<T> {

	/**
	 * Converts the given string to a java type.
	 *
 	 * @param stringValue the string to convert.
	 * @return the converted type.
	 */
	T convert(String stringValue);
}
