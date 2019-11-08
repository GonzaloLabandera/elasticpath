/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for showing facets link if there are facets.
 * @param <I> offer search result identifier
 * @param <LI> facets identifier
 */
@Component
public class OfferSearchResultToFacetsRepositoryImpl<I extends OfferSearchResultIdentifier, LI extends FacetsIdentifier>
		implements LinksRepository<OfferSearchResultIdentifier, FacetsIdentifier> {

	private SearchRepository searchRepository;
	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<FacetsIdentifier> getElements(final OfferSearchResultIdentifier identifier) {
		String pageSizeString = identifier.getSearchId().getValue()
				.get(PaginationEntity.PAGE_SIZE_PROPERTY);

		if ("null".equals(pageSizeString) || StringUtils.isBlank(pageSizeString)) {
			pageSizeString = "0";
		}
		if (!NumberUtils.isDigits(pageSizeString)) {
			return Observable.error(ResourceOperationFailure.badRequestBody("Invalid page size"));
		}

		int pageSize = Integer.parseInt(pageSizeString);
		IdentifierPart<Map<String, String>> searchIdentifier = identifier.getSearchId();
		IdentifierPart<Map<String, String>> appliedFacetsIdentifier = identifier.getAppliedFacets();
		Map<String, String> appliedFacets = appliedFacetsIdentifier.getValue();
		Map<String, String> searchId = searchIdentifier.getValue();

		Subject subject = resourceOperationContext.getSubject();
		Locale locale = SubjectUtil.getLocale(subject);
		Currency currency = SubjectUtil.getCurrency(subject);

		String keyword = searchId.get(SearchOfferEntity.KEYWORDS_PROPERTY);
		String categoryCode = searchId.get(OffersResourceConstants.CATEGORY_CODE_PROPERTY);
		String scope = identifier.getScope().getValue();

		int startPageNumber = identifier.getPageId().getValue();

		OfferSearchData offerSearchData = new OfferSearchData(1, pageSize, scope, appliedFacets, keyword);
		offerSearchData.setCategoryCode(categoryCode);

		return searchRepository.getSearchCriteria(offerSearchData, locale, currency)
				.flatMapObservable(searchCriteria -> searchRepository.getFacetFields(searchCriteria, startPageNumber, pageSize))
				.isEmpty()
				.flatMapObservable(empty -> empty ? Observable.empty()
						: buildFacetsIdentifier(appliedFacetsIdentifier, searchIdentifier, identifier.getScope()));
	}

	private Observable<FacetsIdentifier> buildFacetsIdentifier(final IdentifierPart<Map<String, String>> appliedFacets,
															   final IdentifierPart<Map<String, String>> searchIdentifier,
															   final IdentifierPart<String> scope) {
		return Observable.just(FacetsIdentifier.builder()
				.withAppliedFacets(appliedFacets)
				.withScope(scope)
				.withSearchId(searchIdentifier)
				.build());
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}
}
