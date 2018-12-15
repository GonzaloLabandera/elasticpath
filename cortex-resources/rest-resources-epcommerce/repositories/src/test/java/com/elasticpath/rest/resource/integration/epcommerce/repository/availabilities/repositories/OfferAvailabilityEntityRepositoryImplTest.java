/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.repositories;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.availabilities.AvailabilityEntity;
import com.elasticpath.rest.definition.availabilities.AvailabilityForOfferIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.AvailabilityTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.catalog.ProductLookup;

/**
 * Test for {@link OfferAvailabilityEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferAvailabilityEntityRepositoryImplTest {

	private static final String OFFER_ID = "guid";

	@Mock
	private ProductLookup productLookup;
	@Mock
	private Product product;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private OfferAvailabilityEntityRepositoryImpl<AvailabilityEntity, AvailabilityForOfferIdentifier> offerAvailabilityEntityRepository;

	@Before
	public void setUp() {
		offerAvailabilityEntityRepository.setReactiveAdapter(reactiveAdapter);
		when(productLookup.findByGuid(OFFER_ID)).thenReturn(product);
	}

	@Test
	public void findOneReturnsOfferAvailabilityEntityWithReleaseDateAndState() {
		offerAvailabilityEntityRepository.findOne(
				AvailabilityTestFactory.createAvailabilityForOfferIdentifier(OFFER_ID, ResourceTestConstants.SCOPE))
				.test()
				.assertValue(offerAvailabilityEntity -> offerAvailabilityEntity.getState().equals(Availability.NOT_AVAILABLE.getName()));
	}

	@Test
	public void findOneReturnsErrorWhenNoSkuFoundForTheGivenOfferId() {
		String productNotFound = "Product not found";
		when(productLookup.findByGuid(OFFER_ID)).thenReturn(null);
		offerAvailabilityEntityRepository.findOne(
				AvailabilityTestFactory.createAvailabilityForOfferIdentifier(OFFER_ID, ResourceTestConstants.SCOPE))
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(productNotFound);
	}
}