/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.persister.testscenarios;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.Warehouse;

/**
 * This scenario has a single store using a master catalog.
 */
public class StoreAndCatalogScenario extends AbstractScenario {

	/** The reference to the single store in this scenario. */
	protected Store store;

	/** The catalog used by the store. */
	protected Catalog catalog;

	/**.
	 * Populate database with default common data.
	 */
	@Override
	public void initialize() {
		final Warehouse warehouse = getDataPersisterFactory().getStoreTestPersister().persistDefaultWarehouse();
		catalog = getDataPersisterFactory().getCatalogTestPersister().persistDefaultMasterCatalog();
		store = getDataPersisterFactory().getStoreTestPersister().persistDefaultStore(catalog, warehouse);
		store.setCatalog(catalog);
	}

	/**
	 * Get the populated store.
	 *
	 * @return store
	 */
	public Store getStore() {
		return store;
	}

	/**
	 * Get the populated catalog.
	 *
	 * @return catalog
	 */
	public Catalog getCatalog() {
		return catalog;
	}
}
