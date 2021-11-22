/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import java.util.Optional;

import org.springframework.core.convert.converter.Converter;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

/**
 * Converts {@code com.elasticpath.domain.catalog.BundleConstituent} to {@code com.elasticpath.xpf.connectivity.context.BundleConstituent}.
 */
public class BundleConstituentConverter implements Converter<StoreDomainContext<BundleConstituent>, XPFBundleConstituent> {
	private ProductConverter xpfProductConverter;
	private ProductSkuConverter xpfProductSkuConverter;

	@Override
	public XPFBundleConstituent convert(final StoreDomainContext<BundleConstituent> storeDomainContext) {
		BundleConstituent bundleConstituent = storeDomainContext.getDomain();
		Optional<Store> storeOptional = storeDomainContext.getStore();

		if (bundleConstituent.getConstituent().isProduct()) {
			Product product = bundleConstituent.getConstituent().getProduct();
			XPFProduct xpfProduct = xpfProductConverter.convert(new StoreDomainContext<>(product, storeOptional));

			return new XPFBundleConstituent(xpfProduct);
		}
		if (bundleConstituent.getConstituent().isProductSku()) {
			ProductSku productSku = bundleConstituent.getConstituent().getProductSku();
			XPFProductSku xpfProductSku = xpfProductSkuConverter.convert(new StoreDomainContext<>(productSku, storeOptional));

			return new XPFBundleConstituent(xpfProductSku);
		}

		throw new EpServiceException("Product and sku mustn't be both null");
	}

	public void setXpfProductConverter(final ProductConverter xpfProductConverter) {
		this.xpfProductConverter = xpfProductConverter;
	}

	public void setXpfProductSkuConverter(final ProductSkuConverter xpfProductSkuConverter) {
		this.xpfProductSkuConverter = xpfProductSkuConverter;
	}
}
