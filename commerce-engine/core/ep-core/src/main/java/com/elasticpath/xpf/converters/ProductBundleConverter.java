/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.SelectionRule;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

/**
 * Converts {@code com.elasticpath.domain.catalog.ProductBundle} to {@code com.elasticpath.xpf.connectivity.context.ProductBundle}.
 */
public class ProductBundleConverter implements Converter<StoreDomainContext<ProductBundle>, XPFProductBundle> {

	private static final boolean IS_BUNDLE = true;
	private static final int SELECT_ALL = 0;

	private CategoryConverter xpfCategoryConverter;
	private XPFConverterUtil xpfXPFConverterUtil;
	private BundleConstituentConverter xpfBundleConstituentConverter;
	private ProductTypeConverter xpfProductTypeConverter;

	@Override
	public XPFProductBundle convert(final StoreDomainContext<ProductBundle> storeDomainContext) {
		ProductBundle productBundle = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		Set<XPFCategory> xpfCategories =
				productBundle.getCategories().stream().map(category ->
						xpfCategoryConverter.convert(new StoreDomainContext<>(category, storeOptional)))
						.collect(Collectors.toSet());

		Map<Locale, Map<String, XPFAttributeValue>> xpfAttributeValues =
				xpfXPFConverterUtil.convertToXpfAttributeValues(productBundle.getAttributeValueMap(), storeOptional);

		List<XPFBundleConstituent> xpfBundleConstituents = productBundle.getConstituents().stream()
				.map(bundleConstituent -> xpfBundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent,
						storeOptional)))
				.collect(Collectors.toList());

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, productBundle.getDisplayName(locale)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		XPFProductType xpfProductType = xpfProductTypeConverter.convert(productBundle.getProductType());

		long minConstituentSelections;
		long maxConstituentSelections;
		SelectionRule selectionRule = productBundle.getSelectionRule();
		if (selectionRule == null || selectionRule.getParameter() == SELECT_ALL) {
			minConstituentSelections = productBundle.getConstituents().size();
			maxConstituentSelections = productBundle.getConstituents().size();
		} else {
			minConstituentSelections = selectionRule.getParameter();
			maxConstituentSelections = selectionRule.getParameter();
		}

		return new XPFProductBundle(
				productBundle.getCode(),
				localizedDisplayNames,
				productBundle.getStartDate() == null ? null : productBundle.getStartDate().toInstant(),
				productBundle.getEndDate() == null ? null : productBundle.getEndDate().toInstant(),
				xpfCategories,
				xpfAttributeValues,
				IS_BUNDLE,
				productBundle.isNotSoldSeparately(),
				productBundle.isHidden(),
				xpfProductType,
				xpfBundleConstituents,
				minConstituentSelections,
				maxConstituentSelections);
	}


	public void setXpfCategoryConverter(final CategoryConverter xpfCategoryConverter) {
		this.xpfCategoryConverter = xpfCategoryConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}

	public void setXpfBundleConstituentConverter(final BundleConstituentConverter xpfBundleConstituentConverter) {
		this.xpfBundleConstituentConverter = xpfBundleConstituentConverter;
	}

	public void setXpfProductTypeConverter(final ProductTypeConverter xpfProductTypeConverter) {
		this.xpfProductTypeConverter = xpfProductTypeConverter;
	}
}
