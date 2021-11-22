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
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOptionValue;

/**
 * Converts {@code com.elasticpath.domain.catalog.ProductSku} to {@code com.elasticpath.xpf.connectivity.context.ProductSku}.
 */
public class ProductSkuConverter implements Converter<StoreDomainContext<ProductSku>, XPFProductSku> {

	private ProductSkuOptionValueConverter xpfProductSkuOptionValueConverter;
	private XPFConverterUtil xpfXPFConverterUtil;
	private ProductConverter xpfProductConverter;
	private ProductBundleConverter xpfProductBundleConverter;

	@Override
	@SuppressWarnings("PMD.NPathComplexity")
	public XPFProductSku convert(final StoreDomainContext<ProductSku> storeDomainContext) {

		ProductSku productSku = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		XPFProduct product = (productSku.getProduct() instanceof ProductBundle)
				? xpfProductBundleConverter.convert(new StoreDomainContext<>((ProductBundle) productSku.getProduct(),
				storeOptional))
				: xpfProductConverter.convert(new StoreDomainContext<>(productSku.getProduct(), storeOptional));

		Set<XPFProductSkuOptionValue> productSkuOptionValues =
				productSku.getOptionValues().stream()
						.map(skuOptionValue -> xpfProductSkuOptionValueConverter.convert(new StoreDomainContext<>(skuOptionValue,
								storeOptional)))
						.collect(Collectors.toSet());

		Map<Locale, Map<String, XPFAttributeValue>> xpfAttributeValues =
				xpfXPFConverterUtil.convertToXpfAttributeValues(productSku.getAttributeValueMap(), storeOptional);

		Set<Locale> locales = xpfXPFConverterUtil.getLocalesForStore(storeOptional);

		Map<Locale, String> localizedDisplayNames =
				locales.stream()
						.map(locale -> new Pair<>(locale, productSku.getDisplayName(locale)))
						.filter(pair -> StringUtils.isNotBlank(pair.getSecond()))
						.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));

		return new XPFProductSku(
				product,
				productSku.getSkuCode(),
				localizedDisplayNames,
				productSkuOptionValues,
				xpfAttributeValues,
				productSku.getEffectiveStartDate() == null ? null : productSku.getEffectiveStartDate().toInstant(),
				productSku.getEffectiveEndDate() == null ? null : productSku.getEffectiveEndDate().toInstant(),
				productSku.isShippable());
	}

	public void setXpfProductSkuOptionValueConverter(final ProductSkuOptionValueConverter xpfProductSkuOptionValueConverter) {
		this.xpfProductSkuOptionValueConverter = xpfProductSkuOptionValueConverter;
	}

	public void setXpfXPFConverterUtil(final XPFConverterUtil xpfXPFConverterUtil) {
		this.xpfXPFConverterUtil = xpfXPFConverterUtil;
	}

	public void setXpfProductConverter(final ProductConverter xpfProductConverter) {
		this.xpfProductConverter = xpfProductConverter;
	}

	public void setXpfProductBundleConverter(final ProductBundleConverter xpfProductBundleConverter) {
		this.xpfProductBundleConverter = xpfProductBundleConverter;
	}
}
