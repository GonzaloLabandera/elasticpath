/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shoppingcart.validation.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import com.google.common.collect.ImmutableMap;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.service.misc.TimeService;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.dto.XPFStructuredErrorMessage;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.ProductSkuValidator;

/**
 * Validator to check that the product sku is within dates.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART_READ, priority = 1010)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_CHECKOUT, priority = 1010)
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.VALIDATE_PRODUCT_SKU_AT_ADD_TO_CART, priority = 1010)
public class ProductSkuDatesValidatorImpl extends XPFExtensionPointImpl implements ProductSkuValidator {

	/**
	 * Message id for this items no longer available.
	 */
	private static final String MESSAGE_ID_NO_LONGER_AVAILABLE = "item.no.longer.available";

	/**
	 * Message id for this items not yet available.
	 */
	private static final String MESSAGE_ID_NOT_YET_AVAILABLE = "item.not.yet.available";

	@Autowired
	private TimeService timeService;

	private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
			.withLocale(Locale.US)
			.withZone(ZoneId.systemDefault());

	@Override
	public Collection<XPFStructuredErrorMessage> validate(final XPFProductSkuValidationContext context) {
		Instant currentDate = timeService.getCurrentTime().toInstant();

		Instant startDate = context.getProductSku().getEffectiveStartDate();
		if (startDate != null && startDate.isAfter(currentDate)) {
			return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID_NOT_YET_AVAILABLE,
					String.format("Item '%s' is not yet available for purchase", context.getProductSku().getCode()),
					ImmutableMap.of("item-code", context.getProductSku().getCode(), "available-date", formatter.format(startDate))));
		}

		Instant endDate = context.getProductSku().getEffectiveEndDate();
		if (endDate != null && endDate.isBefore(currentDate)) {
			return Collections.singletonList(new XPFStructuredErrorMessage(MESSAGE_ID_NO_LONGER_AVAILABLE,
					String.format("Item '%s' is no longer available for purchase", context.getProductSku().getCode()),
					ImmutableMap.of("item-code", context.getProductSku().getCode(), "expiry-date", formatter.format(endDate))));
		}

		return Collections.emptyList();

	}
}
