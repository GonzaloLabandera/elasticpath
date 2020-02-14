/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.importer.importers.impl;

import com.elasticpath.common.dto.store.StoreDTO;
import com.elasticpath.domain.store.Store;
import com.elasticpath.importexport.importer.configuration.ImporterConfiguration;
import com.elasticpath.importexport.importer.importers.CollectionsStrategy;
import com.elasticpath.importexport.importer.types.CollectionStrategyType;
import com.elasticpath.importexport.importer.types.DependentElementType;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * A {@link CollectionsStrategy} for {@link Store}s. Clear out references to warehouses, tax codes, tax jurisdictions, payment gateways and supported
 * credit card types.
 */
public class StoreCollectionsStrategy implements CollectionsStrategy<Store, StoreDTO> {

	private final StorePaymentProviderConfigService storePaymentProviderConfigService;
	private final boolean clearWarehouses;
	private final boolean clearTaxCodes;
	private final boolean clearTaxJurisdictions;
	private final boolean clearCreditCardTypes;
	private final boolean clearStorePaymentProviderConfig;

	/**
	 * Default constructor.
	 *
	 * @param storePaymentProviderConfigService service to interact with payment provider config workflow.
	 * @param importerConfiguration current import configuration.
	 */
	public StoreCollectionsStrategy(final StorePaymentProviderConfigService storePaymentProviderConfigService,
									final ImporterConfiguration importerConfiguration) {
		this.storePaymentProviderConfigService = storePaymentProviderConfigService;

		clearWarehouses = importerConfiguration.getCollectionStrategyType(DependentElementType.WAREHOUSES).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
		clearTaxCodes = importerConfiguration.getCollectionStrategyType(DependentElementType.TAX_CODES).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
		clearTaxJurisdictions = importerConfiguration.getCollectionStrategyType(DependentElementType.TAX_JURISDICTIONS).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
		clearStorePaymentProviderConfig = importerConfiguration.getCollectionStrategyType(DependentElementType.STORE_PAYMENT_PROVIDER_CONFIG).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
		clearCreditCardTypes = importerConfiguration.getCollectionStrategyType(DependentElementType.CREDIT_CARD_TYPES).equals(
				CollectionStrategyType.CLEAR_COLLECTION);
	}

	@Override
	public void prepareCollections(final Store store, final StoreDTO dto) {
		if (clearWarehouses) {
			store.getWarehouses().clear();
		}

		if (clearTaxCodes) {
			store.getTaxCodes().clear();
		}
		if (clearTaxJurisdictions) {
			store.getTaxJurisdictions().clear();
		}
		if (clearCreditCardTypes) {
			store.getCreditCardTypes().clear();
		}
		if (clearStorePaymentProviderConfig && store.isPersisted()) {
			storePaymentProviderConfigService.deleteByStore(store);
		}

	}

	@Override
	public boolean isForPersistentObjectsOnly() {
		return false;
	}

}
