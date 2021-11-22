/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;
import com.elasticpath.xpf.connectivity.entity.XPFSession;

@RunWith(MockitoJUnitRunner.class)
public class SessionConverterTest {
	@Mock
	private CustomerSession customerSession;

	@InjectMocks
	private SessionConverter sessionConverter;

	@Test
	public void testConvert() {
		Currency currency = Currency.getInstance("USD");
		Locale locale = Locale.ENGLISH;

		String tagKey1 = "tagKey";
		String tagKey2 = "tagKey2";
		Object tagValue1 = "value1";
		Object tagValue2 = 1;

		TagSet tagSet = new TagSet();
		tagSet.addTag(tagKey1, new Tag(tagValue1));
		tagSet.addTag(tagKey2, new Tag(tagValue2));

		Map<String, Object> contextTagSet = new HashMap<>();
		contextTagSet.put(tagKey1, tagValue1);
		contextTagSet.put(tagKey2, tagValue2);

		when(customerSession.getCurrency()).thenReturn(currency);
		when(customerSession.getLocale()).thenReturn(locale);
		when(customerSession.getCustomerTagSet()).thenReturn(tagSet);

		XPFSession contextSession = sessionConverter.convert(customerSession);

		assertEquals(currency, contextSession.getCurrency());
		assertEquals(locale, contextSession.getLocale());
		assertEquals(contextTagSet, contextSession.getTagSet());
	}
}