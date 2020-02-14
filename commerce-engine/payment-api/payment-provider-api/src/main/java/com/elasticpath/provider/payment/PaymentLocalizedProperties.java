/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment;

import java.util.Locale;
import java.util.Map;

/**
 * Represents a group of payment localized properties.
 */
public interface PaymentLocalizedProperties {
	/**
	 * Returns the value of the given property.
	 *
	 * @param propertyName the property name
	 * @param locale the locale
	 * @return the value of the given property and locale, or the default locale, or null
	 */
	String getValue(String propertyName, Locale locale);

	/**
	 * Gets the value of the given property for the given locale.
	 * If the value doesn't exist, returns <code>null</code>.
	 *
	 * @param propertyName the property name
	 * @param locale the locale
	 * @return the value of the given property and locale, or null if it doesn't exist
	 */
	String getValueWithoutFallBack(String propertyName, Locale locale);

	/**
	 * Sets the given value with the given property and locale.
	 *
	 * @param propertyName the property name
	 * @param locale the locale
	 * @param value the value to set
	 */
	void setValue(String propertyName, Locale locale, String value);

	/**
	 * Returns the payment localized properties map.
	 *
	 * @return the payment localized properties map.
	 */
	Map<String, PaymentLocalizedPropertyValue> getPaymentLocalizedPropertiesMap();

	/**
	 * Sets the payment localized properties map.
	 *
	 * @param map the map to set
	 * @param paymentLocalizedPropertyValueBean the bean to be used to create new values
	 */
	void setPaymentLocalizedPropertiesMap(Map<String, PaymentLocalizedPropertyValue> map,
										  String paymentLocalizedPropertyValueBean);

	/**
	 * Get the locale from the key in the map.
	 *
	 * @param keyInMap the key in the map
	 * @return the locale
	 */
	Locale getLocaleFromKey(String keyInMap);

	/**
	 * Get the propertyName from the key in the map.
	 *
	 * @param keyInMap the key in the map
	 * @return the propertyName
	 */
	String getPropertyNameFromKey(String keyInMap);

}
