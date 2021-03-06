/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.cmclient.catalog.editors.catalog;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * Default implementation. It looks in the database for the uniqueness of the code. 
 */
public class SkuOptionNameVerifierImpl implements SkuOptionNameVerifier {

	private final SkuOptionService skuOptionService = BeanLocator.getSingletonBean(ContextIdNames.SKU_OPTION_SERVICE, SkuOptionService.class);
	
	@Override
	public boolean verifySkuOptionKey(final String newValue) {
		return !skuOptionService.keyExists(newValue);
	}

	@Override
	public boolean verifySkuOptionValueKey(final String newValue) {
		return !skuOptionService.optionValueKeyExists(newValue);
	}

}
