/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.definition.offersearches.SearchOfferEntity.PAGE_SIZE_PROPERTY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.CATEGORY_CODE_PROPERTY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for getting all facet fields.
 * @param <I> facets identifier
 * @param <LI> facet identifier
 */
@Component
public class NavigationToOfferSearchResultsRepositoryImpl<I extends NavigationIdentifier, LI extends OfferSearchResultIdentifier>
		implements LinksRepository<NavigationIdentifier, OfferSearchResultIdentifier> {

	private SearchRepository searchRepository;

	@Override
	public Observable<OfferSearchResultIdentifier> getElements(final NavigationIdentifier identifier) {
		final IdentifierPart<String> storeCode = identifier.getNavigations().getScope();
		String categoryCode = identifier.getNodeId().getValue();

		return searchRepository.getDefaultPageSize(storeCode.getValue())
				.flatMapObservable(pageSize -> Observable.just(OfferSearchResultIdentifier.builder()
						.withPageId(IntegerIdentifier.of(1))
						.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
						.withSearchId(CompositeIdentifier.of(createSearchId(pageSize, storeCode.getValue(), categoryCode)))
						.withScope(storeCode)
						.build()));
	}

	private Map<String, String> createSearchId(final int pageSize, final String scope, final String categoryCode) {
		Map<String, String> searchId = new HashMap<>();
		searchId.put(CATEGORY_CODE_PROPERTY, categoryCode);
		searchId.put(PAGE_SIZE_PROPERTY, String.valueOf(pageSize));
		searchRepository.getDefaultSortAttributeForStore(scope).subscribe(sortAttribute -> searchId.put(SORT, sortAttribute.getGuid()));
		return searchId;
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}
}
