/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.impl.StoreProductImpl;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.attribute.LocaleSubjectAttribute;
import com.elasticpath.rest.identity.attribute.SubjectAttribute;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.settings.SettingsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ExceptionTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.SortAttributeService;
import com.elasticpath.service.search.index.IndexSearchResult;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.search.query.KeywordSearchCriteria;
import com.elasticpath.service.search.solr.IndexUtility;

/**
 * Test class for search lookup strategy.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchRepositoryImplTest {
	private static final String STORE_CODE = "store";
	private static final String LOCALE_CODE = Locale.ENGLISH.getLanguage();
	private static final String PAGINATION_SETTING = "COMMERCE/STORE/listPagination";
	private static final Integer DEFAULT_PAGE_SIZE = 10;
	private static final int INVALID_PAGE_SIZE = -10;
	private static final String EXPECTED_DISPLAY_NAME = "expectedDisplayName";
	@Mock
	private Store store;

	@Mock
	private SortAttributeService sortAttributeService;
	@Mock
	private IndexSearchService indexSearchService;
	@Mock
	private SettingsRepository settingsRepository;
	@Mock
	private StoreProductRepository storeProductRepository;
	@Mock
	private ItemRepository itemRepository;
	@Mock
	private IndexUtility indexUtility;
	@Mock
	private IndexSearchResult indexSearchResult;
	@SuppressWarnings({"PMD.UnusedPrivateField"})
	@Mock
	private ExceptionTransformer exceptionTransformer;
	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private FacetService facetService;
	@Mock
	private StoreRepository storeRepository;
	@Mock
	private CategoryRepository categoryRepository;


	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	private SearchRepositoryImpl repository;

	@Before
	public void setUp() {
		repository = new SearchRepositoryImpl();
		repository.setIndexSearchService(indexSearchService);
		repository.setIndexUtility(indexUtility);
		repository.setItemRepository(itemRepository);
		repository.setReactiveAdapter(reactiveAdapter);
		repository.setSettingsRepository(settingsRepository);
		repository.setStoreProductRepository(storeProductRepository);
		repository.setResourceOperationContext(resourceOperationContext);
		repository.setFacetService(facetService);
		repository.setCategoryRepository(categoryRepository);
		repository.setStoreRepository(storeRepository);
		repository.setSortAttributeService(sortAttributeService);
	}

	@Test
	public void ensureSearchForMultipleItemIdsLessThanPageSizeReturnsThemAll() {
		final int page = 1;
		final int pageSize = 10;

		final ProductCategorySearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setStoreCode(STORE_CODE);
		final Collection<String> expectedItemIds = Arrays.asList("id1", "id2", "id3");

		final List<Long> resultUids = mockIndexSearch(page, pageSize, searchCriteria, expectedItemIds.size());
		mockProductAndItemCalls(resultUids, expectedItemIds);

		Single<PaginatedResult> itemIdsSearchExecutionResult = repository.searchForItemIds(page, pageSize, searchCriteria);

		itemIdsSearchExecutionResult.test()
				.assertComplete()
				.assertValue(paginatedResult -> page == paginatedResult.getCurrentPage())
				.assertValue(paginatedResult -> 1 == paginatedResult.getNumberOfPages())
				.assertValue(paginatedResult -> pageSize == paginatedResult.getResultsPerPage())
				.assertValue(paginatedResult -> expectedItemIds.size() == paginatedResult.getTotalNumberOfResults());
	}

	@Test
	public void ensureSearchForMultipleOfferIdsLessThanPageSizeReturnsThemAll() {
		final int page = 1;
		final int pageSize = 10;

		final ProductCategorySearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setStoreCode(STORE_CODE);
		final Collection<String> expectedItemIds = Arrays.asList("id1", "id2", "id3");

		final List<Long> resultUids = mockIndexSearch(page, pageSize, searchCriteria, expectedItemIds.size());
		mockProductAndItemCalls(resultUids, expectedItemIds);

		Single<PaginatedResult> itemIdsSearchExecutionResult = repository.searchForProductIds(searchCriteria, page, pageSize);

		itemIdsSearchExecutionResult.test()
				.assertComplete()
				.assertValue(paginatedResult -> page == paginatedResult.getCurrentPage())
				.assertValue(paginatedResult -> 1 == paginatedResult.getNumberOfPages())
				.assertValue(paginatedResult -> pageSize == paginatedResult.getResultsPerPage())
				.assertValue(paginatedResult -> expectedItemIds.size() == paginatedResult.getTotalNumberOfResults());
	}

	private List<Long> mockIndexSearch(final int page, final int pageSize, final ProductCategorySearchCriteria searchCriteria,
									   final int numberOfSearchResultsToReturn) {
		final List<Long> resultUids = Collections.emptyList();

		when(indexSearchService.search(searchCriteria, page - 1, pageSize)).thenReturn(indexSearchResult);
		when(indexSearchResult.getCachedResultUids()).thenReturn(resultUids);
		when(indexSearchResult.getLastNumFound()).thenReturn(numberOfSearchResultsToReturn);

		return resultUids;
	}

	private void mockProductAndItemCalls(final List<Long> productUids, final Collection<String> expectedItemIds) {
		final Product product1 = new ProductImpl();
		product1.setCode("one");
		final Product product2 = new ProductImpl();
		product2.setCode("two");
		final Product product3 = new ProductImpl();
		product3.setCode("three");
		final List<StoreProduct> products = Arrays.asList(
				new StoreProductImpl(product1),
				new StoreProductImpl(product2),
				new StoreProductImpl(product3));

		when(storeProductRepository.findByUids(STORE_CODE, productUids)).thenReturn(products);
		when(indexUtility.sortDomainList(productUids, products)).thenReturn(products);

		Iterator<StoreProduct> productsIterator = products.iterator();
		Iterator<String> expectedItemIdsIterator = expectedItemIds.iterator();
		while (productsIterator.hasNext() && expectedItemIdsIterator.hasNext()) {
			Product product = productsIterator.next();
			String itemId = expectedItemIdsIterator.next();

			when(itemRepository.getDefaultItemIdForProduct(product)).thenReturn(itemId);
		}
	}

	@Test
	public void ensureSearchForItemIdsWhereGetDefaultItemIdFailsReturnsServerError() {
		final int page = 1;
		final int pageSize = 10;
		final KeywordSearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setStoreCode(STORE_CODE);
		final Collection<String> expectedItemIds = Arrays.asList("id1", "id2", "id3");
		final List<Long> resultUids = mockIndexSearch(page, pageSize, searchCriteria, 2);

		mockProductAndItemCallWithAssertionError(resultUids, expectedItemIds);

		repository.searchForItemIds(page, pageSize, searchCriteria)
				.test()
				.assertFailure(AssertionError.class);
	}

	private void mockProductAndItemCallWithAssertionError(final List<Long> productUids, final Collection<String> expectedItemIds) {
		final Product product1 = new ProductImpl();
		final List<StoreProduct> products = Arrays.asList(new StoreProductImpl(product1));

		when(storeProductRepository.findByUids(STORE_CODE, productUids)).thenReturn(products);
		when(indexUtility.sortDomainList(productUids, products)).thenReturn(products);

		Iterator<StoreProduct> productsIterator = products.iterator();
		Iterator<String> expectedItemIdsIterator = expectedItemIds.iterator();
		while (productsIterator.hasNext() && expectedItemIdsIterator.hasNext()) {
			Product product = productsIterator.next();

			when(itemRepository.getDefaultItemIdForProduct(product)).thenThrow(new AssertionError());
		}
	}

	@Test
	public void ensureSearchItemIdsWithException() {
		final int page = 1;
		final int pageSize = 10;
		final ProductCategorySearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setStoreCode(STORE_CODE);

		when(indexSearchService.search(searchCriteria, page - 1, pageSize))
				.thenThrow(new EpPersistenceException("persistence exception during search"));

		repository.searchForItemIds(page, pageSize, searchCriteria)
				.test()
				.assertFailure(EpPersistenceException.class);
	}

	@Test
	public void ensureSearchOfferIdsWithException() {
		final int page = 1;
		final int pageSize = 10;
		final ProductCategorySearchCriteria searchCriteria = new KeywordSearchCriteria();
		searchCriteria.setStoreCode(STORE_CODE);

		when(indexSearchService.search(searchCriteria, page - 1, pageSize))
				.thenThrow(new EpPersistenceException("persistence exception during search"));

		repository.searchForProductIds(searchCriteria, page, pageSize)
				.test()
				.assertFailure(EpPersistenceException.class);
	}

	@Test
	public void ensureGetDefaultPageSize() {
		when(settingsRepository.getSetting(PAGINATION_SETTING, STORE_CODE))
				.thenReturn(Maybe.just(DEFAULT_PAGE_SIZE));

		Single<Integer> result = repository.getDefaultPageSize(STORE_CODE);
		result.test().assertComplete().assertNoErrors().assertValue(DEFAULT_PAGE_SIZE);
	}

	@Test
	public void ensureGetDefaultPageSizeWithInvalidStoreCode() {
		when(settingsRepository.getSetting(PAGINATION_SETTING, STORE_CODE))
				.thenReturn(Maybe.empty());

		Single<Integer> result = repository.getDefaultPageSize(STORE_CODE);
		result.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.NOT_FOUND
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	@Test
	public void ensureGetDefaultPageSizeWithMissingSetting() {
		when(settingsRepository.getSetting(PAGINATION_SETTING, STORE_CODE))
				.thenReturn(Maybe.error(ResourceOperationFailure.serverError("Error reading setting")));

		Single<Integer> result = repository.getDefaultPageSize(STORE_CODE);
		result.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.SERVER_ERROR
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	@Test
	public void ensureGetDefaultPageSizeWithNegativePageSize() {
		when(settingsRepository.getSetting(PAGINATION_SETTING, STORE_CODE))
				.thenReturn(Maybe.just(-1));

		Single<Integer> result = repository.getDefaultPageSize(STORE_CODE);
		result.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.SERVER_ERROR
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	@Test
	public void shouldNotValidateEntityForEmptyKeyword() {
		repository.validate(SearchKeywordsEntity.builder().withKeywords("").build())
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertError(throwable -> ResourceStatus.BAD_REQUEST_BODY.equals(
						((ResourceOperationFailure) throwable).getResourceStatus()));

	}

	@Test
	public void shouldNotValidateEntityForTooLongKeyword() {
		final int maxKeywordsToGenerate = 600;
		StringBuilder kewyordBuilder = new StringBuilder();
		IntStream.range(0, maxKeywordsToGenerate).forEach(kewyordBuilder::append);
		repository.validate(SearchKeywordsEntity.builder().withKeywords(kewyordBuilder.toString()).build())
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertError(throwable -> ResourceStatus.BAD_REQUEST_BODY.equals(
						((ResourceOperationFailure) throwable).getResourceStatus()));

	}

	@Test
	public void shouldNotValidateEntityForInvalidPageSize() {
		repository.validate(SearchKeywordsEntity.builder().withKeywords("a").withPageSize(INVALID_PAGE_SIZE).build())
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertError(throwable -> ResourceStatus.BAD_REQUEST_BODY.equals(
						((ResourceOperationFailure) throwable).getResourceStatus()));
	}

	@Test
	public void shouldValidateEntityForUndefinedPageSize() {
		repository.validate(SearchKeywordsEntity.builder().withKeywords("a").withPageSize(null).build())
				.test()
				.assertComplete();
	}
	@Test
	public void testGetDisplayNameByGuid() {
		Subject mockSubject = mock(Subject.class);
		when(resourceOperationContext.getSubject()).thenReturn(mockSubject);
		LocaleSubjectAttribute localeSubjectAttribute = new LocaleSubjectAttribute("key", Locale.ENGLISH);
		Collection<SubjectAttribute> attributes = Collections.singleton(localeSubjectAttribute);
		when(mockSubject.getAttributes()).thenReturn(attributes);


		String guid = "guid";
		Facet facet = mock(Facet.class);
		when(facetService.findByGuid(guid)).thenReturn(facet);
		Map<String, String> displayNameMap = new HashMap<>();
		displayNameMap.put(Locale.ENGLISH.toString(), EXPECTED_DISPLAY_NAME);
		when(facet.getDisplayNameMap()).thenReturn(displayNameMap);

		repository.getDisplayNameByGuid(guid)
				.test()
				.assertComplete()
				.assertValue(EXPECTED_DISPLAY_NAME);
	}

	@Test
	public void testGetSearchCriteria() {
		Map<String, String> appliedFacets = new HashMap<>();
		String keyword = "KEYWORD";
		long categoryUID = 1L;
		Currency currency = Currency.getInstance("CAD");

		Store store = mock(Store.class);
		Category category = mock(Category.class);
		Catalog catalog = mock(Catalog.class);


		when(storeRepository.findStoreAsSingle(STORE_CODE))
				.thenReturn(Single.just(store));
		when(storeRepository.findStoreAsSingle(STORE_CODE))
				.thenReturn(Single.just(store));

		when(store.getCode()).thenReturn(STORE_CODE);
		String categoryCode = "categoryCode";
		when(categoryRepository.findByStoreAndCategoryCode(STORE_CODE,
				categoryCode)).thenReturn(Single.just(category));
		when(category.getUidPk()).thenReturn(categoryUID);
		when(category.getCatalog()).thenReturn(catalog);
		when(catalog.getCode()).thenReturn("CatalogCode");

		OfferSearchData offerSearchData = new OfferSearchData(1, DEFAULT_PAGE_SIZE, STORE_CODE, appliedFacets, keyword);
		offerSearchData.setCategoryCode(categoryCode);

		Single<ProductCategorySearchCriteria> searchCriteria = repository.getSearchCriteria(offerSearchData, Locale.ENGLISH, currency);

		searchCriteria
				.test()
				.assertNoErrors()
				.assertValue(criteria -> criteria.getCategoryUid().equals(categoryUID))
				.assertValue(criteria -> criteria.getCurrency().equals(currency))
				.assertValue(criteria -> criteria.getLocale().equals(Locale.ENGLISH))
				.assertValue(criteria -> criteria.getCatalogCode().equals("CatalogCode"));
	}

	@Test
	public void shouldReturnAttributeGuids() {
		String guid = "guid";
		String guid2 = "guid2";

		when(storeRepository.findStoreAsSingle(STORE_CODE)).thenReturn(Single.just(store));
		when(store.getCode()).thenReturn(STORE_CODE);
		when(sortAttributeService.findSortAttributeGuidsByStoreCodeAndLocalCode(STORE_CODE, LOCALE_CODE))
				.thenReturn(ImmutableList.of(guid, guid2));

		repository.getSortAttributeGuidsForStoreAndLocale(STORE_CODE, LOCALE_CODE)
				.test()
				.assertValueCount(2);
	}

	@Test
	public void shouldReturnSortValue() {
		SortValue sortValue = mock(SortValue.class);
		when(sortAttributeService.findSortValueByGuidAndLocaleCode(STORE_CODE, LOCALE_CODE))
				.thenReturn(sortValue);

		repository.getSortValueByGuidAndLocaleCode(STORE_CODE, LOCALE_CODE)
				.test()
				.assertValueCount(1);
	}

	@Test
	public void shouldReturnSortAttribute() {
		SortAttribute sortAttribute = mock(SortAttribute.class);

		when(storeRepository.findStoreAsSingle(STORE_CODE)).thenReturn(Single.just(store));
		when(store.getCode()).thenReturn(STORE_CODE);
		when(sortAttributeService.getDefaultSortAttributeForStore(STORE_CODE)).thenReturn(sortAttribute);

		repository.getDefaultSortAttributeForStore(STORE_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(1);
	}

}
