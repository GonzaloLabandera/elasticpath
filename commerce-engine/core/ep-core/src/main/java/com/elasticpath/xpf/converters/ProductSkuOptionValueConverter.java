/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOption;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOptionValue;

/**
 * Converts {@code com.elasticpath.domain.skuconfiguration.SkuOptionValue} to {@code com.elasticpath.xpf.connectivity.context.ProductSkuOptionValue}.
 */
public class ProductSkuOptionValueConverter implements Converter<StoreDomainContext<SkuOptionValue>, XPFProductSkuOptionValue> {

	private ProductSkuOptionConverter xpfProductSkuOptionConverter;
	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFProductSkuOptionValue convert(final StoreDomainContext<SkuOptionValue> storeDomainContext) {

		SkuOptionValue skuOptionValue = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, skuOptionValue.getDisplayName(locale, false)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		XPFProductSkuOption productSkuOption =
				xpfProductSkuOptionConverter.convert(new StoreDomainContext<>(skuOptionValue.getSkuOption(),
						storeOptional));
		return new XPFProductSkuOptionValue(
				skuOptionValue.getOptionValueKey(),
				localizedDisplayNames,
				productSkuOption);
	}

	public void setXpfProductSkuOptionConverter(final ProductSkuOptionConverter xpfProductSkuOptionConverter) {
		this.xpfProductSkuOptionConverter = xpfProductSkuOptionConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}
}
