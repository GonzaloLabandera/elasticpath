/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.persister.testscenarios;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;

public class CustomerScenario extends AbstractScenario {
	private Store store;

	private Customer customer;

	private CustomerAddress address;

	private Shopper shopper;

	@Override
	public void initialize() {
		Catalog catalog = getDataPersisterFactory().getCatalogTestPersister().persistDefaultMasterCatalog();
		Warehouse warehouse = getDataPersisterFactory().getStoreTestPersister().persistDefaultWarehouse();

		store = getDataPersisterFactory().getStoreTestPersister().persistDefaultStore(catalog, warehouse);
		customer = getDataPersisterFactory().getStoreTestPersister().createDefaultCustomer(store);
		address = customer.getPreferredBillingAddress();
		shopper = getDataPersisterFactory().getStoreTestPersister().persistShopperWithAssociatedEntities(customer);
	}

	/**
	 * @return the customer
	 */
	public Customer getCustomer() {
		return customer;
	}

	/**
	 * @return the address
	 */
	public CustomerAddress getAddress() {
		return address;
	}

	/**
	 * @return the shopper
	 */
	public Shopper getShopper() {
		return shopper;
	}
}
