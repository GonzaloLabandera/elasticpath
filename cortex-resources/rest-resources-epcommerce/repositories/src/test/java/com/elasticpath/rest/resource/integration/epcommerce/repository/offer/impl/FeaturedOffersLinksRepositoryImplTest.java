/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.rest.definition.base.ScopeIdentifierPart;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offersearches.FeaturedOffersIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.category.CategoryRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl;

/**
 * Test for {@link FeaturedOffersRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FeaturedOffersLinksRepositoryImplTest {

	private static final String STORECODE = "STORECODE";
	private static final String CATEGORYCODE = "CATEGORYCODE";
	private static final long CATEGORY_UID = 1;
	private static final String PRODUCT_GUID = "PRODUCT_GUID";

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private  FeaturedOffersRepositoryImpl<FeaturedOffersIdentifier, OfferIdentifier> featuredOffersLinksRepository;

	@Mock
	private Category mockCategory;

	@Mock
	private Product mockProduct;


	@Test
	public void testGetElementWithValidIdentifier() {

		FeaturedOffersIdentifier featuredoffersIdentifier = getFeaturedOffersIdentifier();


		when(categoryRepository.findByStoreAndCategoryCode(STORECODE, CATEGORYCODE))
				.thenReturn(Single.just(mockCategory));

		when(mockCategory.getUidPk()).thenReturn(CATEGORY_UID);

		when(categoryRepository.getFeaturedProducts(CATEGORY_UID))
				.thenReturn(Observable.fromIterable(ImmutableList.of(mockProduct)));

		when(mockProduct.getGuid())
				.thenReturn(PRODUCT_GUID);

		Observable<OfferIdentifier> elements = featuredOffersLinksRepository.getElements(featuredoffersIdentifier);

		elements.map(identifier -> identifier.getOfferId().getValue().get(SearchRepositoryImpl.PRODUCT_GUID_KEY))
				.test()
				.assertNoErrors()
				.assertValue(PRODUCT_GUID);

	}

	private FeaturedOffersIdentifier getFeaturedOffersIdentifier() {
		return FeaturedOffersIdentifier.builder()
				.withCategoryId(StringIdentifier.of(CATEGORYCODE))
				.withScope(ScopeIdentifierPart.of(STORECODE))
				.build();
	}

	@Test
	public void testGetElementWithNoFeaturedProductsFound() {

		FeaturedOffersIdentifier featuredoffersIdentifier = getFeaturedOffersIdentifier();


		when(categoryRepository.findByStoreAndCategoryCode(STORECODE, CATEGORYCODE))
				.thenReturn(Single.just(mockCategory));

		when(mockCategory.getUidPk()).thenReturn(CATEGORY_UID);

		//no elements in the categories list
		when(categoryRepository.getFeaturedProducts(CATEGORY_UID))
				.thenReturn(Observable.empty());

		Observable<OfferIdentifier> elements = featuredOffersLinksRepository.getElements(featuredoffersIdentifier);

		elements.map(identifier -> identifier.getOfferId().getValue().get(SearchRepositoryImpl.PRODUCT_GUID_KEY))
				.test()
				.assertNoErrors()
				.assertValueCount(0);

	}
}
