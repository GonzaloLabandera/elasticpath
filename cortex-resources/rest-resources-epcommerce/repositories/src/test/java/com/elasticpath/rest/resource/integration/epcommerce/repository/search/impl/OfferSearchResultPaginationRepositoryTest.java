/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.base.ScopeIdentifierPart;
import com.elasticpath.rest.definition.collections.PaginationEntity;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.definition.searches.PageIdIdentifierPart;
import com.elasticpath.rest.definition.searches.SearchIdIdentifierPart;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;

/**
 * Test class for {@link OfferSearchResultPaginationRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferSearchResultPaginationRepositoryTest {

	private static final String OFFER_ID = "offer id";
	private static final int TOTAL_RESULTS = 12842;
	private static final int TOTAL_PAGES = 2569;
	private static final String STORE_CODE = "store_code";
	private static final String USERID = "userid";
	private static final String SEARCH_OFFERS = "search_offers";
	private static final int PAGE = 1;
	private static final int RESULTS_PER_PAGE = 5;
	private static final int THREE = 3;
	private static final String SEARCHTERM = "term";
	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");

	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private SearchRepository searchRepository;

	@InjectMocks
	private OfferSearchResultPaginationRepository<OfferSearchResultIdentifier, OfferIdentifier> paginationRepository;

	@Test
	public void validateSearchData() {
		Map<String, String> searchId = new HashMap<>();
		searchId.put(PaginationEntity.PAGE_SIZE_PROPERTY, "2");
		searchId.put(SearchOfferEntity.KEYWORDS_PROPERTY, SEARCHTERM);
		OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
				.withPageId(IntegerIdentifier.of(1))
				.withSearchId(CompositeIdentifier.of(searchId))
				.withScope(SCOPE)
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.build();

		paginationRepository.validateSearchData(offerSearchResultIdentifier)
				.test()
				.assertValue(offerSearchData -> 1 == offerSearchData.getPageId())
				.assertValue(offerSearchData -> 2 == offerSearchData.getPageSize())
				.assertValue(offerSearchData -> "scope".equals(offerSearchData.getScope()))
				.assertValue(offerSearchData -> SEARCHTERM.equals(offerSearchData.getSearchKeyword()));
	}

	@Test
	public void validateSearchDataNormalizeNullPageSize() {
		Map<String, String> searchId = new HashMap<>();
		searchId.put(PaginationEntity.PAGE_SIZE_PROPERTY, "null");
		searchId.put(SearchOfferEntity.KEYWORDS_PROPERTY, SEARCHTERM);
		OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
				.withPageId(IntegerIdentifier.of(1))
				.withSearchId(CompositeIdentifier.of(searchId))
				.withScope(SCOPE)
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.build();

		paginationRepository.validateSearchData(offerSearchResultIdentifier)
				.test()
				.assertValue(offerSearchData -> 1 == offerSearchData.getPageId())
				.assertValue(offerSearchData -> 0 == offerSearchData.getPageSize())
				.assertValue(offerSearchData -> "scope".equals(offerSearchData.getScope()))
				.assertValue(offerSearchData -> SEARCHTERM.equals(offerSearchData.getSearchKeyword()));
	}

	@Test
	public void validateSearchDataBlankPageSizeNegativePageSize() {
		Map<String, String> searchId = new HashMap<>();
		searchId.put(PaginationEntity.PAGE_SIZE_PROPERTY, "-1");
		searchId.put(SearchOfferEntity.KEYWORDS_PROPERTY, SEARCHTERM);
		OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
				.withPageId(IntegerIdentifier.of(1))
				.withSearchId(CompositeIdentifier.of(searchId))
				.withScope(SCOPE)
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.build();

		paginationRepository.validateSearchData(offerSearchResultIdentifier)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.BAD_REQUEST_BODY
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	@Test
	public void validateSearchDataBlankPageSizeIsString() {
		Map<String, String> searchId = new HashMap<>();
		searchId.put(PaginationEntity.PAGE_SIZE_PROPERTY, "spaksd");
		searchId.put(SearchOfferEntity.KEYWORDS_PROPERTY, SEARCHTERM);
		OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
				.withPageId(IntegerIdentifier.of(1))
				.withSearchId(CompositeIdentifier.of(searchId))
				.withScope(SCOPE)
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.build();

		paginationRepository.validateSearchData(offerSearchResultIdentifier)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.BAD_REQUEST_BODY
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}


	/**
	 * Test a valid offer search.
	 */
	@Test
	public void testOfferSearch() {
		Collection<String> offerIds = Collections.singleton(OFFER_ID);
		PaginatedResult searchResult = new PaginatedResult(offerIds, PAGE, RESULTS_PER_PAGE, TOTAL_RESULTS);

		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldGetDefaultPageSizeWithResult(Single.just(RESULTS_PER_PAGE));
		shouldSearchOfferIdsWithResult(Single.just(searchResult));
		OfferSearchData offerSearchData = new OfferSearchData(PAGE, RESULTS_PER_PAGE, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertComplete()
				.assertValue(paginationEntity -> RESULTS_PER_PAGE == paginationEntity.getPageSize())
				.assertValue(paginationEntity -> TOTAL_PAGES == paginationEntity.getPages())
				.assertValue(paginationEntity -> PAGE == paginationEntity.getCurrent())
				.assertValue(paginationEntity -> TOTAL_RESULTS == paginationEntity.getResults())
				.assertValue(paginationEntity -> 1 == paginationEntity.getResultsOnPage());
	}

	/**
	 * Test a valid offer search with a custom page size.
	 */
	@Test
	public void testOfferSearchWithCustomPageSize() {
		Collection<String> offerIds = Collections.singleton(OFFER_ID);
		PaginatedResult searchResult = new PaginatedResult(offerIds, PAGE, RESULTS_PER_PAGE, THREE);

		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldSearchOfferIdsWithResult(Single.just(searchResult));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE, THREE, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertComplete()
				.assertValue(paginationEntity -> THREE == paginationEntity.getPageSize())
				.assertValue(paginationEntity -> 1 == paginationEntity.getPages())
				.assertValue(paginationEntity -> PAGE == paginationEntity.getCurrent())
				.assertValue(paginationEntity -> THREE == paginationEntity.getResults())
				.assertValue(paginationEntity -> 1 == paginationEntity.getResultsOnPage());
	}

	/**
	 * Test offer search when no store is found.
	 */
	@Test
	public void testOfferSearchWithStoreNotFound() {
		shouldFindSubject();
		mockSearchCriteria(Single.error(ResourceOperationFailure.notFound("not found")));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE, RESULTS_PER_PAGE, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.NOT_FOUND
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}


	/**
	 * Test offer search when trying to access a page greater than the number of resulting pages.
	 */
	@Test
	public void testOfferSearchWithPageGreaterThanResultPages() {
		Collection<String> emptyOfferIds = Collections.emptyList();
		PaginatedResult searchResult = new PaginatedResult(emptyOfferIds, PAGE, RESULTS_PER_PAGE,
				RESULTS_PER_PAGE);

		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldGetDefaultPageSizeWithResult(Single.just(RESULTS_PER_PAGE));
		shouldSearchOfferIdsWithResult(Single.just(searchResult));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE + 1, RESULTS_PER_PAGE, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.NOT_FOUND
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	/**
	 * Test offer search with invalid pagination setting result returned.
	 */
	@Test
	public void testOfferSearchWithInvalidPageSizeReturnedFromSettingsRepository() {
		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldGetDefaultPageSizeWithResult(Single.error(ResourceOperationFailure.serverError("Invalid pagination setting.")));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE, 0, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.SERVER_ERROR
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	/**
	 * Test offer search when pagination setting is invalid.
	 */
	@Test
	public void testOfferSearchWithPaginationSettingOfZero() {
		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldGetDefaultPageSizeWithResult(Single.error(ResourceOperationFailure.serverError("Zero size pagination setting")));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE, 0, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.SERVER_ERROR
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}

	/**
	 * Test offer search when page setting is invalid.
	 */
	@Test
	public void testOfferSearchWithPageSettingOfZero() {
		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldGetDefaultPageSizeWithResult(Single.error(ResourceOperationFailure.serverError("Zero size pagination setting")));
		paginationRepository.validateSearchData(OfferSearchResultIdentifier.builder()
				.withSearchId(SearchIdIdentifierPart.of("-", "-"))
				.withScope(ScopeIdentifierPart.of("-"))
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.withPageId(PageIdIdentifierPart.of(0)).build())
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertErrorMessage("Page id 0 can't be smaller than 1");
	}

	/**
	 * Test the behaviour of offer search with search result failure.
	 */
	@Test
	public void testOfferSearchWithSearchResultFailure() {
		shouldFindSubject();
		mockSearchCriteria(Single.just(new ProductSearchCriteria()));
		shouldSearchOfferIdsWithResult(Single.error(ResourceOperationFailure.serverError("Server error during search")));
		shouldGetDefaultPageSizeWithResult(Single.just(RESULTS_PER_PAGE));

		OfferSearchData offerSearchData = new OfferSearchData(PAGE, RESULTS_PER_PAGE, STORE_CODE, DEFAULT_APPLIED_FACETS, SEARCH_OFFERS);

		paginationRepository.getPaginationInfo(offerSearchData)
				.test()
				.assertFailure(ResourceOperationFailure.class)
				.assertFailure(
						throwable -> ResourceStatus.SERVER_ERROR
								.equals(((ResourceOperationFailure) throwable).getResourceStatus())
				);
	}


	private void shouldFindSubject() {
		Subject subject = TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(
				STORE_CODE, USERID, Locale.ENGLISH, Currency.getInstance("CAD"));
		when(resourceOperationContext.getSubject())
				.thenReturn(subject);
	}

	private void mockSearchCriteria(final Single<ProductCategorySearchCriteria> searchCriteria) {
		when(searchRepository.getSearchCriteria(
				nullable(String.class), any(String.class), any(Locale.class), any(Currency.class), anyMap(), any(String.class)))
				.thenReturn(searchCriteria);
	}

	private void shouldGetDefaultPageSizeWithResult(final Single<Integer> result) {
		when(searchRepository.getDefaultPageSize(STORE_CODE)).thenReturn(result);
	}

	private void shouldSearchOfferIdsWithResult(final Single<PaginatedResult> result) {
		when(searchRepository.searchForProductIds(any(ProductCategorySearchCriteria.class), anyInt(), anyInt())).thenReturn(result);
	}
}
