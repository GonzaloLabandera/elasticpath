/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.catalog.impl;

import static com.elasticpath.domain.catalog.ProductAssociationType.DEPENDENT_ITEM;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.catalog.DependentItemLookup;

/**
 * Default implementation of {@link DependentItemLookup}.
 * <p>
 * Uses Product Associations - specifically the new {@link com.elasticpath.domain.catalog.ProductAssociationType#DEPENDENT_ITEM} type - to identify
 * a given sku's dependent items.
 */
public class DependentItemLookupImpl implements DependentItemLookup {

	private PersistenceEngine persistenceEngine;

	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	@Override
	public Map<String, Integer> findDependentItemsForSku(final Store store, final ProductSku parentSku) {
		Objects.requireNonNull(store, "Store is required");
		Objects.requireNonNull(parentSku, "Parent SKU is required");

		final int sourceProductCode = parentSku.getProduct().getUidPkInt();
		final long currentCatalogCode = store.getCatalog().getUidPk();

		final List<Object[]> dependentItemsWithQuantity = getPersistenceEngine().retrieveByNamedQuery("DEPENDENT_ITEMS_WITH_QUANTITY",
				currentCatalogCode, DEPENDENT_ITEM, sourceProductCode);

		return dependentItemsWithQuantity.stream().collect(Collectors.toMap(resultItems -> (String) resultItems[0],
				resultItems -> (Integer) resultItems[1]));

	}



}
