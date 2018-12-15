/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SKU_CODE;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.prices.OfferPriceRangeIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.price.PriceRepository;

/**
 * Tests for {@link PriceRangeToOfferRepositoryImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class PriceToOfferRepositoryImplTest {

	private final OfferIdentifier offerIdentifier = IdentifierTestFactory.buildOfferIdentifier(SCOPE, SKU_CODE);

	@Mock
	private PriceRepository priceRepository;

	@InjectMocks
	private PriceRangeToOfferRepositoryImpl<OfferIdentifier, OfferPriceRangeIdentifier> priceRangeToOfferRepository;

	@Test
	public void verifyGetElementsReturnsEmptyWhenPriceDoesNotExistForMinprice() {
		when(priceRepository.priceExistsForProduct(SCOPE, SKU_CODE)).thenReturn(Single.just(false));
		priceRangeToOfferRepository.getElements(offerIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyGetElementsReturnsNonEmptyWhenPriceExistsForMinprice() {
		when(priceRepository.priceExistsForProduct(SCOPE, SKU_CODE)).thenReturn(Single.just(true));
		priceRangeToOfferRepository.getElements(offerIdentifier)
				.map(OfferPriceRangeIdentifier::getOffer)
				.test()
				.assertValue(offerIdentifier);
	}
}