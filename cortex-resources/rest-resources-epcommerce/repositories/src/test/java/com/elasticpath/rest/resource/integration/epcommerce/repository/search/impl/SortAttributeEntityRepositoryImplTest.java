/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.search.SortValue;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeEntity;
import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Test for {@link SortAttributeEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SortAttributeEntityRepositoryImplTest {
	private static final String RATING_GUID = "ratingGuid";

	private final IdentifierPart<Map<String, String>> appliedFacets = CompositeIdentifier.of(DEFAULT_APPLIED_FACETS);

	private final IdentifierPart<Map<String, String>> searchId = CompositeIdentifier.of(ImmutableMap.of(SORT, RATING_GUID));

	private final IdentifierPart<Integer> pageId = IntegerIdentifier.of(1);

	private final IdentifierPart<String> scopeId = StringIdentifier.of(SCOPE);

	private final OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
			.withSearchId(searchId)
			.withScope(scopeId)
			.withAppliedFacets(appliedFacets)
			.withPageId(pageId)
			.build();

	private final SortAttributeSelectorIdentifier selectorIdentifier = SortAttributeSelectorIdentifier.builder()
			.withOfferSearchResult(offerSearchResultIdentifier)
			.build();

	private final SortAttributeSelectorChoiceIdentifier selectorChoiceIdentifier = SortAttributeSelectorChoiceIdentifier.builder()
			.withSortAttributeId(StringIdentifier.of(RATING_GUID))
			.withSortAttributeSelector(selectorIdentifier)
			.build();

	@InjectMocks
	private SortAttributeEntityRepositoryImpl<SortAttributeEntity, SortAttributeIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private SortValue sortValue;

	@Test
	public void verifyFindOneReturnsSortAttribute() {
		String localeCode = "en";
		String name = "name";

		SortAttributeIdentifier sortAttributeIdentifier = SortAttributeIdentifier.builder()
				.withSortAttributeSelectorChoice(selectorChoiceIdentifier)
				.build();

		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocale(SCOPE, USER_ID, LOCALE));
		when(searchRepository.getSortValueByGuidAndLocaleCode(RATING_GUID, localeCode))
				.thenReturn(Single.just(sortValue));
		when(sortValue.getName()).thenReturn(name);

		repository.findOne(sortAttributeIdentifier)
				.test()
				.assertValue(sortAttributeEntity -> sortAttributeEntity.getDisplayName().equals(name));
	}
}
