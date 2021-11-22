/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;
import com.elasticpath.xpf.connectivity.entity.XPFCustomerStatusEnum;

/**
 * Converts {@code com.elasticpath.domain.customer.Customer} to {@code com.elasticpath.xpf.connectivity.context.Customer}.
 */
public class CustomerConverter implements Converter<StoreDomainContext<Customer>, XPFCustomer> {

	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFCustomer convert(final StoreDomainContext<Customer> storeDomainContext) {
		Customer customer = storeDomainContext.getDomain();
		Map<Locale, Map<String, XPFAttributeValue>> xpfAttributeValues =
				xpfXPFConverterUtil.convertCustomerProfilesToXpfAttributeValues(customer.getProfileValueMap(),
						storeDomainContext.getStore());

		Set<String> segments = customer.getCustomerGroups().stream()
				.filter(CustomerGroup::isEnabled)
				.map(CustomerGroup::getName)
				.collect(Collectors.toSet());

		XPFCustomerStatusEnum xpfCustomerStatusEnum = XPFCustomerStatusEnum.valueOf(customer.getStatus());

		return new XPFCustomer(customer.getGuid(), customer.getSharedId(), customer.getEmail(), xpfCustomerStatusEnum,
				xpfAttributeValues, segments);
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}
}
