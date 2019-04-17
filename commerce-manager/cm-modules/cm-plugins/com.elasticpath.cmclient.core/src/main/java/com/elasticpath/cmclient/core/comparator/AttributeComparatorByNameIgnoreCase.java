/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */

package com.elasticpath.cmclient.core.comparator;

import java.util.Comparator;
import java.util.Locale;

import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.domain.attribute.Attribute;

/**
 * Sorts attributes by name (ignore case).
 */
public class AttributeComparatorByNameIgnoreCase implements Comparator<Attribute> {

	@Override
	public int compare(final Attribute attribute1, final Attribute attribute2) {
		final Locale locale = CorePlugin.getDefault().getDefaultLocale();
		if (attribute1 == null || attribute2 == null || attribute1.getDisplayName(locale) == null || attribute2.getDisplayName(locale) == null) {
			return 1;
		}
		return attribute1.getDisplayName(locale).compareToIgnoreCase(attribute2.getDisplayName(locale));
	}

}
