/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OfferSearchUtil.buildFacetIdentifier;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static com.elasticpath.service.search.solr.FacetConstants.APPLIED_FACETS_SEPARATOR;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_COUNT;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_DISPLAY_NAME;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_FILTER;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetSelectorIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetValueIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.identity.util.SubjectUtil;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;
import com.elasticpath.service.search.solr.FacetValue;

/**
 * Implementation of a repository for getting facet values of a facet field.
 *
 * @param <SI> the selector identifier type
 * @param <CI> the choice identifier type
 */
@Component
public class FacetSelectorRepositoryImpl<SI extends FacetSelectorIdentifier, CI extends FacetSelectorChoiceIdentifier>
		implements SelectorRepository<FacetSelectorIdentifier, FacetSelectorChoiceIdentifier> {

	private SearchRepository searchRepository;
	private ResourceOperationContext resourceOperationContext;

	@Override
	public Observable<SelectorChoice> getChoices(final FacetSelectorIdentifier facetSelectorIdentifier) {
		FacetIdentifier facetIdentifier = facetSelectorIdentifier.getFacet();
		FacetsIdentifier facetsIdentifier = facetIdentifier.getFacets();
		String scope = facetsIdentifier.getScope().getValue();

		Subject subject = resourceOperationContext.getSubject();
		Locale locale = SubjectUtil.getLocale(subject);
		Currency currency = SubjectUtil.getCurrency(subject);

		String facetGuid = facetIdentifier.getFacetId().getValue();
		Map<String, String> searchId = facetsIdentifier.getSearchId().getValue();

		String keyword = searchId.get(SearchOfferEntity.KEYWORDS_PROPERTY);
		String categoryCode = searchId.get(OffersResourceConstants.CATEGORY_CODE_PROPERTY);
		Map<String, String> appliedFacets = facetSelectorIdentifier.getFacet().getFacets().getAppliedFacets().getValue();

		OfferSearchData offerSearchData = new OfferSearchData(1, 1, scope, appliedFacets, keyword);
		offerSearchData.setCategoryCode(categoryCode);

		return searchRepository.getSearchCriteria(offerSearchData, locale, currency)
				.flatMapObservable(searchCriteria -> searchRepository.getFacetValues(facetGuid, searchCriteria)
				.map(facetValue -> buildSelectorChoice(facetIdentifier, facetGuid, appliedFacets, facetValue)));
	}

	private SelectorChoice buildSelectorChoice(final FacetIdentifier facetIdentifier, final String facetGuid,
											   final Map<String, String> appliedFacets, final FacetValue facetValue) {
		return SelectorChoice.builder()
				.withStatus(getChoiceStatus(appliedFacets, facetGuid, facetValue.getFacetFilter()))
				.withChoice(buildFacetSelectorChoiceIdentifier(facetValue, facetIdentifier))
				.build();
	}

	private ChoiceStatus getChoiceStatus(final Map<String, String> appliedFacets, final String facetGuid, final String facetName) {
		if (appliedFacets.containsKey(facetGuid)) {
			for (String facet : appliedFacets.get(facetGuid).split(APPLIED_FACETS_SEPARATOR)) {
				if (facetName.equals(facet)) {
					return ChoiceStatus.CHOSEN;
				}
			}
		}
		return ChoiceStatus.CHOOSABLE;
	}

	private FacetSelectorChoiceIdentifier buildFacetSelectorChoiceIdentifier(final FacetValue facetValue, final FacetIdentifier facetIdentifier) {
		return FacetSelectorChoiceIdentifier.builder()
				.withFacetValue(buildFacetValueIdentifier(facetValue, facetIdentifier))
				.build();
	}

	private FacetValueIdentifier buildFacetValueIdentifier(final FacetValue facetValue, final FacetIdentifier facetIdentifier) {
		Map<String, String> facetValueMap = ImmutableMap.of(FACET_VALUE_DISPLAY_NAME, facetValue.getDisplayName(),
				FACET_VALUE_COUNT, String.valueOf(facetValue.getCount()),
				FACET_VALUE_FILTER, facetValue.getFacetFilter());

		return FacetValueIdentifier.builder()
				.withFacet(facetIdentifier)
				.withFacetValueId(CompositeIdentifier.of(facetValueMap))
				.build();
	}


	@Override
	public Single<Choice> getChoice(final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier) {
		FacetValueIdentifier facetValueIdentifier = facetSelectorChoiceIdentifier.getFacetValue();
		FacetIdentifier facetIdentifier = facetValueIdentifier.getFacet();
		Map<String, String> appliedFacets = facetIdentifier.getFacets().getAppliedFacets().getValue();
		Map<String, String> facetMap = facetValueIdentifier.getFacetValueId().getValue();
		String facetGuid = facetIdentifier.getFacetId().getValue();
		String facetName = facetMap.get(FACET_VALUE_FILTER);
		return Single.just(Choice.builder()
				.withAction(facetSelectorChoiceIdentifier)
				.withStatus(getChoiceStatus(appliedFacets, facetGuid, facetName))
				.withDescription(facetSelectorChoiceIdentifier.getFacetValue())
				.build());
	}

	@Override
	public Single<SelectResult<FacetSelectorIdentifier>> selectChoice(final FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier) {
		FacetValueIdentifier facetValueIdentifier = facetSelectorChoiceIdentifier.getFacetValue();
		FacetIdentifier facetIdentifier = facetValueIdentifier.getFacet();
		String facetGuid = facetIdentifier.getFacetId().getValue();

		Map<String, String> facetValueMap = facetValueIdentifier.getFacetValueId().getValue();

		String facetName = facetValueMap.get(FACET_VALUE_FILTER);

		FacetsIdentifier facetsIdentifier = facetIdentifier.getFacets();
		Map<String, String> appliedFacetsMap = new HashMap<>(facetsIdentifier.getAppliedFacets().getValue());

		String updatedFacetValue = updateAppliedFacets(facetName, appliedFacetsMap.get(facetGuid));
		if (StringUtils.isNotEmpty(updatedFacetValue)) {
			appliedFacetsMap.put(facetGuid, updatedFacetValue);
		} else {
			appliedFacetsMap.remove(facetGuid);
		}

		return Single.just(buildSelectResult(facetGuid, facetsIdentifier, appliedFacetsMap));
	}

	private SelectResult<FacetSelectorIdentifier> buildSelectResult(final String facetGuid,
																	final FacetsIdentifier facetsIdentifier,
																	final Map<String, String> appliedFacetsMap) {
		return SelectResult.<FacetSelectorIdentifier>builder()
				.withIdentifier(buildFacetSelectorIdentifier(facetGuid, facetsIdentifier, appliedFacetsMap))
				.withStatus(SelectStatus.EXISTING)
				.build();
	}

	private FacetSelectorIdentifier buildFacetSelectorIdentifier(final String facetGuid,
																 final FacetsIdentifier facetsIdentifier,
																 final Map<String, String> appliedFacetsMap) {
		return FacetSelectorIdentifier.builder()
				.withFacet(buildFacetIdentifier(buildFacetsIdentifier(facetsIdentifier, appliedFacetsMap), facetGuid))
				.build();
	}

	private FacetsIdentifier buildFacetsIdentifier(final FacetsIdentifier facetsIdentifier, final Map<String, String> appliedFacetsMap) {
		return FacetsIdentifier.builder()
				.withScope(facetsIdentifier.getScope())
				.withSearchId(facetsIdentifier.getSearchId())
				.withAppliedFacets(CompositeIdentifier.of(appliedFacetsMap.isEmpty() ? DEFAULT_APPLIED_FACETS : appliedFacetsMap))
				.build();
	}

	private String updateAppliedFacets(final String facetName, final String appliedFacets) {
		StringBuilder stringBuilder = new StringBuilder();
		String appliedFacetsSafe = StringUtils.defaultIfEmpty(appliedFacets, StringUtils.EMPTY);
		for (String facet : appliedFacetsSafe.split(APPLIED_FACETS_SEPARATOR)) {
			if (!facet.equals(facetName)) {
				stringBuilder.append(facet).append(APPLIED_FACETS_SEPARATOR);
			}
		}
		// remove last separator if it exists
		stringBuilder.setLength(Math.max(stringBuilder.length() - 1, 0));
		// choice not currently selected so add it to applied facets
		if (stringBuilder.length() == appliedFacetsSafe.length()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(APPLIED_FACETS_SEPARATOR);
			}
			stringBuilder.append(facetName);
		}
		return stringBuilder.toString();
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
