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
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

/**
 * Converts {@code com.elasticpath.domain.catalog.Product} to {@code com.elasticpath.xpf.connectivity.context.Product}.
 */
public class ProductConverter implements Converter<StoreDomainContext<Product>, XPFProduct> {

	private static final boolean IS_BUNDLE = false;

	private CategoryConverter xpfCategoryConverter;
	private ProductTypeConverter xpfProductTypeConverter;
	private XPFConverterUtil xpfXPFConverterUtil;

	@Override
	public XPFProduct convert(final StoreDomainContext<Product> storeDomainContext) {

		Product product = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		final Set<XPFCategory> xpfCategories =
				product.getCategories().stream()
						.map(category -> xpfCategoryConverter.convert(new StoreDomainContext<>(category, storeOptional)))
						.collect(Collectors.toSet());

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, product.getDisplayName(locale)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		final Map<Locale, Map<String, XPFAttributeValue>> xpfAttributeValues =
				xpfXPFConverterUtil.convertToXpfAttributeValues(product.getAttributeValueMap(), storeOptional);

		final ProductType productType = product.getProductType();
		final XPFProductType xpfProductType = xpfProductTypeConverter.convert(productType);

		return new XPFProduct(
				product.getCode(),
				localizedDisplayNames,
				product.getStartDate() == null ? null : product.getStartDate().toInstant(),
				product.getEndDate() == null ? null : product.getEndDate().toInstant(),
				xpfProductType,
				xpfCategories,
				xpfAttributeValues,
				IS_BUNDLE,
				product.isNotSoldSeparately(),
				product.isHidden());
	}

	public void setXpfCategoryConverter(final CategoryConverter xpfCategoryConverter) {
		this.xpfCategoryConverter = xpfCategoryConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}

	public void setXpfProductTypeConverter(final ProductTypeConverter xpfProductTypeConverter) {
		this.xpfProductTypeConverter = xpfProductTypeConverter;
	}
}
