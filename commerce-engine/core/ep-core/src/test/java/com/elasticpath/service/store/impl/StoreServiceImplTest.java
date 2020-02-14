/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.store.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.search.UpdateType;
import com.elasticpath.domain.store.CreditCardType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.StoreState;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.persistence.api.FetchGroupLoadTuner;
import com.elasticpath.persistence.api.FlushMode;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.query.SearchCriteria;
import com.elasticpath.service.store.StoreService;

/**
 * Test case for <code>StoreServiceImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.GodClass" })
@RunWith(MockitoJUnitRunner.class)
public class StoreServiceImplTest {

	private static final String STORE_DISPLAYABLE = "STORE_DISPLAYABLE_FLAG";

	private static final String STORE_NAME = "STORE_NAME";

	private static final String FIND_ALL_STORES = "FIND_ALL_STORES";

	private static final String FIND_ALL_COMPLETE_STORES = "FIND_ALL_COMPLETE_STORES";

	private static final String FIND_STORE_WITH_CODE = "FIND_STORE_WITH_CODE";

	private static final String FIND_ALL_STORE_UIDS = "FIND_ALL_STORE_UIDS";

	private static final String STORE_WITH_ORDER_IN_USE = "STORE_WITH_ORDER_IN_USE";

	private static final String STORE_WITH_USER_IN_USE = "STORE_WITH_USER_IN_USE";

	@InjectMocks
	private StoreServiceImpl storeServiceImpl;

	private static final String PLACEHOLDER_FOR_LIST = "list";

	@Mock
	private PersistenceEngine persistenceEngine;

	@Mock
	private FetchPlanHelper fetchPlanHelper;

	@Mock
	private IndexNotificationService indexNotificationService;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception in case of any errors
	 */
	@Before
	public void setUp() throws Exception {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRODUCT_SEARCH_CRITERIA, ProductSearchCriteria.class))
				.thenReturn(new ProductSearchCriteria());
	}

	/**
	 * Tests the save or update method for store service.
	 */
	@Test
	public void testSaveOrUpdate() {
		final Store store = new StoreImpl();
		final List<SearchCriteria> result = new ArrayList<>();
		result.add(new ProductSearchCriteria());
		final MockUpdateNotification mockUpdateNotification = mock(MockUpdateNotification.class);

		when(persistenceEngine.saveOrUpdate(store)).thenReturn(store);

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			protected List<SearchCriteria> buildUpdateSearchCriteriaList(final Store store) {
				return result;
			}

			@Override
			protected void notifyObjectsByUpdateCriteria(final List<SearchCriteria> updateSearchCriteriaList) {
				mockUpdateNotification.mockMethod(updateSearchCriteriaList);
			}
		};

		storeServiceImpl.setPersistenceEngine(persistenceEngine);
		storeServiceImpl.saveOrUpdate(store);

		verify(persistenceEngine).saveOrUpdate(store);
		verify(mockUpdateNotification).mockMethod(result);
	}

	/**
	 * Tests notifyObjectsByUpdateCriteria() method.
	 */
	@Test
	public void testNotifyObjectsByUpdateCriteria() {
		final List<SearchCriteria> result = new ArrayList<>();
		final ProductSearchCriteria productSearchCriteria = new ProductSearchCriteria();
		result.add(productSearchCriteria);

		storeServiceImpl.notifyObjectsByUpdateCriteria(result);
		verify(indexNotificationService).addViaQuery(UpdateType.UPDATE, productSearchCriteria, false);
	}

	/**
	 * Tests buildProductUpdateCriteria() method with given store.
	 */
	@Test
	public void testBuildProductUpdateCriteria() {
		storeServiceImpl = new StoreServiceImpl() {
			@Override
			protected boolean updateProductsNotificationRequired(final Store storeBeforePersistence) {
				return false;
			}
		};

		assertThat(storeServiceImpl.buildProductUpdateCriteria(null)).isNull();

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			protected boolean updateProductsNotificationRequired(final Store storeBeforePersistence) {
				return true;
			}

			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return beanFactory.getPrototypeBean(name, clazz);
			}
		};

		Store store = new StoreImpl();
		Catalog catalog = new CatalogImpl();
		catalog.setCode("catalogCode");
		store.setCatalog(catalog);

		SearchCriteria productUpdateCriteria = storeServiceImpl.buildProductUpdateCriteria(store);
		assertThat(productUpdateCriteria).isInstanceOf(ProductSearchCriteria.class);

		ProductSearchCriteria productSearchCriteria = (ProductSearchCriteria) productUpdateCriteria;
		assertThat(productSearchCriteria.getLocale()).isEqualTo(Locale.US);
		assertThat(productSearchCriteria.getCatalogCode()).isEqualTo(catalog.getCode());
	}

	/**
	 * Tests updateProductsNotificationRequired() method with incomplete store.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredIncompleteStore() {
		Store store = new StoreImpl();
		store.setStoreState(StoreState.UNDER_CONSTRUCTION);

		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isFalse();

		store.setUidPk(1L);
		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isFalse();
	}

	/**
	 * Tests updateProductsNotificationRequired() method with complete not persisted store.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredCompleteNotPersistedStore() {
		Store store = new StoreImpl();
		store.setStoreState(StoreState.OPEN);
		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isTrue();
	}

	/**
	 * Tests updateProductsNotificationRequired() method with complete persisted store if displayablity not changed.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredCompletePersistedStoreDisplayabilityNotChanged() {
		final Store store = new StoreImpl();
		store.setUidPk(1L);
		store.setStoreState(StoreState.OPEN);
		store.setDisplayOutOfStock(true);

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			boolean isGivenStoreBecomingActive(final Store store) {
				return false;
			}

		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);

		when(persistenceEngine.retrieveByNamedQuery(STORE_DISPLAYABLE, store.getUidPk())).thenReturn(ImmutableList.of(store.isDisplayOutOfStock()));

		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isFalse();
		verify(persistenceEngine).retrieveByNamedQuery(STORE_DISPLAYABLE, store.getUidPk());
	}

	/**
	 * Tests updateProductsNotificationRequired() method with complete persisted store if displayablity changed.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredCompletePersistedStoreDisplayabilityChanged() {
		final Store store = new StoreImpl();
		store.setUidPk(1L);
		store.setStoreState(StoreState.OPEN);
		store.setDisplayOutOfStock(true);

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			boolean isGivenStoreBecomingActive(final Store store) {
				return false;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);

		when(persistenceEngine.retrieveByNamedQuery(STORE_DISPLAYABLE, store.getUidPk())).thenReturn(ImmutableList.of(!store.isDisplayOutOfStock()));

		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isTrue();
		verify(persistenceEngine).retrieveByNamedQuery(STORE_DISPLAYABLE, store.getUidPk());
	}

	/**
	 * Tests updateProductsNotificationRequired() method with complete persisted store if it is becoming active.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredCompletePersistedStoreBecomingActive() {
		final Store store = new StoreImpl();
		store.setUidPk(1L);
		store.setStoreState(StoreState.OPEN);

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			boolean isStoreDisplayabilityDifferentThanPersistedStoreDisplayability(final Store store) {
				return false;
			}

			@Override
			public Store getStore(final long storeUid) throws EpServiceException {
				final Store oldStore = new StoreImpl();
				oldStore.setStoreState(StoreState.UNDER_CONSTRUCTION);
				return oldStore;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);

		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isTrue();
	}

	/**
	 * Tests updateProductsNotificationRequired() method with complete persisted store if it is not becoming active.
	 */
	@Test
	public void testUpdateProductsNotificationRequiredCompletePersistedStoreNotBecomingActive() {
		final Store store = new StoreImpl();
		store.setUidPk(1L);
		store.setStoreState(StoreState.OPEN);

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			boolean isStoreDisplayabilityDifferentThanPersistedStoreDisplayability(final Store store) {
				return false;
			}

			@Override
			public Store getStore(final long storeUid) throws EpServiceException {
				final Store oldStore = new StoreImpl();
				oldStore.setStoreState(StoreState.OPEN);
				return oldStore;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);

		assertThat(storeServiceImpl.updateProductsNotificationRequired(store)).isFalse();
	}

	/**
	 * Tests getPersistedStoreName() method for given store.
	 */
	@Test
	public void testGetPersistedStoreName() {
		final String persistedStoreName = "persistedStoreName";

		when(persistenceEngine.retrieveByNamedQuery(STORE_NAME, 1L)).thenReturn(ImmutableList.of(persistedStoreName));

		assertThat(storeServiceImpl.getPersistedStoreName(1L)).isEqualTo(persistedStoreName);
		verify(persistenceEngine).retrieveByNamedQuery(STORE_NAME, 1L);
	}

	/**
	 * Test method for 'com.elasticpath.service.store.StoreServiceImpl.remove(Store)'.
	 */
	@Test
	public void testRemove() {
		final Store store = mock(Store.class);
		storeServiceImpl.remove(store);
		verify(persistenceEngine).delete(store);
	}

	/**
	 * Test that getStore() and getObject() return the same store when given a valid UIDPK.
	 */
	@Test
	public void testGetStore() {
		final StoreImpl store = mock(StoreImpl.class);
		final long uidpk = 1234L;

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			Store getPersistentStore(final long uidPk) {
				return store;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);
		storeServiceImpl.setIndexNotificationService(indexNotificationService);

		assertThat(storeServiceImpl.getStore(uidpk)).isEqualTo(store);
		assertThat(storeServiceImpl.getObject(uidpk)).isEqualTo(store);
	}

	/**
	 * Test that getStore() returns null when given a UID for a store that doesn't exist.
	 */
	@Test
	public void testGetStoreNonExisting() {
		final long nonExistUid = 3456L;

		storeServiceImpl = new StoreServiceImpl() {
			@Override
			Store getPersistentStore(final long uidPk) {
				return null;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);
		storeServiceImpl.setIndexNotificationService(indexNotificationService);

		assertThat(storeServiceImpl.getStore(nonExistUid)).isNull();
	}

	/**
	 * Test that getStore() returns a new non-persistent Store instance when given a UID <= 0.
	 */
	@Test
	public void testGetStoreNew() {
		final Store store = mock(Store.class);
		when(store.getUidPk()).thenReturn(0L);

		storeServiceImpl = new StoreServiceImpl() {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
				return (T) store;
			}
		};
		storeServiceImpl.setPersistenceEngine(persistenceEngine);
		assertThat(storeServiceImpl.getStore(0).getUidPk()).isEqualTo(0);
	}


	/**
	 * Test that findStoreWithCode returns the store from the persistence layer that has the given code.
	 */
	@Test
	public void testFindStoreWithCodeExisting() {
		final List<Store> storeList = new ArrayList<>();
		final String existingCode = "test code";
		final Store store = mock(Store.class);
		storeList.add(store);
		doReturn(storeList).when(persistenceEngine).retrieveByNamedQuery(FIND_STORE_WITH_CODE, FlushMode.COMMIT, existingCode);

		assertThat(storeServiceImpl.findStoreWithCode(existingCode)).isEqualTo(storeList.get(0));
		verify(persistenceEngine).retrieveByNamedQuery(FIND_STORE_WITH_CODE, FlushMode.COMMIT, existingCode);
	}

	/**
	 * Test that findStoreWithCode returns null if the store with the given code doesn't exist.
	 */
	@Test
	public void testFindStoreWithCodeNonExisting() {
		final List<Store> storeList = new ArrayList<>();
		final String nonExistingCode = "non existing code";
		doReturn(storeList).when(persistenceEngine).retrieveByNamedQuery(FIND_STORE_WITH_CODE, FlushMode.COMMIT, nonExistingCode);
		assertThat(storeServiceImpl.findStoreWithCode(nonExistingCode)).isNull();
		verify(persistenceEngine).retrieveByNamedQuery(FIND_STORE_WITH_CODE, FlushMode.COMMIT, nonExistingCode);
	}

	/**
	 * Test that a null store or url always counts as unique url.
	 */
	@Test
	public void testIsStoreUniqueForStateNull() {
		final Store store = mock(Store.class);

		when(store.getUrl()).thenReturn(null);

		assertThat(storeServiceImpl.isStoreUrlUniqueForState(null, StoreState.OPEN))
			.as("null store should just return true")
			.isTrue();
		assertThat(storeServiceImpl.isStoreUrlUniqueForState(store, StoreState.OPEN))
			.as("null url should return true")
			.isTrue();
		verify(store).getUrl();
	}

	/**
	 * Test that if a store with the same URL as the given store's URL is already persistent (differing
	 * only by a trailing slash) then the given store's URL is deemed non-unique.
	 */
	@Test
	public void testIsStoreUniqueForStateWithUrlInUse() {

		final Store persistentStore = mock(Store.class, "persistentStore");
		final String persistentStoreUrl = "http://my.store.url";

		final Store newStore = mock(Store.class, "newStore");
		final String newStoreUrl = "http://my.store.url/";

		when(persistentStore.getUrl()).thenReturn(persistentStoreUrl);
		when(newStore.getUrl()).thenReturn(newStoreUrl);


		StoreServiceImpl service = new StoreServiceImpl() {
			@Override
			List<Store> findStoresWithState(final StoreState state) {
				return ImmutableList.of(persistentStore);
			}
		};

		assertThat(service.isStoreUrlUniqueForState(newStore, StoreState.OPEN))
			.as("The new store's URL already exists for another open store (without the given slash), so should be regarded as non-unique")
			.isFalse();
	}

	/**
	 * Test that a non-existent url counts as unique.
	 */
	@Test
	public void testIsStoreUniqueForStateWithUrlNotUsed() {
		final Store persistentStore = mock(Store.class, "persistentStore");
		final String persistentStoreUrl = "http://my.store.url";

		final Store newStore = mock(Store.class, "newStore");
		final String newStoreUrl = "http://my.newstore.url/";

		when(persistentStore.getUrl()).thenReturn(persistentStoreUrl);
		when(newStore.getUrl()).thenReturn(newStoreUrl);

		StoreServiceImpl service = new StoreServiceImpl() {
			@Override
			List<Store> findStoresWithState(final StoreState state) {
				return ImmutableList.of(persistentStore);
			}
		};

		assertThat(service.isStoreUrlUniqueForState(newStore, StoreState.OPEN))
			.as("The new store's URL doesn't already exist, so should be regarded as unique")
			.isTrue();
	}

	/**
	 * Test method for 'com.elasticpath.service.store.StoreServiceImpl.findAllStoreUids()'.
	 */
	@Test
	public void testFindAllStoreUids() {
		final List<Long> uidList = new ArrayList<>();
		doReturn(uidList).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORE_UIDS);
		assertThat(storeServiceImpl.findAllStoreUids()).isEqualTo(uidList);
		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORE_UIDS);

		reset(persistenceEngine);

		// make sure the query returns something seemingly valid
		final long storeUid = 1234L;
		uidList.add(storeUid);
		doReturn(uidList).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORE_UIDS);
		assertThat(storeServiceImpl.findAllStoreUids()).isEqualTo(uidList);
		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORE_UIDS);
	}

	/**
	 * Test method for {@link StoreServiceImpl#findAllStores()}.
	 */
	@Test
	public void testFindAllStores() {
		final List<Store> storeList = new ArrayList<>();
		doReturn(storeList).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORES);
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

		assertThat(storeServiceImpl.findAllStores()).isEqualTo(storeList);

		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORES);
		verifyZeroInteractions(fetchPlanHelper);

		reset(persistenceEngine, fetchPlanHelper);

		// make sure the query returns something seemingly valid
		final Store store = mock(Store.class);
		doReturn(storeList).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORES);
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

		storeList.add(store);
		assertThat(storeServiceImpl.findAllStores()).isEqualTo(storeList);
		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORES);
		verifyZeroInteractions(fetchPlanHelper);
	}

	/**
	 * Test method for 'com.elasticpath.service.store.StoreServiceImpl.storeInUse()'.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testStoreInUse() {
		final long storeUid = 1L;
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_ORDER_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_USER_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_IMPORTJOB_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_SHIPPING_SERVICE_LEVEL_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_STORE_ASSOCIATION_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_PROMOTION_IN_USE", storeUid)).thenReturn(new ArrayList<>());
		when(persistenceEngine.retrieveByNamedQuery("STORE_WITH_CUSTOMER_ATTRIBUTE_IN_USE", storeUid)).thenReturn(new ArrayList<>());

		assertThat(storeServiceImpl.storeInUse(storeUid)).isFalse();

		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_ORDER_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_USER_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_IMPORTJOB_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_SHIPPING_SERVICE_LEVEL_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_STORE_ASSOCIATION_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_PROMOTION_IN_USE", storeUid);
		verify(persistenceEngine).retrieveByNamedQuery("STORE_WITH_CUSTOMER_ATTRIBUTE_IN_USE", storeUid);

		reset(persistenceEngine);

		final List<Long> storeList = new ArrayList<>();
		storeList.add(storeUid);

		when(persistenceEngine.retrieveByNamedQuery(STORE_WITH_ORDER_IN_USE, storeUid)).thenReturn(new ArrayList<>());
		doReturn(storeList).when(persistenceEngine).retrieveByNamedQuery(STORE_WITH_USER_IN_USE, storeUid);

		assertThat(storeServiceImpl.storeInUse(storeUid)).isTrue();

		verify(persistenceEngine).retrieveByNamedQuery(STORE_WITH_ORDER_IN_USE, storeUid);
		verify(persistenceEngine).retrieveByNamedQuery(STORE_WITH_USER_IN_USE, storeUid);
	}

	/**
	 * Test method for {@link StoreServiceImpl#findStoresWithCatalogUids(Collection)}.
	 */
	@Test
	public void testFindStoresWithCatalogUids() {
		final Collection<Long> uids = new ArrayList<>();
		uids.add(1L);
		uids.add(2L);

		final Collection<Store> result = new ArrayList<>();
		doReturn(result).when(persistenceEngine).retrieveByNamedQueryWithList("STORE_WITH_CATALOG_UID", PLACEHOLDER_FOR_LIST, uids);
		assertThat(storeServiceImpl.findStoresWithCatalogUids(uids)).isEqualTo(result);
		verify(persistenceEngine).retrieveByNamedQueryWithList("STORE_WITH_CATALOG_UID", PLACEHOLDER_FOR_LIST, uids);
	}

	/**
	 * Test method for {@link StoreServiceImpl#findAllStores(FetchGroupLoadTuner)}.
	 */
	@Test
	public void testFindAllStoresFGLoadTuner() {
		final Store store = mock(Store.class);
		final FetchGroupLoadTuner loadTuner = mock(FetchGroupLoadTuner.class);
		final List<Store> stores = ImmutableList.of(store);
		doReturn(stores).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_STORES);
		when(persistenceEngine.withLoadTuners(loadTuner)).thenReturn(persistenceEngine);

		assertThat(storeServiceImpl.findAllStores(loadTuner)).isEqualTo(stores);

		when(persistenceEngine.retrieveByNamedQuery(FIND_ALL_STORES)).thenReturn(new ArrayList<>());

		assertThat(storeServiceImpl.findAllStores(loadTuner)).isEmpty();

		verify(persistenceEngine, times(2)).retrieveByNamedQuery(FIND_ALL_STORES);
		verify(persistenceEngine, times(2)).withLoadTuners(loadTuner);
	}

	/**
	 * Test that findAllCompleteStores calls the method {@link StoreServiceImpl#findAllCompleteStores(FetchGroupLoadTuner)}
	 * with a null load tuner.
	 */
	@Test
	public void testFindAllCompleteStores() {
		final List<Store> completeStores = Collections.emptyList();
		doReturn(completeStores).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_COMPLETE_STORES, StoreState.UNDER_CONSTRUCTION,
				StoreState.RESTRICTED);
		when(persistenceEngine.withLoadTuners((LoadTuner[]) null)).thenReturn(persistenceEngine);

		assertThat(storeServiceImpl.findAllCompleteStores()).isEqualTo(completeStores);
		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_COMPLETE_STORES, StoreState.UNDER_CONSTRUCTION, StoreState.RESTRICTED);
		verify(persistenceEngine).withLoadTuners((LoadTuner[]) null);
	}

	/**
	 * Test that findAllCompleteStores(LoadTuner) calls the persistence engine with the appropriate named
	 * query and that the fetch plan is configured with the given load tuner.
	 */
	@Test
	public void testFindAllCompleteStoresWithLoadTuner() {
		final List<Store> completeStores = Collections.emptyList();
		final FetchGroupLoadTuner loadTuner = mock(FetchGroupLoadTuner.class);
		doReturn(completeStores).when(persistenceEngine).retrieveByNamedQuery(FIND_ALL_COMPLETE_STORES, StoreState.UNDER_CONSTRUCTION,
				StoreState.RESTRICTED);
		when(persistenceEngine.withLoadTuners(loadTuner)).thenReturn(persistenceEngine);

		assertThat(storeServiceImpl.findAllCompleteStores(loadTuner)).isEqualTo(completeStores);

		verify(persistenceEngine).retrieveByNamedQuery(FIND_ALL_COMPLETE_STORES, StoreState.UNDER_CONSTRUCTION, StoreState.RESTRICTED);
		verify(persistenceEngine).withLoadTuners(loadTuner);
	}

	/**
	 * Test that findAllCompleteStoreUids() calls the persistence engine with the appropriate named query.
	 */
	@Test
	public void testFindAllCompleteStoreUids() {
		final List<Long> stores = Collections.emptyList();
		doReturn(stores).when(persistenceEngine).retrieveByNamedQuery("FIND_ALL_COMPLETE_STORE_UIDS", StoreState.UNDER_CONSTRUCTION,
				StoreState.RESTRICTED);

		assertThat(storeServiceImpl.findAllCompleteStoreUids()).isEqualTo(stores);

		verify(persistenceEngine).retrieveByNamedQuery("FIND_ALL_COMPLETE_STORE_UIDS", StoreState.UNDER_CONSTRUCTION, StoreState.RESTRICTED);
	}

	/**
	 * Tests that will find all credit card type for all complete stores.
	 */
	@Test
	public void testFindAllSupportedCreditCardTypes() {

		final Set<CreditCardType> types = new HashSet<>();

		final Store store = mock(Store.class);
		final CreditCardType visa = mock(CreditCardType.class, "visa");
		final CreditCardType masterCard = mock(CreditCardType.class, "masterCard");
		types.add(visa);
		types.add(masterCard);

		final List<Store> stores = new ArrayList<>();
		stores.add(store);


		when(visa.getCreditCardType()).thenReturn("Visa");
		when(masterCard.getCreditCardType()).thenReturn("MasterCard");
		when(store.getCreditCardTypes()).thenReturn(types);

		StoreService storeService = new StoreServiceImpl() {
			@Override
			public List<Store> findAllCompleteStores() {
				return stores;
			}
		};

		assertThat(storeService.findAllSupportedCreditCardTypes()).hasSize(2);
		verify(visa).getCreditCardType();
		verify(masterCard).getCreditCardType();
		verify(store).getCreditCardTypes();

	}

	/** Convenience interface for mocking object. */
	public interface MockUpdateNotification {

		/**
		 * Method that should be mocked.
		 * @param objects objects
		 */
		void mockMethod(Object objects);
	}
}
