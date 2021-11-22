/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.service.misc.TimeService;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuDatesValidatorTest {

	private static final Date NOW = new Date();

	private static final String SKU_CODE = "sku_code";

	private static final int DAYS_DELTA = 5;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
			.withLocale(Locale.US)
			.withZone(ZoneId.systemDefault());

	@InjectMocks
	private ProductSkuDatesValidatorImpl validator;

	@Mock
	private XPFProductSkuValidationContext context;

	@Mock
	private XPFProductSku productSku;

	@Mock
	private TimeService timeService;

	@Before
	public void setUp() {
		given(productSku.getCode()).willReturn(SKU_CODE);

		given(context.getProductSku()).willReturn(productSku);

		given(timeService.getCurrentTime()).willReturn(NOW);

	}

	@Test
	public void testProductNotYetAvailable() {
		// Given
		Instant startDate = Instant.now().plus(DAYS_DELTA, ChronoUnit.DAYS);
		given(productSku.getEffectiveStartDate()).willReturn(startDate);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.not.yet.available",
				String.format("Item '%s' is not yet available for purchase", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE, "available-date", formatter.format(startDate)));
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductNoLongerAvailable() {
		// Given
		Instant startDate = Instant.now().plus(-(DAYS_DELTA + DAYS_DELTA), ChronoUnit.DAYS);
		Instant endDate = Instant.now().plus(-DAYS_DELTA, ChronoUnit.DAYS);
		given(productSku.getEffectiveStartDate()).willReturn(startDate);
		given(productSku.getEffectiveEndDate()).willReturn(endDate);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		XPFStructuredErrorMessage structuredErrorMessage = new XPFStructuredErrorMessage("item.no.longer.available",
				String.format("Item '%s' is no longer available for purchase", SKU_CODE),
				ImmutableMap.of("item-code", SKU_CODE, "expiry-date", formatter.format(endDate)));
		assertThat(messageCollections).containsOnly(structuredErrorMessage);
	}

	@Test
	public void testProductSkuDatesAretWithinRange() {
		// Given
		Instant startDate = Instant.now().plus(-DAYS_DELTA, ChronoUnit.DAYS);
		Instant endDate = Instant.now().plus(DAYS_DELTA, ChronoUnit.DAYS);
		given(productSku.getEffectiveStartDate()).willReturn(startDate);
		given(productSku.getEffectiveEndDate()).willReturn(endDate);

		// When
		Collection<XPFStructuredErrorMessage> messageCollections = validator.validate(context);

		// Then
		assertThat(messageCollections).isEmpty();
	}

}
