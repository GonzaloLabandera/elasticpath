/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.orderpaymentapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link StorePaymentProviderConfigService}.
 */
public class StorePaymentProviderConfigServiceTest extends DbTestCase {

	private static final String STORE_PAYMENT_PROVIDER_CONFIG_GUID = "GUID";
	private static String paymentProviderConfigGuid;

	@Autowired
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Autowired
	private StoreService storeService;

	@Before
	public void setUp() {
		paymentProviderConfigGuid = Utils.uniqueCode("PAYMENTPROVIDERCONFIG");
	}

	@Test
	@DirtiesDatabase
	public void ensureFindByCustomerFindsCustomerPaymentInstruments() {
        StorePaymentProviderConfig entity = createTestEntity();
        Store store = scenario.getStore();
        entity.setStoreCode(store.getCode());

        storePaymentProviderConfigService.saveOrUpdate(entity);

        Collection<StorePaymentProviderConfig> configs = storePaymentProviderConfigService.findByStore(store);
        Iterator<StorePaymentProviderConfig> iterator = configs.iterator();
        assertTrue("No StorePaymentProviderConfig entities were found for this Store", iterator.hasNext());
        assertEquals("Wrong StorePaymentProviderConfig associated with the Store", entity.getUidPk(), iterator.next().getUidPk());
    }

	@Test
	@DirtiesDatabase
	public void ensureFindStoreNamesByPaymentProviderConfigGuid() {
        StorePaymentProviderConfig entity = createTestEntity();
        Store store = scenario.getStore();
        entity.setStoreCode(store.getCode());

        storePaymentProviderConfigService.saveOrUpdate(entity);

        Collection<String> storeNames =
                storePaymentProviderConfigService.findStoreNameByProviderConfig(entity.getPaymentProviderConfigGuid());
        assertThat(storeNames)
                .containsOnly(store.getName());
    }

	@Test
	@DirtiesDatabase
	public void removingStorePaymentProviderConfigDoesNotRemoveStore() {
        Store store = scenario.getStore();

        StorePaymentProviderConfig entity = createTestEntity();
        entity.setStoreCode(store.getCode());

        storePaymentProviderConfigService.saveOrUpdate(entity);
        storePaymentProviderConfigService.remove(entity);

        final Store persistedStore = storeService.getStore(store.getUidPk());
        assertNotNull("Store was unexpectedly removed", persistedStore);
    }

	@Test
	@DirtiesDatabase
	public void ensureFindByGuidReturnsExpectedStorePaymentProviderConfig() {
		StorePaymentProviderConfig entity = createTestEntity();

		storePaymentProviderConfigService.saveOrUpdate(entity);

		StorePaymentProviderConfig config = storePaymentProviderConfigService.findByGuid(STORE_PAYMENT_PROVIDER_CONFIG_GUID);
		assertEquals("StorePaymentProviderConfig GUIDs do not match.", config.getGuid(), entity.getGuid());
	}

	private StorePaymentProviderConfig createTestEntity() {
		StorePaymentProviderConfig entity = getBeanFactory().getPrototypeBean(ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG,
				StorePaymentProviderConfig.class);
		entity.setGuid(STORE_PAYMENT_PROVIDER_CONFIG_GUID);
		entity.setPaymentProviderConfigGuid(paymentProviderConfigGuid);
		return entity;
	}

}
