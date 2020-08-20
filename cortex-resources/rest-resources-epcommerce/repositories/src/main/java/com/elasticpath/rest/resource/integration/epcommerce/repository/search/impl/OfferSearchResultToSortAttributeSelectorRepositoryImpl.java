/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.CATEGORY_CODE_PROPERTY;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.commons.util.impl.LocaleUtils;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Offer search result to sort attributes.
 * @param <I> OfferSearchResultIdentifier
 * @param <LI> SortAttributeSelectorIdentifier
 */
@Component
public class OfferSearchResultToSortAttributeSelectorRepositoryImpl<I extends OfferSearchResultIdentifier,
		LI extends SortAttributeSelectorIdentifier> implements LinksRepository<OfferSearchResultIdentifier, SortAttributeSelectorIdentifier> {

	private static final int PAGE_SIZE = 1;

	private SearchRepository searchRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<SortAttributeSelectorIdentifier> getElements(final OfferSearchResultIdentifier identifier) {
		Locale locale = SubjectUtil.getLocale(resourceOperationContext.getSubject());
		Currency currency = SubjectUtil.getCurrency(resourceOperationContext.getSubject());

		String localeCode = LocaleUtils.getCommerceLocalCode(locale);
		String scope = identifier.getScope().getValue();

		Map<String, String> searchId = identifier.getSearchId().getValue();
		String searchOffers = searchId.get(SearchOfferEntity.KEYWORDS_PROPERTY);
		String categoryCode = searchId.get(CATEGORY_CODE_PROPERTY);

		Map<String, String> appliedFacets = identifier.getAppliedFacets().getValue();

		OfferSearchData offerSearchData = new OfferSearchData(PAGE_SIZE, PAGE_SIZE, scope, appliedFacets, searchOffers);
		offerSearchData.setCategoryCode(categoryCode);

		return searchRepository.getSortAttributeGuidsForStoreAndLocale(scope, localeCode)
				.switchIfEmpty(Observable.empty())
				.flatMapSingle(guid -> searchRepository.getSearchCriteria(offerSearchData, locale, currency))
				.flatMapSingle(searchCriteria -> searchRepository.searchForProductIds(searchCriteria, PAGE_SIZE, PAGE_SIZE))
				.flatMap(result -> result.getTotalNumberOfResults() == 0 ? Observable.empty() : buildSortAttributeSelectorIdentifier(identifier));
	}

	private Observable<SortAttributeSelectorIdentifier> buildSortAttributeSelectorIdentifier(final OfferSearchResultIdentifier identifier) {
		return Observable.just(SortAttributeSelectorIdentifier.builder()
				.withOfferSearchResult(identifier)
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
