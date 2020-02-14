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

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;

/**
 * Test for {@link AddToCartFormsLinksRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AddToCartFormsLinksRepositoryTest {

	private static final String TEST_VALUE = "test";

	@InjectMocks
	private AddToCartFormsLinksRepository<ItemIdentifier, AddToCartFormsIdentifier> repository;
	@Mock
	private MultiCartResolutionStrategyHolder holder;
	@Mock
	private MultiCartResolutionStrategy strategy;
	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private CustomerSessionRepository customerSessionRepository;

	@Test
	public void testFindElements() {

		ItemIdentifier itemIdentifier = ItemIdentifier.builder()
				.withItemId(CompositeIdentifier.of(TEST_VALUE, TEST_VALUE))
				.withScope(SCOPE_IDENTIFIER_PART)
				.build();

		Subject subject = mock(Subject.class);

		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);
		CustomerSession customerSession = mock(CustomerSession.class);
		Shopper shopper = mock(Shopper.class);

		when(customerSessionRepository.findOrCreateCustomerSession()).thenReturn(Single.just(customerSession));

		when(strategy.supportsCreate(subject, shopper, SCOPE)).thenReturn(true);
		when(customerSession.getShopper()).thenReturn(shopper);

		repository.getElements(itemIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(1);

	}

	@Test
	public void testFindElementsWhenNotSupported() {

		ItemIdentifier itemIdentifier = ItemIdentifier.builder()
				.withItemId(CompositeIdentifier.of(TEST_VALUE, TEST_VALUE))
				.withScope(SCOPE_IDENTIFIER_PART)
				.build();

		Subject subject = mock(Subject.class);

		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);
		CustomerSession customerSession = mock(CustomerSession.class);
		Shopper shopper = mock(Shopper.class);

		when(customerSessionRepository.findOrCreateCustomerSession()).thenReturn(Single.just(customerSession));

		when(strategy.supportsCreate(subject, shopper, SCOPE)).thenReturn(false);
		when(customerSession.getShopper()).thenReturn(shopper);

		repository.getElements(itemIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(0);

	}

}
