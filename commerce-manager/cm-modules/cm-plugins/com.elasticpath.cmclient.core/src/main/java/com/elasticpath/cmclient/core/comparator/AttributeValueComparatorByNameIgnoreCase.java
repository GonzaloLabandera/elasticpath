/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.core.comparator;

import java.util.Comparator;
import java.util.Locale;

import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeValue;

/**
 * AttributeValue comparator, sorts objects by their attribute names in ascending order.
 */
public class AttributeValueComparatorByNameIgnoreCase implements Comparator<AttributeValue> {

	private final Locale locale;

	/**
	 * Instantiates an Attribute value Comparator with the given locale.
	 *
	 * @param locale specify the locale to be used when comparing Attribute display names
	 */
	public AttributeValueComparatorByNameIgnoreCase(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Instantiates an Attribute value Comparator using the JVM instance's default locale
	 * to compare Attribute display names.
	 */
	public AttributeValueComparatorByNameIgnoreCase() {
		this(CorePlugin.getDefault().getDefaultLocale());
	}

	/**
	 * Compares two AttributeValue objects by there attributes' names.
	 * 
	 * @param attrVal1 first attribute value
	 * @param attrVal2 second attribute value
	 * 
	 * @return a negative integer, zero, or a positive integer as the first AttributeValue name 
	 * is less than, equal to, or greater than the second ignoring the case 
	 */
	public int compare(final AttributeValue attrVal1, final AttributeValue attrVal2) {
		
		final Attribute attribute1 = attrVal1.getAttribute();
		final Attribute attribute2 = attrVal2.getAttribute();

		if (attribute1 == null || attribute2 == null) {
			return 1;
		}

		String name1 = attribute1.getDisplayName(locale, false, false);
		String name2 = attribute2.getDisplayName(locale, false, false);

		if (name1 == null || name2 == null) {
			return 1;
		}
		
		return name1.compareToIgnoreCase(name2);
	}

}
