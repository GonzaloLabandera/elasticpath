/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.createSearchCriteria;

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
import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchOfferEntity;
import com.elasticpath.rest.definition.searches.SearchesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.pagination.PaginationEntity;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;

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
	private StoreRepository storeRepository;

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
		SearchesIdentifier searchesIdentifier = identifier.getSearches();
		String scope = searchesIdentifier.getScope().getValue();

		return storeRepository.findStoreAsSingle(scope)
				.map(store -> createSearchCriteria(keyword, store, appliedFacets, locale, currency, true))
				.flatMapObservable(searchCriteria -> searchRepository.getFacetFields(searchCriteria,
						pageSize))
				.isEmpty()
				.flatMapObservable(empty -> empty ? Observable.empty()
						: buildFacetsIdentifier(appliedFacetsIdentifier, searchIdentifier, searchesIdentifier));
	}

	private Observable<FacetsIdentifier> buildFacetsIdentifier(final IdentifierPart<Map<String, String>> appliedFacets,
															   final IdentifierPart<Map<String, String>> searchIdentifier,
															   final SearchesIdentifier searchesIdentifier) {
		return Observable.just(FacetsIdentifier.builder()
				.withAppliedFacets(appliedFacets)
				.withSearches(searchesIdentifier)
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

	@Reference
	public void setStoreRepository(final StoreRepository storeRepository) {
		this.storeRepository = storeRepository;
	}
}
