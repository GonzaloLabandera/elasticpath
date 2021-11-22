/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.xpf.connectivity.entity.XPFSession;

/**
 * Converts {@code com.elasticpath.domain.customer.CustomerSession} to {@code com.elasticpath.xpf.connectivity.context.Session}.
 */
public class SessionConverter implements Converter<CustomerSession, XPFSession> {
	@Override
	public XPFSession convert(final CustomerSession customerSession) {
		Map<String, Object> tagSetMap = customerSession.getCustomerTagSet().getTags().entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getValue()));

		return new XPFSession(customerSession.getCurrency(), customerSession.getLocale(), tagSetMap);
	}
}
