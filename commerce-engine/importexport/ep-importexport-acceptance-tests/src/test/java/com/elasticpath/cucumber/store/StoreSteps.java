/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cucumber.store;


import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.base.Splitter;
import cucumber.api.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.StorePaymentProviderConfigImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;
import com.elasticpath.domain.store.StoreType;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.store.StoreService;

/**
 * Store steps.
 */
public class StoreSteps {

	@Autowired
	private StoreService storeService;

	@Autowired
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	/**
	 * Setup the tests store.
	 *
	 * @param storeMap store info.
	 */
	@Given("^a store with following values$")
	public void setUpStores(final Map<String, String> storeMap) {
		final Store store = new StoreImpl();

		store.setTimeZone(TimeZone.getTimeZone(storeMap.get("timezone")));
		store.setCountry(storeMap.get("store country"));
		store.setSubCountry(storeMap.get("store sub country"));
		store.setCode(storeMap.get("store code"));
		store.setName(storeMap.get("store name"));
		store.setDefaultCurrency(Currency.getInstance(storeMap.get("currency")));
		store.setStoreState(StoreState.UNDER_CONSTRUCTION);
		store.setStoreType(StoreType.B2C);
		store.setDefaultLocale(Locale.US);
		storeService.saveOrUpdate(store);

		final Store savedStore = storeService.findStoreWithCode(storeMap.get("store code"));

		@SuppressWarnings("UnstableApiUsage") final List<String> paymentProviderConfigGuids = Splitter.on(",")
				.omitEmptyStrings()
				.splitToList(storeMap.get("payment provider configs"));
		int guidIncrement = 0;
		for (String paymentProviderConfigGuid : paymentProviderConfigGuids) {
            final StorePaymentProviderConfig storePaymentProviderConfig = new StorePaymentProviderConfigImpl();
            storePaymentProviderConfig.setStoreCode(savedStore.getCode());
            storePaymentProviderConfig.setPaymentProviderConfigGuid(paymentProviderConfigGuid);
            storePaymentProviderConfig.setGuid("Store Payment Provider Config GUID " + guidIncrement);
            storePaymentProviderConfigService.saveOrUpdate(storePaymentProviderConfig);
            guidIncrement++;
        }
	}
}
