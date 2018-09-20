/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.impl;

import static org.mockito.Mockito.when;

import java.util.Objects;
import java.util.Optional;

import io.reactivex.Maybe;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.rest.definition.orders.BillingaddressInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for {@link OrderBillingAddressValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderBillingAddressValidationServiceImplTest {

	private static final String SCOPE = "scope";
	private static final String ORDER_ID = "orderId";
	private static final String MESSAGE_NEED_BILLING_ADDRESS = "A billing address must be provided before you can complete the purchase.";

	@Mock
	private CartOrder cartOrder;

	@Mock
	private Address address;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@InjectMocks
	private OrderBillingAddressValidationServiceImpl validationService;

	@Test
	public void shouldReturnFalseWhenBillingAddressIsNotMissing() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.getBillingAddress(cartOrder)).thenReturn(Maybe.just(address));
		validationService.isMissingBillingAddress(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void shouldReturnTrueWhenBillingAddressIsMissing() {
		when(cartOrderRepository.findByGuidAsSingle(SCOPE, ORDER_ID)).thenReturn(Single.just(cartOrder));
		when(cartOrderRepository.getBillingAddress(cartOrder)).thenReturn(Maybe.empty());
		validationService.isMissingBillingAddress(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void shouldReturnLinkedMessagesWhenBillingAddressDoesNotExist() {
		BillingaddressInfoIdentifier billingaddressInfoIdentifier = BillingaddressInfoIdentifier.builder()
				.withOrder(getOrderIdentifier())
				.build();

		validationService.getLinkedMessage(true, getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(billingaddressInfoIdentifierLinkedMessage ->
						Objects.equals(billingaddressInfoIdentifierLinkedMessage.getId(), StructuredErrorMessageIdConstants.ID_NEED_BILLING_ADDRESS))
				.assertValue(billingaddressInfoIdentifierLinkedMessage ->
						Objects.equals(billingaddressInfoIdentifierLinkedMessage.getType(), StructuredMessageTypes.NEEDINFO))
				.assertValue(billingaddressInfoIdentifierLinkedMessage ->
						Objects.equals(billingaddressInfoIdentifierLinkedMessage.getDebugMessage(), MESSAGE_NEED_BILLING_ADDRESS))
				.assertValue(billingaddressInfoIdentifierLinkedMessage ->
						billingaddressInfoIdentifierLinkedMessage.getLinkedIdentifier().equals(Optional.of(billingaddressInfoIdentifier)));
	}

	@Test
	public void shouldNotReturnLinkedMessagesWhenBillingAddressExists() {
		validationService.getLinkedMessage(false, getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	private OrderIdentifier getOrderIdentifier() {
		return OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(ORDER_ID))
				.withScope(StringIdentifier.of(SCOPE))
				.build();
	}

}