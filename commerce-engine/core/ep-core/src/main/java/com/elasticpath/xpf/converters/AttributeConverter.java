/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttribute;

/**
 * Converts {@code com.elasticpath.domain.attribute.Attribute} to {@code com.elasticpath.xpf.connectivity.context.Attribute}.
 */
public class AttributeConverter implements Converter<StoreDomainContext<Attribute>, XPFAttribute> {

	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFAttribute convert(final StoreDomainContext<Attribute> storeDomainContext) {

		Attribute attribute = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		return new XPFAttribute(attribute.getKey(),
				attribute.isLocaleDependant(),
				locales.stream()
						.map(locale -> new Pair<>(locale, attribute.getDisplayName(locale)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}
}
