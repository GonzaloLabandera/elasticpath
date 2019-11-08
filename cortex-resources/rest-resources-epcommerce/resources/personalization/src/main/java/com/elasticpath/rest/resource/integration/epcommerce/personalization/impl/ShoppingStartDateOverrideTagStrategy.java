/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.commons.util.impl.DateUtils;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerTagStrategy;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * ShoppingStartDateOverrideTagStrategy overrides the SHOPPING_START_DATE tag on the customer's session
 * with the SHOPPING_CONTEXT_DATE_OVERRIDE trait if it is present.
 */
@Component(service = CustomerTagStrategy.class)
public class ShoppingStartDateOverrideTagStrategy implements CustomerTagStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(ShoppingStartDateOverrideTagStrategy.class);

	/** Tag name. */
	private static final String SHOPPING_DATE_OVERRIDE_KEY = "SHOPPING_CONTEXT_DATE_OVERRIDE";

	/** shopping context date tag name. */
	private static final String SHOPPING_START_TIME_KEY = "SHOPPING_START_TIME";

	/** Date format for ISO8601 dates. */
	private final SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtils.DATE_TIME_FORMAT_STRING_LOCAL_TIMEZONE, Locale.US);


	@Override
	public void populate(final Customer customer, final TagSet tagSet) {
		Tag contextDateOverrideTag = tagSet.getTagValue(SHOPPING_DATE_OVERRIDE_KEY);

		if (contextDateOverrideTag != null) {
			Date overrideDate;
			String dateString = (String) contextDateOverrideTag.getValue();

			try {
				overrideDate = dateFormat.parse(dateString);
				tagSet.addTag(SHOPPING_START_TIME_KEY, new Tag(overrideDate.getTime()));
			} catch (ParseException ex) {
				LOG.debug("Malformed shopping context override date: {}", dateString);
			}
		}
	}
}
