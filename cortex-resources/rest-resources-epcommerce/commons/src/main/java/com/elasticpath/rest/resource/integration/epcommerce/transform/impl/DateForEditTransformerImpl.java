/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.transform.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;

/**
 * Implementation of <code>DateForEditTransformer</code>.
 */
@Component
public class DateForEditTransformerImpl implements DateForEditTransformer {

	@Override
	public Optional<Date> transformToDomain(final AttributeType attributeType, final String date) {
		Objects.requireNonNull(date);

		if (AttributeType.DATETIME.equals(attributeType)) {
			return Optional.of(Date.from(Instant.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(date))));
		} else if (AttributeType.DATE.equals(attributeType)) {
			// in order to convert to a java.util.Date we need to imply a zone, default to UTC.
			LocalDate localDate = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(date));
			Instant instant = localDate.atStartOfDay(ZoneId.of("Z")).toInstant();
			return Optional.of(Date.from(instant));
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> transformToString(final AttributeType attributeType, final Date date) {
		Objects.requireNonNull(date);

		if (AttributeType.DATETIME.equals(attributeType)) {
			return Optional.of(DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("Z")).format(date.toInstant()));
		} else if (AttributeType.DATE.equals(attributeType)) {
			return Optional.of(DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.of("Z")).format(date.toInstant()));
		}
		return Optional.empty();
	}
}
