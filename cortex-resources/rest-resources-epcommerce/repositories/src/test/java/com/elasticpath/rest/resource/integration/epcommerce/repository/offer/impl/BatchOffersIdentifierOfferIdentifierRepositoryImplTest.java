/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.offer.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.PRODUCT_GUID_KEY;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.offers.BatchOffersIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormIdentifier;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.id.type.StringListIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;

/**
 * Test for {@link BatchOffersIdentifierOfferIdentifierRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchOffersIdentifierOfferIdentifierRepositoryImplTest {

	private static final String VALID_GUID = "guid";

	private static final String VALID_GUID2 = "guid2";

	private static final String INVALID_GUID = "invalidGuid";

	private static final String NOT_FOUND = "notFound";

	private static final String SCOPE = "scope";

	private final IdentifierPart<String> scope = StringIdentifier.of(SCOPE);

	private final BatchOffersLookupFormIdentifier lookupFormIdentifier = BatchOffersLookupFormIdentifier.builder()
			.withScope(scope)
			.build();

	@InjectMocks
	private BatchOffersIdentifierOfferIdentifierRepositoryImpl<BatchOffersIdentifier, OfferIdentifier> repository;

	@Mock
	private StoreProductRepository storeProductRepository;

	@Mock
	private StoreProduct product;

	@Mock
	private StoreProduct product2;

	@Before
	public void setUp() {
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, VALID_GUID))
				.thenReturn(Single.just(product));
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, VALID_GUID2))
				.thenReturn(Single.just(product2));
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, INVALID_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		when(product.getGuid()).thenReturn(VALID_GUID);
		when(product2.getGuid()).thenReturn(VALID_GUID2);
	}

	@Test
	public void verifyOffersAreReturnedWithValidCodes() {
		repository.getElements(buildBatchOffersIdentifier(ImmutableList.of(VALID_GUID, VALID_GUID2)))
				.test()
				.assertNoErrors()
				.assertValueAt(0, assertGuidInOfferIdentifier(VALID_GUID))
				.assertValueAt(1, assertGuidInOfferIdentifier(VALID_GUID2));
	}

	@Test
	public void verifyValidOffersAreStillReturnedEvenWithInvalidGuids() {
		repository.getElements(buildBatchOffersIdentifier(ImmutableList.of(INVALID_GUID, VALID_GUID, VALID_GUID2)))
				.test()
				.assertNoErrors()
				.assertValueAt(0, assertGuidInOfferIdentifier(VALID_GUID))
				.assertValueAt(1, assertGuidInOfferIdentifier(VALID_GUID2));
	}

	@Test
	public void verifyInvalidGuidDoesNotThrowError() {
		repository.getElements(buildBatchOffersIdentifier(ImmutableList.of(INVALID_GUID)))
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	private BatchOffersIdentifier buildBatchOffersIdentifier(final List<String> guids) {
		return BatchOffersIdentifier.builder()
					.withBatchOffersLookupForm(lookupFormIdentifier)
					.withBatchId(StringListIdentifier.of(guids))
					.build();
	}

	private Predicate<OfferIdentifier> assertGuidInOfferIdentifier(final String expected) {
		return offerIdentifier -> offerIdentifier.getOfferId().getValue().get(PRODUCT_GUID_KEY).equals(expected);
	}
}
