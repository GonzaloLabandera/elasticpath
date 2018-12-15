/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.importexport.importer.changesetsupport.impl;

import com.elasticpath.common.dto.Dto;
import com.elasticpath.domain.objectgroup.BusinessObjectDescriptor;
import com.elasticpath.importexport.common.dto.products.ProductDTO;
import com.elasticpath.importexport.common.exception.runtime.ImportRuntimeException;
import com.elasticpath.importexport.importer.changesetsupport.ObjectDescriptorPopulator;

/**
 * {@link ObjectDescriptorPopulator} implementation specific for ProductDTO processing.
 */
public class ProductObjectDescriptorPopulator implements ObjectDescriptorPopulator {

	private MethodObjectDescriptorPopulator productDelegate;

	private MethodObjectDescriptorPopulator bundleDelegate;

	@Override
	public BusinessObjectDescriptor populate(final BusinessObjectDescriptor descriptor, final Dto dto) {
		if (dto instanceof ProductDTO) {
			return populate(descriptor, (ProductDTO) dto);
		} else {
			throw new ImportRuntimeException("IE-31013", dto.getClass().getCanonicalName(), ProductDTO.class.getCanonicalName());
		}
	}

	/**
	 * Delegates the population of the <code>descriptor</code> to either a Product type populator or a ProductBundle type populator, according
	 * to which type of Product the <code>productDTO</code> represents.
	 *
	 * @param descriptor the descriptor to be populated.
	 * @param productDTO the Dto from which to populate the descriptor.
	 * @return a populated Business Object Descriptor.
	 */
	protected BusinessObjectDescriptor populate(final BusinessObjectDescriptor descriptor, final ProductDTO productDTO) {
		if (productDTO.isBundle()) {
			return bundleDelegate.populate(descriptor, productDTO);
		} else {
			return productDelegate.populate(descriptor, productDTO);
		}
	}

	/**
	 * Sets the productDelegate.
	 *
	 * @param productDelegate the productDelegate.
	 */
	public void setProductDelegate(final MethodObjectDescriptorPopulator productDelegate) {
		this.productDelegate = productDelegate;
	}

	protected MethodObjectDescriptorPopulator getProductDelegate() {
		return productDelegate;
	}

	/**
	 * Sets the bundleDelegate.
	 *
	 * @param bundleDelegate the bundleDelegate.
	 */
	public void setBundleDelegate(final MethodObjectDescriptorPopulator bundleDelegate) {
		this.bundleDelegate = bundleDelegate;
	}

	protected MethodObjectDescriptorPopulator getBundleDelegate() {
		return bundleDelegate;
	}
}
