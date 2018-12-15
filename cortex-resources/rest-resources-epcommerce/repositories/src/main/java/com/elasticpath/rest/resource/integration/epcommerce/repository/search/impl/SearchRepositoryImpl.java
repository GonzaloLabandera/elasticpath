/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.search.Facet;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.settings.SettingsRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.rest.util.math.NumberUtil;
import com.elasticpath.service.search.FacetService;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.index.IndexSearchResult;
import com.elasticpath.service.search.index.IndexSearchService;
import com.elasticpath.service.search.solr.FacetValue;
import com.elasticpath.service.search.solr.IndexUtility;

/**
 * Repository class for general search.
 */
@Component
public class SearchRepositoryImpl implements SearchRepository {

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
	private ItemRepository itemRepository;
	private IndexUtility indexUtility;
	private ReactiveAdapter reactiveAdapter;
	private FacetService facetService;
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
		return reactiveAdapter.fromServiceAsSingle(() -> indexSearchService.search(productSearchCriteria))
				.flatMap(productSearchResult -> createPaginatedSearchResult(startPageNumber, numberOfResultsPerPage, productSearchResult));
	}

	@Override
	@CacheResult
	public Observable<String> getFacetFields(final ProductCategorySearchCriteria productSearchCriteria,
											 final int numberOfResultsPerPage) {
		return productSearchCriteria.isFacetingEnabled()
				? getFacetFieldsSearchResult(productSearchCriteria, numberOfResultsPerPage)
				: Observable.empty();
	}

	private Observable<String> getFacetFieldsSearchResult(final ProductCategorySearchCriteria productSearchCriteria,
														  final int numberOfResultsPerPage) {
		return reactiveAdapter.fromServiceAsSingle(() -> indexSearchService.search(productSearchCriteria, 0, numberOfResultsPerPage))
				.flatMapObservable(searchResult -> Observable.fromIterable(searchResult.getFacetFields(numberOfResultsPerPage)));
	}

	@Override
	@CacheResult
	public Observable<FacetValue> getFacetValues(final String facetGuid, final ProductCategorySearchCriteria productSearchCriteria,
												 final int maxResults) {
		return reactiveAdapter.fromServiceAsSingle(() -> indexSearchService.search(productSearchCriteria))
				.flatMapObservable(searchResult -> Observable.fromIterable(searchResult.getFacetValues(facetGuid, maxResults)))
				.onErrorResumeNext(throwNotFound(facetGuid));
	}

	private Observable<FacetValue> throwNotFound(final String facetGuid) {
		return Observable.error(ResourceOperationFailure.notFound(
				String.format(FACET_FIELD_WITH_TYPE_NOT_FOUND, facetGuid)));
	}

	@Override
	public Single<PaginatedResult> searchForProductIds(final ProductCategorySearchCriteria productSearchCriteria,
													   final int startPageNumber, final int numberOfResultsPerPage) {

		return reactiveAdapter.fromServiceAsSingle(() -> indexSearchService.search(productSearchCriteria))
				.flatMap(productSearchResult ->
						Single.just(productSearchResult.getResults((startPageNumber - 1) * numberOfResultsPerPage, numberOfResultsPerPage))
								.flatMap(this::getSortedProducts)
								.map(sortedProducts -> sortedProducts.stream()
										.map(product -> ImmutableSortedMap.of(PRODUCT_GUID_KEY, product.getGuid()))
										.map(CompositeIdUtil::encodeCompositeId)
										.collect(Collectors.toList()))
								.map(productIds -> new PaginatedResult(productIds, startPageNumber, numberOfResultsPerPage, productSearchResult
										.getLastNumFound())));
	}

	private Single<PaginatedResult> createPaginatedSearchResult(final int startPageNumber, final int numberOfResultsPerPage,
																final IndexSearchResult productSearchResult) {
		return Single.just(productSearchResult.getResults((startPageNumber - 1) * numberOfResultsPerPage, numberOfResultsPerPage))
				.flatMap(this::getSortedProducts)
				.map(this::getItemIds)
				.map(itemIds -> new PaginatedResult(itemIds, startPageNumber, numberOfResultsPerPage, productSearchResult
						.getLastNumFound()));
	}

	private List<String> getItemIds(final List<Product> sortedProducts) {
		// translate product IDs to item IDs in the same order
		List<String> itemIds = new ArrayList<>();
		for (Product product : sortedProducts) {
			String defaultItemId = itemRepository.getDefaultItemIdForProduct(product);
			itemIds.add(defaultItemId);
		}
		return itemIds;
	}

	private Single<List<Product>> getSortedProducts(final List<Long> productUids) {
		return reactiveAdapter.fromServiceAsSingle(() -> storeProductRepository.findByUids(productUids))
				.flatMap(products -> reactiveAdapter.fromServiceAsSingle(() -> indexUtility.sortDomainList(productUids,
						products)));
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
}
