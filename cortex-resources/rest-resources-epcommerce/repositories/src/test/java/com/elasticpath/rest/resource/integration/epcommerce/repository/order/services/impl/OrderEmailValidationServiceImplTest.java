/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.order.services.impl;

import static org.mockito.Mockito.when;

import java.util.Optional;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.orders.EmailInfoIdentifier;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.StructuredErrorMessageIdConstants;
import com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Test for {@link OrderEmailValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderEmailValidationServiceImplTest {

	private static final String USER_ID = "userId";
	private static final String VALID_EMAIL = "valid@valid.com";
	private static final String SCOPE = "scope";
	private static final String ORDER_ID = "orderId";
	private static final String MESSAGE_NEED_EMAIL = "An email address must be provided before you can complete the purchase.";

	@Mock
	private Customer customer;

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@InjectMocks
	private OrderEmailValidationServiceImpl validationService;

	@Test
	public void shouldNotReturnLinkedMessagesWhenEmailExistsAndValid() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(customer.getEmail()).thenReturn(VALID_EMAIL);

		validationService.validateEmailAddressExists(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void shouldReturnLinkedMessagesWhenAnonymousUser() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(customer.getEmail()).thenReturn(AuthenticationConstants.ANONYMOUS_USER_ID);

		validationService.validateEmailAddressExists(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(linkedMessage -> linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO))
				.assertValue(linkedMessage -> linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_EMAIL))
				.assertValue(linkedMessage -> linkedMessage.getDebugMessage().equals(MESSAGE_NEED_EMAIL))
				.assertValue(linkedMessage -> linkedMessage.getLinkedIdentifier()
						.equals(Optional.of(EmailInfoIdentifier.builder().withOrder(getOrderIdentifier()).build())));
	}

	@Test
	public void shouldReturnLinkedMessagesWhenEmailDoesNotExist() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_ID);
		when(customerRepository.getCustomer(USER_ID)).thenReturn(Single.just(customer));
		when(customer.getEmail()).thenReturn(null);

		validationService.validateEmailAddressExists(getOrderIdentifier())
				.test()
				.assertNoErrors()
				.assertValueCount(1)
				.assertValue(linkedMessage -> linkedMessage.getType().equals(StructuredMessageTypes.NEEDINFO))
				.assertValue(linkedMessage -> linkedMessage.getId().equals(StructuredErrorMessageIdConstants.NEED_EMAIL))
				.assertValue(linkedMessage -> linkedMessage.getDebugMessage().equals(MESSAGE_NEED_EMAIL))
				.assertValue(linkedMessage -> linkedMessage.getLinkedIdentifier()
						.equals(Optional.of(EmailInfoIdentifier.builder().withOrder(getOrderIdentifier()).build())));
	}

	private OrderIdentifier getOrderIdentifier() {
		return OrderIdentifier.builder()
				.withOrderId(StringIdentifier.of(ORDER_ID))
				.withScope(StringIdentifier.of(SCOPE))
				.build();
	}

}