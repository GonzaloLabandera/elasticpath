/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.importexport.importer.changesetsupport.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.domain.objectgroup.impl.BusinessObjectDescriptorImpl;
import com.elasticpath.importexport.common.dto.products.ProductDTO;
import com.elasticpath.importexport.common.dto.products.bundles.ProductBundleDTO;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;

@RunWith(MockitoJUnitRunner.class)
public class ProductObjectDescriptorPopulatorTest {

	@InjectMocks
	private ProductObjectDescriptorPopulator productObjectDescriptorPopulator;

	@Mock
	private MethodObjectDescriptorPopulator productDelegate;

	@Mock
	private MethodObjectDescriptorPopulator bundleDelegate;

	@Test
	public void testPopulateForProduct() {
		final BusinessObjectDescriptor aDescriptor = new BusinessObjectDescriptorImpl();
		final BusinessObjectDescriptor expectedDescriptor = new BusinessObjectDescriptorImpl();
		final ProductDTO dto = new ProductDTO();
		dto.setBundle(false);

		when(productDelegate.populate(aDescriptor, dto)).thenReturn(expectedDescriptor);

		final BusinessObjectDescriptor populate = productObjectDescriptorPopulator.populate(aDescriptor, dto);

		assertThat(populate).isNotNull();
		assertThat(populate).isSameAs(expectedDescriptor);
	}

	@Test
	public void testPopulateForBundle() {
		final BusinessObjectDescriptor aDescriptor = new BusinessObjectDescriptorImpl();
		final BusinessObjectDescriptor expectedDescriptor = new BusinessObjectDescriptorImpl();
		final ProductDTO dto = new ProductDTO();
		dto.setBundle(true);

		when(bundleDelegate.populate(aDescriptor, dto)).thenReturn(expectedDescriptor);

		final BusinessObjectDescriptor populate = productObjectDescriptorPopulator.populate(aDescriptor, dto);

		assertThat(populate).isNotNull();
		assertThat(populate).isSameAs(expectedDescriptor);
	}

	@Test
	public void testPopulateForInvalidDto() {
		final BusinessObjectDescriptor aDescriptor = new BusinessObjectDescriptorImpl();
		final ProductBundleDTO dto = new ProductBundleDTO();

		final Throwable thrown = catchThrowable(() -> productObjectDescriptorPopulator.populate(aDescriptor, dto));

		assertThat(thrown)
				.isNotNull()
				.isInstanceOf(ImportRuntimeException.class);

		final ImportRuntimeException importRuntimeException = (ImportRuntimeException) thrown;
		assertThat(importRuntimeException.getIEMessage())
				.isNotNull();

		assertThat(importRuntimeException.getIEMessage().getCode())
				.isEqualTo("IE-31013");

		assertThat(importRuntimeException.getIEMessage().getParams())
				.containsExactly(ProductBundleDTO.class.getCanonicalName(), ProductDTO.class.getCanonicalName());
	}
}