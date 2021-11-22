/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFCartType;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

/**
 * Converts {@code com.elasticpath.domain.store.Store} to {@code com.elasticpath.xpf.connectivity.context.Store}.
 */
public class StoreConverter implements Converter<Store, XPFStore> {
	private CatalogConverter xpfCatalogConverter;
	private CartTypeConverter xpfCartTypeConverter;

	@Override
	public XPFStore convert(final Store store) {
		XPFCatalog xpfCatalog = xpfCatalogConverter.convert(store.getCatalog());
		Set<XPFCartType> cartTypes = store.getShoppingCartTypes().stream().map(xpfCartTypeConverter::convert).collect(Collectors.toSet());

		Set<Currency> supportedCurrencies = new HashSet<>(store.getSupportedCurrencies());
		Set<Locale> supportedLocales = new HashSet<>(store.getSupportedLocales());

		return new XPFStore(
				store.getCode(),
				store.getName(),
				store.getTimeZone(),
				xpfCatalog,
				store.getDefaultCurrency(),
				store.getDefaultLocale(),
				cartTypes,
				supportedCurrencies,
				supportedLocales
		);
	}

	public void setXpfCatalogConverter(final CatalogConverter xpfCatalogConverter) {
		this.xpfCatalogConverter = xpfCatalogConverter;
	}

	public void setXpfCartTypeConverter(final CartTypeConverter xpfCartTypeConverter) {
		this.xpfCartTypeConverter = xpfCartTypeConverter;
	}
}
