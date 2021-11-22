/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerGroup;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCustomer;

@RunWith(MockitoJUnitRunner.class)
public class CustomerConverterTest {
	@Mock
	private Customer customer;
	@Mock
	private Map<String, CustomerProfileValue> customerProfileValueMap;
	@Mock
	private Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues;
	@Mock
	private XPFConverterUtil xpfConverterUtil;
	@Mock
	private CustomerGroup customerGroup1, customerGroup2;
	@InjectMocks
	private CustomerConverter customerConverter;

	@Test
	public void testConvert() {
		String sharedId = "sharedId";
		String email = "email";
		String guid = "guid";
		String customerGroupName = "customerGroupName";
		Integer customerStatus = 1;
		when(customer.getSharedId()).thenReturn(sharedId);
		when(customer.getEmail()).thenReturn(email);
		when(customer.getGuid()).thenReturn(guid);

		when(customer.getProfileValueMap()).thenReturn(customerProfileValueMap);
		when(customer.getCustomerGroups()).thenReturn(Stream.of(customerGroup1, customerGroup2).collect(Collectors.toList()));
		when(customer.getStatus()).thenReturn(customerStatus);
		when(customerGroup2.getName()).thenReturn(customerGroupName);
		when(customerGroup1.isEnabled()).thenReturn(false);
		when(customerGroup2.isEnabled()).thenReturn(true);
		when(xpfConverterUtil.convertCustomerProfilesToXpfAttributeValues(customerProfileValueMap, Optional.empty()))
				.thenReturn(contextAttributeValues);

		XPFCustomer contextCustomer = customerConverter.convert(new StoreDomainContext<>(customer, Optional.empty()));

		assertEquals(sharedId, contextCustomer.getSharedId());
		assertEquals(email, contextCustomer.getEmail());
		assertEquals(contextAttributeValues, contextCustomer.getAttributeValues());
		assertEquals(Collections.singleton(customerGroupName), contextCustomer.getSegments());
	}
}