/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.common.dto.customer.StoreCustomerAttributeDTO;
import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.importexport.common.adapters.DomainAdapter;
import com.elasticpath.service.customer.StoreCustomerAttributeService;

/**
 * An importer for {@link StoreCustomerAttribute}s.
 */
public class StoreCustomerAttributeImporter extends AbstractImporterImpl<StoreCustomerAttribute, StoreCustomerAttributeDTO> {

	private DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> domainAdapter;

	private StoreCustomerAttributeService storeCustomerAttributeService;

	@Override
	public String getImportedObjectName() {
		return StoreCustomerAttributeDTO.ROOT_ELEMENT;
	}

	@Override
	protected String getDtoGuid(final StoreCustomerAttributeDTO dto) {
		return dto.getGuid();
	}

	@Override
	protected DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> getDomainAdapter() {
		return domainAdapter;
	}

	@Override
	protected StoreCustomerAttribute findPersistentObject(final StoreCustomerAttributeDTO dto) {
		return storeCustomerAttributeService.findByGuid(dto.getGuid()).orElse(null);
	}

	@Override
	protected void setImportStatus(final StoreCustomerAttributeDTO object) {
		getStatusHolder().setImportStatus(object.getGuid());
	}

	@Override
	public Class<? extends StoreCustomerAttributeDTO> getDtoClass() {
		return StoreCustomerAttributeDTO.class;
	}

	public void setDomainAdapter(
			final DomainAdapter<StoreCustomerAttribute, StoreCustomerAttributeDTO> domainAdapter) {
		this.domainAdapter = domainAdapter;
	}

	public void setStoreCustomerAttributeService(final StoreCustomerAttributeService storeCustomerAttributeService) {
		this.storeCustomerAttributeService = storeCustomerAttributeService;
	}
}
