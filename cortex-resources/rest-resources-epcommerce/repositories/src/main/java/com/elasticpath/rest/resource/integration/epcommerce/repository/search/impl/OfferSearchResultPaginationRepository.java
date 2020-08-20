/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.CATEGORY_CODE_PROPERTY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.commons.util.impl.LocaleUtils;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.repository.PaginationRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.pagination.PagingLink;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.query.SortBy;
import com.elasticpath.service.search.query.SortOrder;
import com.elasticpath.service.search.query.StandardSortBy;
import com.elasticpath.service.search.solr.FacetConstants;

/**
 * Repository that provides lookup of item data through indexed offer search.
 *
 * @param <I>  the self identifier type
 * @param <LI> the linked identifier type
 */
@Component(service = PaginationRepository.class)
public class OfferSearchResultPaginationRepository<I extends OfferSearchResultIdentifier, LI extends OfferIdentifier>
		extends PaginationRepository<OfferSearchResultIdentifier, OfferIdentifier> {

	private static final Logger LOG = LoggerFactory.getLogger(OfferSearchResultPaginationRepository.class);

	private static final int FIRST_PAGE_ID = 1;

	private ResourceOperationContext resourceOperationContext;
	private SearchRepository searchRepository;

	@Override
	public Single<PaginationEntity> getPaginationInfo(final OfferSearchResultIdentifier offerSearchResultIdentifier) {
		return validateSearchData(offerSearchResultIdentifier).flatMap(this::getPaginationInfo);
	}

	@Override
	public Observable<OfferIdentifier> getElements(final OfferSearchResultIdentifier offerSearchResultIdentifier) {
		return validateSearchData(offerSearchResultIdentifier).flatMapObservable(this::getItems);
	}

	@Override
	public Observable<PagingLink<OfferSearchResultIdentifier>> getPagingLinks(
			final OfferSearchResultIdentifier offerSearchResultIdentifier) {
		return validateSearchData(offerSearchResultIdentifier).flatMapObservable(offerSearchData ->
				getLinks(offerSearchData, offerSearchResultIdentifier));
	}

	@Override
	public OfferSearchResultIdentifier buildPageIdentifier(
			final OfferSearchResultIdentifier offerSearchResultIdentifier, final IdentifierPart<Integer> pageId) {

		return OfferSearchResultIdentifier
				.builderFrom(offerSearchResultIdentifier)
				.withPageId(pageId)
				.build();
	}

	/**
	 * Validate search data.
	 *
	 * @param offerSearchResultIdentifier OfferSearchResultIdentifier
	 * @return OfferSearchData or validation error
	 */
	public Single<OfferSearchData> validateSearchData(final OfferSearchResultIdentifier offerSearchResultIdentifier) {
		int pageId = offerSearchResultIdentifier.getPageId().getValue();

		if (pageId < FIRST_PAGE_ID) {
			String unknownPageIdErrorMsg = "Page id " + pageId + " can't be smaller than " + FIRST_PAGE_ID;
			LOG.error(unknownPageIdErrorMsg);
			return Single.error(ResourceOperationFailure.badRequestBody(unknownPageIdErrorMsg));
		}

		final Map<String, String> searchId = offerSearchResultIdentifier.getSearchId().getValue();
		String pageSizeString = searchId.get(PaginationEntity.PAGE_SIZE_PROPERTY);

		if ("null".equals(pageSizeString) || StringUtils.isBlank(pageSizeString)) {
			pageSizeString = "0";
		}
		if (!NumberUtils.isDigits(pageSizeString)) {
			return Single.error(ResourceOperationFailure.badRequestBody("Invalid page size"));
		}

		String searchOffers = searchId.get(SearchOfferEntity.KEYWORDS_PROPERTY);
		String categoryCode = searchId.get(CATEGORY_CODE_PROPERTY);

		if (searchOffers == null && categoryCode == null) {
			return Single.error(ResourceOperationFailure.badRequestBody("Search keyword or category code must be defined."));
		}

		int pageSize = Integer.parseInt(pageSizeString);

		String scope = offerSearchResultIdentifier.getScope().getValue();
		Map<String, String> appliedFacets = offerSearchResultIdentifier.getAppliedFacets().getValue();

		OfferSearchData offerSearchData = new OfferSearchData(pageId, pageSize, scope, appliedFacets, searchOffers);
		offerSearchData.setCategoryCode(categoryCode);

		if (searchId.containsKey(SORT)) {
			populateSorting(searchId, offerSearchData);
		}

		return Single.just(offerSearchData);
	}

	private void populateSorting(final Map<String, String> searchId, final OfferSearchData offerSearchData) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		String localeCode = LocaleUtils.getCommerceLocalCode(locale);
		searchRepository.getSortValueByGuidAndLocaleCode(searchId.get(SORT), localeCode)
				.subscribe(sortValue -> setOfferSearchData(offerSearchData, sortValue));
	}

	private void setOfferSearchData(final OfferSearchData offerSearchData, final SortValue sortValue) {
		offerSearchData.setSortBy(getSortType(sortValue.getBusinessObjectId(), sortValue.getAttributeType()));
		offerSearchData.setSortOrder(sortValue.isDescending() ? SortOrder.DESCENDING : SortOrder.ASCENDING);
	}

	/**
	 *  Returns the sort type (protected for test and extensibility purposes).
	 * @param sortString the sort string.
	 * @param sortAttributeType The attribute  type.
	 * @return the sortby object.
	 */
	protected SortBy getSortType(final String sortString, final SortAttributeGroup sortAttributeType) {
		if (sortAttributeType == SortAttributeGroup.FIELD_TYPE) {
			switch (sortString) {
				case FacetConstants.PRODUCT_NAME:
					return StandardSortBy.PRODUCT_NAME;
				case FacetConstants.PRICE:
					return StandardSortBy.PRICE;
				case FacetConstants.FEATURED:
					return StandardSortBy.FEATURED_CATEGORY;
				case FacetConstants.SALES_COUNT:
					return StandardSortBy.TOP_SELLER;
				default:
					return StandardSortBy.RELEVANCE;
			}
		}

		SortBy attribute = StandardSortBy.ATTRIBUTE;
		attribute.setSortString(sortString);
		return attribute;
	}

	/**
	 * Get pagination information for search data.
	 *
	 * @param offerSearchData search data
	 * @return PaginationEntity containing pagination information
	 */
	public Single<PaginationEntity> getPaginationInfo(final OfferSearchData offerSearchData) {
		return getPaginatedResult(offerSearchData)
				.map(paginatedResult ->
						PaginationEntity.builder()
								.withCurrent(offerSearchData.getPageId())
								.withPageSize(offerSearchData.getPageSize())
								.withPages(paginatedResult.getNumberOfPages())
								.withResultsOnPage(paginatedResult.getResultIds().size())
								.withResults(paginatedResult.getTotalNumberOfResults())
								.build());
	}

	/**
	 * Get linked items for search data.
	 *
	 * @param offerSearchData search data.
	 * @return linked items
	 */
	protected Observable<OfferIdentifier> getItems(final OfferSearchData offerSearchData) {
		return getPaginatedResult(offerSearchData)
				.flatMapObservable(paginatedResult -> Observable.fromIterable(paginatedResult.getResultIds()))
				.map(offerId ->
						OfferIdentifier.builder()
								.withOfferId(CompositeIdentifier.of(CompositeIdUtil.decodeCompositeId(offerId)))
								.withScope(StringIdentifier.of(offerSearchData.getScope()))
								.build()
				);
	}

	/**
	 * Get paging links for search data.
	 *
	 * @param offerSearchData             search data
	 * @param offerSearchResultIdentifier current page identifier
	 * @return paging links
	 */
	protected Observable<PagingLink<OfferSearchResultIdentifier>> getLinks(
			final OfferSearchData offerSearchData,
			final OfferSearchResultIdentifier offerSearchResultIdentifier) {
		return getPaginatedResult(offerSearchData)
				.flatMapObservable(paginatedResult ->
						createPagingLinks(offerSearchData.getPageId(), paginatedResult.getNumberOfPages(), offerSearchResultIdentifier)
				);
	}

	/**
	 * Get paginated result for search data.
	 *
	 * @param offerSearchData search data
	 * @return paginated result
	 */
	protected Single<PaginatedResult> getPaginatedResult(final OfferSearchData offerSearchData) {
		return getSearchCriteria(offerSearchData)
				.flatMap(offerSearchCriteria ->
						getPageSizeUsed(offerSearchData.getScope(), offerSearchData.getPageSize())
								.flatMap(pageSizeUsed -> search(offerSearchData.getPageId(), offerSearchCriteria, pageSizeUsed))
				).flatMap(paginatedResult -> validateSearchResult(offerSearchData, paginatedResult));
	}

	private Single<ProductCategorySearchCriteria> getSearchCriteria(final OfferSearchData offerSearchData) {
		Subject subject = resourceOperationContext.getSubject();
		Locale locale = SubjectUtil.getLocale(subject);
		Currency currency = SubjectUtil.getCurrency(subject);

		return searchRepository.getSearchCriteria(offerSearchData, locale, currency);
	}

	private Single<PaginatedResult> validateSearchResult(final OfferSearchData offerSearchData,
														 final PaginatedResult paginatedResult) {
		int pageCount = paginatedResult.getNumberOfPages();
		if (offerSearchData.getPageId() > pageCount) {
			LOG.debug("Tried to access page {} which exceeds number of pages: {}", offerSearchData.getPageId(), pageCount);
			return Single.error(ResourceOperationFailure.notFound(String.format("Page %s does not exist.",
					offerSearchData.getPageId())));
		}
		return Single.just(paginatedResult);
	}

	private Single<PaginatedResult> search(final int currentPageNumber, final ProductCategorySearchCriteria searchCriteria,
										   final Integer pageSizeUsed) {
		return searchRepository.searchForProductIds(searchCriteria, currentPageNumber, pageSizeUsed);
	}

	/**
	 * Get used page size.
	 *
	 * @param storeCode              store code
	 * @param numberOfResultsPerPage input page size
	 * @return page size
	 */
	protected Single<Integer> getPageSizeUsed(final String storeCode, final int numberOfResultsPerPage) {
		return numberOfResultsPerPage == 0 ? searchRepository.getDefaultPageSize(storeCode) : Single.just(numberOfResultsPerPage);
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}
}
