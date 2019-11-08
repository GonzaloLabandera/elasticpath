/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.navigations.NavigationsIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Test for {@link NavigationToOfferSearchResultsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NavigationToOfferSearchResultsRepositoryImplTest {

	private static final String NODE_ID = "nodeId";

	private static final String SCOPE = "scope";

	@InjectMocks
	private NavigationToOfferSearchResultsRepositoryImpl<NavigationIdentifier, OfferSearchResultIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	private final NavigationIdentifier navigationIdentifier = NavigationIdentifier.builder()
			.withNodeId(StringIdentifier.of(NODE_ID))
			.withNavigations(NavigationsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build())
			.build();

	@Test
	public void testGetElementsReturnsIdentifier() {
		when(searchRepository.getDefaultPageSize(anyString())).thenReturn(Single.just(1));
		when(searchRepository.getDefaultSortAttributeForStore(anyString())).thenReturn(Maybe.empty());

		repository.getElements(navigationIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(1);
	}
}
