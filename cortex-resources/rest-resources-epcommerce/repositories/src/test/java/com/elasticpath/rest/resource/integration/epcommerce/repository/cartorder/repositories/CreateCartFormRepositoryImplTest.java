/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE_IDENTIFIER_PART;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.carts.CartDescriptorEntity;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;

/**
 * Test for {@link CreateCartFormRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CreateCartFormRepositoryImplTest {

	@InjectMocks
	private CreateCartFormRepositoryImpl <CartDescriptorEntity, CreateCartFormIdentifier>repository;
	@Mock
	private MultiCartResolutionStrategyHolder holder;
	@Mock
	private MultiCartResolutionStrategy strategy;
	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private Subject subject;

	@Test
	public void testFindOne() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);

		ModifierField modifierField = mock(ModifierField.class);
		when(modifierField.getCode()).thenReturn("CODE");

		when(strategy.hasMulticartEnabled(SCOPE)).thenReturn(true);

		List<ModifierField> modifierFields = Collections.singletonList(modifierField);
		when(strategy.getModifierFields(SCOPE)).thenReturn(modifierFields);
		repository.findOne(CreateCartFormIdentifier.builder()
				.withCarts(CartsIdentifier.builder()
						.withScope(SCOPE_IDENTIFIER_PART).build())
				.build())
				.test()
				.assertNoErrors()
				.assertValueCount(1);
	}
	@Test
	public void testFindOneWhenMulticartNotSupported() {
		when(resourceOperationContext.getSubject()).thenReturn(subject);
		when(holder.getStrategies()).thenReturn(Collections.singletonList(strategy));

		when(strategy.isApplicable(subject)).thenReturn(true);

		when(strategy.hasMulticartEnabled(SCOPE)).thenReturn(false);
		repository.findOne(CreateCartFormIdentifier.builder()
				.withCarts(CartsIdentifier.builder()
						.withScope(SCOPE_IDENTIFIER_PART).build())
				.build())
				.test()
				.assertError(ResourceOperationFailure.class);
	}
}