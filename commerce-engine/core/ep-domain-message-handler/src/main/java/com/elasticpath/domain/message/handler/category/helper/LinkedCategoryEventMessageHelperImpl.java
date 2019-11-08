/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.message.handler.category.helper;

import java.util.List;
import java.util.stream.Collectors;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.service.store.StoreService;

/**
 * An implementation of {@link LinkedCategoryEventMessageHelper}.
 */
public class LinkedCategoryEventMessageHelperImpl implements LinkedCategoryEventMessageHelper {

	private final StoreService storeService;

	/**
	 * Constructor.
	 *
	 * @param storeService store data service.
	 */
	public LinkedCategoryEventMessageHelperImpl(final StoreService storeService) {
		this.storeService = storeService;
	}

	@Override
	public String getUnlinkedCategoryCode(final EventMessage eventMessage) {
		final String compoundGuid = eventMessage.getGuid();

		return compoundGuid.substring(0, compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER));
	}

	@Override
	public List<String> getUnlinkedCategoryStores(final EventMessage eventMessage) {
		final String compoundGuid = eventMessage.getGuid();
		final String catalogCode = compoundGuid.substring(compoundGuid.indexOf(Category.CATEGORY_LEGACY_GUID_DELIMITER)
				+ Category.CATEGORY_LEGACY_GUID_DELIMITER.length());

		return storeService.findStoresWithCatalogCode(catalogCode).stream().map(Store::getCode).collect(Collectors.toList());
	}

}
