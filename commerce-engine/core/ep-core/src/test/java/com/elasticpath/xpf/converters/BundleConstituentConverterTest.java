/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;

@RunWith(MockitoJUnitRunner.class)
public class BundleConstituentConverterTest {
	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ConstituentItem constituentItem;
	@Mock
	private Product product;
	@Mock
	private XPFProduct contextProduct;
	@Mock
	private ProductSku productSku;
	@Mock
	private XPFProductSku contextProductSku;
	@Mock
	private ProductConverter productConverter;
	@Mock
	private ProductSkuConverter productSkuConverter;

	@InjectMocks
	private BundleConstituentConverter bundleConstituentConverter;

	@Test
	public void testConvertWithProduct() {
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.isProduct()).thenReturn(true);
		when(constituentItem.getProduct()).thenReturn(product);

		when(productConverter.convert(new StoreDomainContext<>(product, Optional.empty()))).thenReturn(contextProduct);

		XPFBundleConstituent contextBundleConstituent =
				bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent, Optional.empty()));

		assertEquals(contextProduct, contextBundleConstituent.getProduct());
		assertNull(contextBundleConstituent.getProductSku());
	}

	@Test
	public void testConvertWithProductSku() {
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.isProductSku()).thenReturn(true);
		when(constituentItem.getProductSku()).thenReturn(productSku);

		when(productSkuConverter.convert(new StoreDomainContext<>(productSku, Optional.empty()))).thenReturn(contextProductSku);

		XPFBundleConstituent contextBundleConstituent =
				bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent, Optional.empty()));

		assertEquals(contextProductSku, contextBundleConstituent.getProductSku());
		assertNull(contextBundleConstituent.getProduct());
	}

	@Test
	public void testConvertWithoutBothProductAndProductSku() {
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.isProductSku()).thenReturn(false);
		when(constituentItem.isProduct()).thenReturn(false);

		assertThatThrownBy(() -> bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent, Optional.empty())))
				.isInstanceOf(EpServiceException.class)
				.hasMessageContaining("Product and sku mustn't be both null");
	}
}