/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchOfferEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;

public class SearchOfferEntitySearchResultRepositoryImplTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private SearchOfferEntitySearchResultRepositoryImpl<SearchOfferEntity, OfferSearchResultIdentifier> repository;

	@Before
	public void setUp() {
		repository = new SearchOfferEntitySearchResultRepositoryImpl<>();
		repository.setSearchRepository(new SearchRepositoryImpl());
	}

	@Test
	public void shouldCreateIdentifier() {
		repository.submit(SearchOfferEntity.builder()
						.withKeywords("a")
						.withPageSize(1)
						.build(),
				SCOPE)
				.test()
				.assertComplete();
	}

}