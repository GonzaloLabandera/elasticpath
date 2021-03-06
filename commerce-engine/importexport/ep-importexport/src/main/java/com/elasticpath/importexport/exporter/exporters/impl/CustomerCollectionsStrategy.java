/*
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.exporter.exporters.impl;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.service.customer.CustomerService;

/**
 * Strategy which determines how collections in {@link Customer} are managed.
 */
public class CustomerCollectionsStrategy implements CollectionsStrategy<Customer, CustomerDTO> {

	private final boolean clearAddresses;
	private final boolean clearGroups;

	private final CustomerService customerService;

	/**
	 * Constructor which sets individual collection strategies.
	 * 
	 * @param importerConfiguration the {@link ImporterConfiguration} to set.
	 * @param customerService the {@link CustomerService} to set.
	 */
	public CustomerCollectionsStrategy(final ImporterConfiguration importerConfiguration, final CustomerService customerService) {

		this.customerService = customerService;

		clearAddresses = importerConfiguration.getCollectionStrategyType(DependentElementType.ADDRESSES).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
		clearGroups = importerConfiguration.getCollectionStrategyType(DependentElementType.CUSTOMER_GROUPS).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
	}

	@Override
	public void prepareCollections(final Customer customer, final CustomerDTO dto) {

		if (clearAddresses) {
			customerService.removeAllAddresses(customer);
		}

		if (clearGroups) {
			customer.getCustomerGroups().clear();
		}

	}

	@Override
	public boolean isForPersistentObjectsOnly() {
		return true;
	}
}
