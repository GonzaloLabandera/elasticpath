/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;

/**
 * Test for {@link CreateCartFormLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateCartFormLinksRepositoryImplTest {

	@InjectMocks
	private CreateCartFormLinksRepositoryImpl <CartsIdentifier, CreateCartFormIdentifier>repository;
	@Mock
	private MultiCartResolutionStrategyHolder holder;
	@Mock
	private MultiCartResolutionStrategy strategy;
	@Mock
	private ResourceOperationContext resourceOperationContext;
	@Mock
	private ShopperRepository shopperRepository;
	@Mock
	private Subject subject;

	@Test
	public void testGetElements() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);
		Shopper shopper = mock(Shopper.class);

		when(shopperRepository.findOrCreateShopper()).thenReturn(
				Single.just(shopper));
		when(strategy.supportsCreate(subject, shopper, SCOPE)).thenReturn(true);
		CartsIdentifier cartsIdentifier = CartsIdentifier.builder()
				.withScope(SCOPE_IDENTIFIER_PART).build();
		repository.getElements(cartsIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(identifier -> identifier.getCarts().equals(cartsIdentifier));
	}


	@Test
	public void testGetElementsWhenNotRegistered() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);
		Shopper shopper = mock(Shopper.class);

		when(shopperRepository.findOrCreateShopper()).thenReturn(
				Single.just(shopper));

		when(strategy.supportsCreate(subject, shopper, SCOPE)).thenReturn(false);
		CartsIdentifier cartsIdentifier = CartsIdentifier.builder()
				.withScope(SCOPE_IDENTIFIER_PART).build();
		repository.getElements(cartsIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(0);
	}


}
