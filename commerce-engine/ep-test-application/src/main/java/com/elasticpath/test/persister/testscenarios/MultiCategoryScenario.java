/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.persister.testscenarios;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;

/**
 * This multi category scenario populates database with a linked category.
 */
public class MultiCategoryScenario extends SimpleStoreScenario {

	/**
	 * The virtual catalog used by the store.
	 */
	private Catalog virtualCatalog;

	/**
	 * The store based on the virtual catalog.
	 */
	private Store virtualStore;

	/**
	 * The linked category.
	 */
	private Category linkedCategory;

	public MultiCategoryScenario() {
		super();
	}

	/**
	 * Populate database with default common data.
	 */
	@Override
	public void initialize() {
		super.initialize();
		virtualCatalog = getDataPersisterFactory().getCatalogTestPersister().createPersistedVirtualCatalog();
		virtualStore = getDataPersisterFactory().getStoreTestPersister().persistDefaultStore(virtualCatalog, warehouse);
		linkedCategory = getDataPersisterFactory().getCatalogTestPersister().persistLinkedCategory(virtualCatalog, category);
	}

	public Catalog getVirtualCatalog() {
		return virtualCatalog;
	}

	public Category getLinkedCategory() {
		return linkedCategory;
	}

	public Store getVirtualStore() {
		return virtualStore;
	}
}


