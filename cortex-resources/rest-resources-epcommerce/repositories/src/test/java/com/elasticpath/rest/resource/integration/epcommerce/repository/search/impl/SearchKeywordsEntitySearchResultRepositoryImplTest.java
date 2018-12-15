/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.rest.definition.searches.KeywordSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;

public class SearchKeywordsEntitySearchResultRepositoryImplTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private SearchKeywordsEntitySearchResultRepositoryImpl<SearchKeywordsEntity, KeywordSearchResultIdentifier> repository;

	@Before
	public void setUp() {
		repository = new SearchKeywordsEntitySearchResultRepositoryImpl<>();
		repository.setSearchRepository(new SearchRepositoryImpl());
	}

	@Test
	public void shouldCreateIdentifier() {
		repository.submit(SearchKeywordsEntity.builder()
						.withKeywords("a")
						.withPageSize(1)
						.build(),
				SCOPE)
				.test()
				.assertComplete();
	}

}