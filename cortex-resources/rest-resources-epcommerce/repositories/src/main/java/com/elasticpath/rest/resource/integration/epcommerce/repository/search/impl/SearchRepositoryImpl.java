/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.createNavigationSearchCriteria;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.createSearchCriteria;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.settings.SettingsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.util.math.NumberUtil;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.SortAttributeService;
import com.elasticpath.service.search.index.IndexSearchResult;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.search.query.SortBy;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.solr.FacetValue;
import com.elasticpath.service.search.solr.IndexUtility;

/**
 * Repository class for general search.
 */
@Component
public class SearchRepositoryImpl implements SearchRepository {

	/**
	 * Page ID of the first page.
	 */
	public static final int FIRST_PAGE = 1;
	/**
	 * Composite ID product Guid Key.
	 */
	public static final String PRODUCT_GUID_KEY = "P";
	private static final int KEYWORDS_MAX_LENGTH = 500;
	private static final Range<Integer> RANGE = Range.between(1, Integer.MAX_VALUE);
	private static final String DEFAULT_PAGINATION_VALUE_NO_SETTING_FOUND_ERROR =
			"Error encountered while retrieving default pagination setting with context '%s'";
	private static final String DEFAULT_PAGINATION_VALUE_NO_VALUE_ERROR = "No default pagination value for '%s' is defined";
	private static final String DEFAULT_PAGINATION_VALUE_INVALID_VALUE_ERROR = "Default pagination value for '%s' is invalid: '%s'";
	private static final String PAGINATION_SETTING = "COMMERCE/STORE/listPagination";
	private static final String FACET_FIELD_WITH_TYPE_NOT_FOUND = "Facet with guid %s not found.";

	private SettingsRepository settingsRepository;
	private IndexSearchService indexSearchService;
	private StoreProductRepository storeProductRepository;
	private StoreRepository storeRepository;
	private CategoryRepository categoryRepository;
	private ItemRepository itemRepository;
	private IndexUtility indexUtility;
	private ReactiveAdapter reactiveAdapter;
	private FacetService facetService;
	private SortAttributeService sortAttributeService;
	private ResourceOperationContext resourceOperationContext;

	@Reference
	public void setSettingsRepository(final SettingsRepository settingsRepository) {
		this.settingsRepository = settingsRepository;
	}

	@Reference
	public void setIndexSearchService(final IndexSearchService indexSearchService) {
		this.indexSearchService = indexSearchService;
	}

	@Reference
	public void setStoreProductRepository(final StoreProductRepository storeProductRepository) {
		this.storeProductRepository = storeProductRepository;
	}

	@Reference
	public void setItemRepository(final ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Reference
	public void setIndexUtility(final IndexUtility indexUtility) {
		this.indexUtility = indexUtility;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setFacetService(final FacetService facetService) {
		this.facetService = facetService;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setStoreRepository(final StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}

	@Reference
	public void setCategoryRepository(final CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Reference
	public void setSortAttributeService(final SortAttributeService sortAttributeService) {
		this.sortAttributeService = sortAttributeService;
	}

	@Override
	@CacheResult
	public Single<Integer> getDefaultPageSize(final String storeCode) {
		return settingsRepository.<Integer>getSetting(PAGINATION_SETTING, storeCode)
				.toSingle()
				.onErrorResumeNext(throwable -> {
					if (throwable instanceof NoSuchElementException) {
						return Single.error(ResourceOperationFailure.notFound(String.format(DEFAULT_PAGINATION_VALUE_NO_VALUE_ERROR, storeCode)));
					} else {
						return Single.error(ResourceOperationFailure.serverError(
								String.format(DEFAULT_PAGINATION_VALUE_NO_SETTING_FOUND_ERROR, storeCode)));
					}
				})
				.flatMap(paginationSetting -> getPaginationValue(storeCode, paginationSetting));
	}

	private Single<Integer> getPaginationValue(final String storeCode, final Integer paginationValue) {
		if (paginationValue == null || !NumberUtil.isPositive(paginationValue)) {
			return Single.error(ResourceOperationFailure.serverError(
					String.format(DEFAULT_PAGINATION_VALUE_INVALID_VALUE_ERROR, storeCode, paginationValue)));
		}
		return Single.just(paginationValue);
	}

	@Override
	public Single<PaginatedResult> searchForItemIds(final int startPageNumber, final int numberOfResultsPerPage,
													final ProductCategorySearchCriteria productSearchCriteria) {
		return getSearchResultAsSingle(productSearchCriteria, (startPageNumber - 1) * numberOfResultsPerPage, numberOfResultsPerPage)
				.flatMap(productSearchResult -> createPaginatedSearchResult(productSearchCriteria.getStoreCode(),
						startPageNumber, numberOfResultsPerPage, productSearchResult));
	}

	@Override
	public Observable<String> getFacetFields(final ProductCategorySearchCriteria productSearchCriteria,
											 final int startPageNumber, final int numberOfResultsPerPage) {
		return productSearchCriteria.isFacetingEnabled()
				? getFacetFieldsSearchResult(productSearchCriteria, (startPageNumber - 1) * numberOfResultsPerPage, numberOfResultsPerPage)
				: Observable.empty();
	}

	private Observable<String> getFacetFieldsSearchResult(final ProductCategorySearchCriteria productSearchCriteria,
														  final int startIndex, final int maxResults) {
		return getSearchResultAsSingle(productSearchCriteria, startIndex, maxResults)
				.flatMapObservable(searchResult -> Observable.fromIterable(searchResult.getFacetFields()));
	}

	@Override
	public Observable<FacetValue> getFacetValues(final String facetGuid, final ProductCategorySearchCriteria productSearchCriteria) {
		return getSearchResultAsSingle(productSearchCriteria, 0, 0)
				.flatMapObservable(searchResult -> Observable.fromIterable(searchResult.getFacetValues(facetGuid)))
				.onErrorResumeNext(throwNotFound(facetGuid));
	}

	private Observable<FacetValue> throwNotFound(final String facetGuid) {
		return Observable.error(ResourceOperationFailure.notFound(
				String.format(FACET_FIELD_WITH_TYPE_NOT_FOUND, facetGuid)));
	}

	private Single<IndexSearchResult> getSearchResultAsSingle(final ProductCategorySearchCriteria productSearchCriteria,
															  final int startPageNumber, final int numberOfResultsPerPage) {
		return reactiveAdapter.fromServiceAsSingle(() -> getSearchResult(productSearchCriteria, startPageNumber, numberOfResultsPerPage));
	}

	@CacheResult
	private IndexSearchResult getSearchResult(final ProductCategorySearchCriteria productSearchCriteria,
											  final int startPageNumber, final int numberOfResultsPerPage) {
		return indexSearchService.search(productSearchCriteria, startPageNumber, numberOfResultsPerPage);
	}

	@Override
	public Single<PaginatedResult> searchForProductIds(final ProductCategorySearchCriteria productSearchCriteria,
													   final int startPageNumber, final int numberOfResultsPerPage) {

		return getSearchResultAsSingle(productSearchCriteria, (startPageNumber - 1) * numberOfResultsPerPage, numberOfResultsPerPage)
				.flatMap(searchResult -> getSortedProducts(productSearchCriteria.getStoreCode(), searchResult.getCachedResultUids())
						.map(sortedProducts -> sortedProducts.stream()
								.map(product -> ImmutableSortedMap.of(PRODUCT_GUID_KEY, product.getGuid()))
								.map(CompositeIdUtil::encodeCompositeId)
								.collect(Collectors.toList()))
						.map(productIds -> new PaginatedResult(productIds, startPageNumber, numberOfResultsPerPage, searchResult
								.getLastNumFound())));
	}

	private Single<PaginatedResult> createPaginatedSearchResult(final String storeCode,
																final int startPageNumber, final int numberOfResultsPerPage,
																final IndexSearchResult productSearchResult) {
		return Single.just(productSearchResult.getCachedResultUids())
				.flatMap(productUids -> getSortedProducts(storeCode, productUids))
				.map(this::getItemIds)
				.map(itemIds -> new PaginatedResult(itemIds, startPageNumber, numberOfResultsPerPage, productSearchResult
						.getLastNumFound()));
	}

	private List<String> getItemIds(final List<StoreProduct> sortedProducts) {
		// translate product IDs to item IDs in the same order
		List<String> itemIds = new ArrayList<>();
		for (Product product : sortedProducts) {
			String defaultItemId = itemRepository.getDefaultItemIdForProduct(product);
			itemIds.add(defaultItemId);
		}
		return itemIds;
	}

	private Single<List<StoreProduct>> getSortedProducts(final String storeCode, final List<Long> productUids) {
		return reactiveAdapter.fromServiceAsSingle(() -> storeProductRepository.findByUids(storeCode, productUids))
				.flatMap(products -> reactiveAdapter.fromServiceAsSingle(() -> indexUtility.sortDomainList(productUids, products)));
	}

	@Override
	public Completable validate(final SearchKeywordsEntity searchKeywordsEntity) {
		String keywords = searchKeywordsEntity.getKeywords();
		if (StringUtils.isEmpty(keywords)) {
			return Completable.error(ResourceOperationFailure
					.badRequestBody("Keywords field is missing a value."));
		}
		if (StringUtils.length(keywords) > KEYWORDS_MAX_LENGTH) {
			return Completable.error(ResourceOperationFailure
					.badRequestBody(String.format("Keywords field is too long, the maximum length is %s.",
							KEYWORDS_MAX_LENGTH)));
		}

		Integer pageSize = searchKeywordsEntity.getPageSize();
		if (pageSize != null && !RANGE.contains(pageSize)) {
			return Completable.error(ResourceOperationFailure
					.badRequestBody(String.format("Page Size is outside this range: %s", RANGE)));
		}

		return Completable.complete();
	}

	@Override
	public Single<String> getDisplayNameByGuid(final String facetGuid) {
		String locale = ObjectUtils.defaultIfNull(SubjectUtil.getLocale(resourceOperationContext.getSubject()), Locale.getDefault()).toString();
		return Single.just(facetService.findByGuid(facetGuid))
				.map(Facet::getDisplayNameMap)
				.map(map -> map.get(locale));
	}

	@Override
	public Single<ProductCategorySearchCriteria> getSearchCriteria(final OfferSearchData offerSearchData, final Locale locale,
																   final Currency currency) {
		String storeCode = offerSearchData.getScope();
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMap(store -> buildSearchCriteria(offerSearchData, locale, currency, store));
	}

	@Override
	@CacheResult
	public Observable<String> getSortAttributeGuidsForStoreAndLocale(final String storeCode, final String localeCode) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMapObservable(store ->
						Observable.fromIterable(sortAttributeService.findSortAttributeGuidsByStoreCodeAndLocalCode(store.getCode(), localeCode)));
	}

	@Override
	public Single<SortValue> getSortValueByGuidAndLocaleCode(final String guid, final String localCode) {
		return reactiveAdapter.fromServiceAsSingle(() -> sortAttributeService.findSortValueByGuidAndLocaleCode(guid, localCode));
	}

	private Single<ProductCategorySearchCriteria> buildNavigationSearchCriteria(final String categoryCode, final Locale locale,
																				final Currency currency, final Map<String, String> appliedFacets,
																				final String storeCode, final SortBy sortBy,
																				final SortOrder sortOrder) {
		return categoryRepository.findByStoreAndCategoryCode(storeCode, categoryCode)
				.map(category -> createNavigationSearchCriteria(appliedFacets, locale, currency, category, storeCode, sortBy, sortOrder));
	}

	private Single<ProductCategorySearchCriteria> buildSearchCriteria(final OfferSearchData offerSearchData, final Locale locale,
																	  final Currency currency, final Store store) {
		String categoryCode = offerSearchData.getCategoryCode();
		String keyword = offerSearchData.getSearchKeyword();
		SortBy sortBy = offerSearchData.getSortBy();
		SortOrder sortOrder = offerSearchData.getSortOrder();
		Map<String, String> appliedFacets = offerSearchData.getAppliedFacets();

		return categoryCode == null ? Single.just(createSearchCriteria(keyword, store, appliedFacets, locale, currency, sortBy, sortOrder))
				: buildNavigationSearchCriteria(categoryCode, locale, currency, appliedFacets, store.getCode(), sortBy, sortOrder);
	}

	@Override
	public Maybe<SortAttribute> getDefaultSortAttributeForStore(final String storeCode) {
		return storeRepository.findStoreAsSingle(storeCode)
				.flatMapMaybe(store -> reactiveAdapter.fromServiceAsMaybe(()
						-> sortAttributeService.getDefaultSortAttributeForStore(store.getCode()), Maybe.empty()));
	}
}
