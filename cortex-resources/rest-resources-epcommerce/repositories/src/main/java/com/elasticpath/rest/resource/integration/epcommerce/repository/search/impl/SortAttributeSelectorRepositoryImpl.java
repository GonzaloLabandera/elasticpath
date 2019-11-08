/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Repository for selecting sort attributes.
 * @param <SI> the sort attribute selector identifier
 * @param <CI> the sort attribute selector choice identifier
 */
@Component
public class SortAttributeSelectorRepositoryImpl<SI extends SortAttributeSelectorIdentifier, CI extends SortAttributeSelectorChoiceIdentifier>
		implements SelectorRepository<SortAttributeSelectorIdentifier, SortAttributeSelectorChoiceIdentifier> {

	private SearchRepository searchRepository;

	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<SelectorChoice> getChoices(final SortAttributeSelectorIdentifier sortAttributeSelectorIdentifier) {
		String localeCode = SubjectUtil.getLocale(resourceOperationContext.getSubject()).getLanguage();
		OfferSearchResultIdentifier offerSearchResult = sortAttributeSelectorIdentifier.getOfferSearchResult();
		Map<String, String> searchId = offerSearchResult.getSearchId().getValue();
		String scope = offerSearchResult.getScope().getValue();
		return searchRepository.getSortAttributeGuidsForStoreAndLocale(scope, localeCode)
				.map(guid -> SelectorChoice.builder()
						.withChoice(buildSelectorChoice(sortAttributeSelectorIdentifier, guid))
						.withStatus(getChoiceStatus(searchId, guid))
						.build());
	}

	private SortAttributeSelectorChoiceIdentifier buildSelectorChoice(final SortAttributeSelectorIdentifier sortAttributeSelectorIdentifier,
																	  final String guid) {
		return SortAttributeSelectorChoiceIdentifier.builder()
				.withSortAttributeSelector(sortAttributeSelectorIdentifier)
				.withSortAttributeId(StringIdentifier.of(guid))
				.build();
	}

	private ChoiceStatus getChoiceStatus(final Map<String, String> searchId, final String guid) {
		return searchId.containsKey(SORT) && searchId.get(SORT).equals(guid) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	@Override
	public Single<Choice> getChoice(final SortAttributeSelectorChoiceIdentifier sortAttributeSelectorChoiceIdentifier) {
		String guid = sortAttributeSelectorChoiceIdentifier.getSortAttributeId().getValue();
		Map<String, String> searchId = sortAttributeSelectorChoiceIdentifier.getSortAttributeSelector().getOfferSearchResult()
				.getSearchId().getValue();
		return Single.just(Choice.builder()
				.withDescription(buildSortAttributeIdentifier(sortAttributeSelectorChoiceIdentifier))
				.withStatus(getChoiceStatus(searchId, guid))
				.withAction(sortAttributeSelectorChoiceIdentifier)
				.build());
	}

	private SortAttributeIdentifier buildSortAttributeIdentifier(final SortAttributeSelectorChoiceIdentifier sortAttributeSelectorChoiceIdentifier) {
		return SortAttributeIdentifier.builder()
				.withSortAttributeSelectorChoice(sortAttributeSelectorChoiceIdentifier)
				.build();
	}

	@Override
	public Single<SelectResult<SortAttributeSelectorIdentifier>> selectChoice(
			final SortAttributeSelectorChoiceIdentifier sortAttributeSelectorChoiceIdentifier) {
		OfferSearchResultIdentifier offerSearchResult = sortAttributeSelectorChoiceIdentifier.getSortAttributeSelector().getOfferSearchResult();
		Map<String, String> searchId = new HashMap<>(offerSearchResult.getSearchId().getValue());
		String guid = sortAttributeSelectorChoiceIdentifier.getSortAttributeId().getValue();
		searchId.put(SORT, guid);
		return Single.just(SelectResult.<SortAttributeSelectorIdentifier>builder()
				.withIdentifier(buildSortAttributeSelectorIdentifier(offerSearchResult, searchId))
				.withStatus(searchId.containsKey(SORT) && searchId.get(SORT).equals(guid) ? SelectStatus.EXISTING : SelectStatus.SELECTED)
				.build());
	}

	private SortAttributeSelectorIdentifier buildSortAttributeSelectorIdentifier(final OfferSearchResultIdentifier offerSearchResultIdentifier,
																				 final Map<String, String> searchId) {

		return SortAttributeSelectorIdentifier.builder()
				.withOfferSearchResult(buildOfferSearchResultIdentifier(offerSearchResultIdentifier, searchId))
				.build();
	}

	private OfferSearchResultIdentifier buildOfferSearchResultIdentifier(final OfferSearchResultIdentifier offerSearchResultIdentifier,
																		 final Map<String, String> searchId) {
		return OfferSearchResultIdentifier.builder()
				.withAppliedFacets(offerSearchResultIdentifier.getAppliedFacets())
				.withScope(offerSearchResultIdentifier.getScope())
				.withSearchId(CompositeIdentifier.of(searchId))
				.withPageId(IntegerIdentifier.of(1))
				.build();
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
