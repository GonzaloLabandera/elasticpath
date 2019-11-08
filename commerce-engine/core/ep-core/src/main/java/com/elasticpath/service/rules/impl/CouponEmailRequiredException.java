/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.rules.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.exception.InvalidBusinessStateException;


/**
 * This exception is thrown when applying a coupon that requires an email without an email.
 */
public class CouponEmailRequiredException extends EpServiceException implements InvalidBusinessStateException {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private final List<StructuredErrorMessage> structuredErrorMessages;

	/**
	 * Constructor.
	 * @param couponCode the coupon code
	 */
	public CouponEmailRequiredException(final String couponCode) {
		super(errorMessage(couponCode));
		Map<String, String> data = new HashMap<>();
		data.put("coupon-code", couponCode);
		structuredErrorMessages = Collections.singletonList(
				new StructuredErrorMessage("coupon.email.required", errorMessage(couponCode), data));
	}

	private static String errorMessage(final String couponCode) {
		return String.format("Email address is required for the <%s> coupon.", couponCode);
	}

	@Override
	public List<StructuredErrorMessage> getStructuredErrorMessages() {
		return structuredErrorMessages;
	}

}
