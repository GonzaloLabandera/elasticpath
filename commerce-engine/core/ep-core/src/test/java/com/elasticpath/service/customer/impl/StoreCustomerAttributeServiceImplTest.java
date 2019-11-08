/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.StoreCustomerAttribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.store.StoreService;

@RunWith(MockitoJUnitRunner.class)
public class StoreCustomerAttributeServiceImplTest {

	private static final String GUID = "guid";

	private static final String ATTRIBUTE_KEY = "attributeKey";

	private static final String STORE_CODE = "storeCode";
	private static final String CUSTOMER_ATTRIBUTES_FIND_BY_STORE = "CUSTOMER_ATTRIBUTES_FIND_BY_STORE";

	@InjectMocks
	private StoreCustomerAttributeServiceImpl storeCustomerAttributeService;

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private StoreService storeService;

	@Mock
	private StoreCustomerAttribute storeCustomerAttribute;

	@Mock
	private Store store;

	@Before
	public void setup() {
		doReturn(store).when(storeService).findStoreWithCode(STORE_CODE);
		doReturn(STORE_CODE).when(store).getCode();
		doReturn(GUID).when(storeCustomerAttribute).getGuid();
	}

	@Test
	public void testFindByGuid() {
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery("CUSTOMER_ATTRIBUTE_FIND_BY_GUID", GUID);

		assertThat(storeCustomerAttributeService.findByGuid(GUID).get()).isEqualTo(storeCustomerAttribute);
	}

	@Test
	public void testFindByGuidReturnsNullWhenNotFound() {
		doReturn(Collections.emptyList())
				.when(persistenceEngine).retrieveByNamedQuery("CUSTOMER_ATTRIBUTE_FIND_BY_GUID", GUID);

		assertThat(storeCustomerAttributeService.findByGuid(GUID).isPresent()).isFalse();
	}

	@Test
	public void testFindByStoreCodeAndAttributeKey() {
		doReturn(store).when(storeService).findStoreWithCode(STORE_CODE);
		doReturn(STORE_CODE).when(store).getCode();
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery("CUSTOMER_ATTRIBUTE", STORE_CODE, ATTRIBUTE_KEY);

		assertThat(storeCustomerAttributeService.findByStoreCodeAndAttributeKey(STORE_CODE, ATTRIBUTE_KEY).get()).isEqualTo(storeCustomerAttribute);
	}

	@Test
	public void testFindByGuids() {
		final List<String> guids = Collections.singletonList(GUID);
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQueryWithList("CUSTOMER_ATTRIBUTE_FIND_BY_GUIDS", "list", guids);

		assertThat(storeCustomerAttributeService.findByGuids(guids)).containsOnly(storeCustomerAttribute);
	}

	@Test
	public void testFindByStore() {
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery(CUSTOMER_ATTRIBUTES_FIND_BY_STORE, STORE_CODE);

		assertThat(storeCustomerAttributeService.findByStore(STORE_CODE)).containsOnly(storeCustomerAttribute);
	}

	@Test
	public void testFindAll() {
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery("CUSTOMER_ATTRIBUTES_FIND_ALL");

		assertThat(storeCustomerAttributeService.findAll()).containsOnly(storeCustomerAttribute);
	}

	@Test
	public void testUpdateAllAdd() {
		doReturn(Lists.newArrayList())
				.when(persistenceEngine).retrieveByNamedQuery(CUSTOMER_ATTRIBUTES_FIND_BY_STORE, STORE_CODE);
		final Map<String, StoreCustomerAttribute> storeCustomerAttributes = Maps.newHashMap();
		storeCustomerAttributes.put(GUID, storeCustomerAttribute);
		storeCustomerAttributeService.updateAll(STORE_CODE, storeCustomerAttributes);
		verify(persistenceEngine).save(storeCustomerAttribute);

	}

	@Test
	public void testUpdateAllRemove() {
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery(CUSTOMER_ATTRIBUTES_FIND_BY_STORE, STORE_CODE);
		storeCustomerAttributeService.updateAll(STORE_CODE, Collections.emptyMap());
		verify(persistenceEngine).delete(storeCustomerAttribute);

	}

	@Test
	public void testUpdateAllUpdate() {
		doReturn(Arrays.asList(storeCustomerAttribute))
				.when(persistenceEngine).retrieveByNamedQuery(CUSTOMER_ATTRIBUTES_FIND_BY_STORE, STORE_CODE);
		final Map<String, StoreCustomerAttribute> storeCustomerAttributes = Maps.newHashMap();
		storeCustomerAttributes.put(GUID, storeCustomerAttribute);
		storeCustomerAttributeService.updateAll(STORE_CODE, storeCustomerAttributes);
		verify(persistenceEngine).saveOrUpdate(storeCustomerAttribute);

	}
}
