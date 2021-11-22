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
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;

/**
 * Converts {@code com.elasticpath.domain.catalog.Category} to {@code com.elasticpath.xpf.connectivity.context.Category}.
 */
public class CategoryConverter implements Converter<StoreDomainContext<Category>, XPFCategory> {

	private CatalogConverter xpfCatalogConverter;
	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFCategory convert(final StoreDomainContext<Category> storeDomainContext) {

		Category category = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		XPFCatalog xpfCatalog = xpfCatalogConverter.convert(category.getCatalog());

		Map<Locale, Map<String, XPFAttributeValue>> xpfAttributeValues =
				xpfXPFConverterUtil.convertToXpfAttributeValues(category.getAttributeValueMap(), storeOptional);

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, category.getDisplayName(locale)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		return new XPFCategory(
				category.getCode(),
				localizedDisplayNames,
				xpfAttributeValues,
				xpfCatalog);
	}

	public void setXpfCatalogConverter(final CatalogConverter xpfCatalogConverter) {
		this.xpfCatalogConverter = xpfCatalogConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}
}
