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
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOption;

/**
 * Converts {@code com.elasticpath.domain.skuconfiguration.SkuOption} to {@code com.elasticpath.xpf.connectivity.context.ProductSkuOption}.
 */
public class ProductSkuOptionConverter implements Converter<StoreDomainContext<SkuOption>, XPFProductSkuOption> {

	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFProductSkuOption convert(final StoreDomainContext<SkuOption> storeDomainContext) {

		SkuOption skuOption = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);


		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, skuOption.getDisplayName(locale, false)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		return new XPFProductSkuOption(skuOption.getOptionKey(), localizedDisplayNames);
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}
}
