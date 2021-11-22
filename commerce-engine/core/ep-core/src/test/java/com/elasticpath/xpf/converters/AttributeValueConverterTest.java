/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFAttribute;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;

@RunWith(MockitoJUnitRunner.class)
public class AttributeValueConverterTest {
	@Mock
	private AttributeValue attributeValue;
	@Mock
	private AttributeConverter attributeConverter;
	@Mock
	private Attribute attribute;
	@Mock
	private XPFAttribute contextAttribute;
	@Mock
	private Object object;

	@InjectMocks
	private AttributeValueConverter attributeValueConverter;

	@Test
	public void testConvert() {
		String stringValue = "attributeValue";
		when(attributeValue.getAttribute()).thenReturn(attribute);
		when(attributeValue.getValue()).thenReturn(object);
		when(attributeValue.getStringValue()).thenReturn(stringValue);
		when(attributeConverter.convert(new StoreDomainContext<>(attribute, Optional.empty()))).thenReturn(contextAttribute);

		XPFAttributeValue contextAttributeValue = attributeValueConverter
				.convert(new StoreDomainContext<>(attributeValue, Optional.empty()));

		assertEquals(contextAttribute, contextAttributeValue.getAttribute());
		assertEquals(object, contextAttributeValue.getValue());
		assertEquals(stringValue, contextAttributeValue.getStringValue());
	}
}
