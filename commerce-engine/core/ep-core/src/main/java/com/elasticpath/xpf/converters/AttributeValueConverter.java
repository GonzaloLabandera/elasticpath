/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFAttribute;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;

/**
 * Converts {@code com.elasticpath.domain.attribute.AttributeValue} to {@code com.elasticpath.xpf.connectivity.context.AttributeValue}.
 */
public class AttributeValueConverter implements Converter<StoreDomainContext<AttributeValue>, XPFAttributeValue> {

	private AttributeConverter xpfAttributeConverter;

	@Override
	public XPFAttributeValue convert(final StoreDomainContext<AttributeValue> storeDomainContext) {
		AttributeValue attributeValue = storeDomainContext.getDomain();

		XPFAttribute xpfAttribute =
				xpfAttributeConverter.convert(new StoreDomainContext<>(attributeValue.getAttribute(), storeDomainContext.getStore()));
		return new XPFAttributeValue(
				attributeValue.getStringValue(),
				attributeValue.getValue(),
				xpfAttribute);

	}

	public void setXpfAttributeConverter(final AttributeConverter xpfAttributeConverter) {
		this.xpfAttributeConverter = xpfAttributeConverter;
	}
}
