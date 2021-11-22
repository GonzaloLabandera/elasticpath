/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;

/**
 * Converts {@code com.elasticpath.domain.catalog.Catalog} to {@code com.elasticpath.xpf.connectivity.context.Catalog}.
 */
public class CatalogConverter implements Converter<Catalog, XPFCatalog> {
	@Override
	public XPFCatalog convert(final Catalog catalog) {
		return new XPFCatalog(catalog.getCode());
	}
}
