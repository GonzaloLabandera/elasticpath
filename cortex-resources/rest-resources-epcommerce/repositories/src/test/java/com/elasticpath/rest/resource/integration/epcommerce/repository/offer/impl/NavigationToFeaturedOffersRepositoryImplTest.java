/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.rest.definition.navigations.NavigationIdentifier;
import com.elasticpath.rest.definition.navigations.NavigationsIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;

/**
 * Test for {@link NavigationToFeaturedOffersRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NavigationToFeaturedOffersRepositoryImplTest {

	private static final String STORECODE = "STORECODE";
	private static final String CATEGORYCODE = "CATEGORYCODE";
	private static final long CATEGORY_UID = 1;

	private final NavigationIdentifier navigationIdentifier = NavigationIdentifier.builder()
			.withNavigations(NavigationsIdentifier.builder().withScope(StringIdentifier.of(STORECODE)).build())
			.withNodeId(StringIdentifier.of(CATEGORYCODE))
			.build();

	@InjectMocks
	private NavigationToFeaturedOffersRepositoryImpl<NavigationIdentifier, FeaturedOffersIdentifier> repository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private Category mockCategory;

	@Mock
	private Product mockProduct;

	@Before
	public void setUp() {
		when(categoryRepository.findByStoreAndCategoryCode(STORECODE, CATEGORYCODE)).thenReturn(Single.just(mockCategory));
		when(mockCategory.getUidPk()).thenReturn(CATEGORY_UID);
	}

	@Test
	public void verifyFeaturedOffersIdentifierIsReturnedWhenFeaturedProductsExists() {
		when(categoryRepository.getFeaturedProducts(CATEGORY_UID)).thenReturn(Observable.fromIterable(ImmutableList.of(mockProduct)));

		repository.getElements(navigationIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(1);
	}

	@Test
	public void verifyNoValueIsReturnedWhenFeaturedProductsDoNotExist() {
		when(categoryRepository.getFeaturedProducts(CATEGORY_UID)).thenReturn(Observable.empty());

		repository.getElements(navigationIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}
}
