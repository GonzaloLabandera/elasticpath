/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.common.dto.StructuredErrorMessage;
import com.elasticpath.base.exception.structured.EpValidationException;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerMessageIds;
import com.elasticpath.validation.ConstraintViolationTransformer;


/**
 * Test <code>AddressServiceimpl</code>.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@RunWith(MockitoJUnitRunner.class)
public class AddressServiceImplTest {

	private static final String SHARED_ID = "Shared Id";
	private static final String STRUCTURED_ERROR_MESSAGES = "structuredErrorMessages";
	private static final String SHARED_ID_FIELD = "shared-id";
	private static final String ID_EXISTS_ERROR_MESSAGE = "Customer with the given shared Id already exists";
	private static final String ADDRESS_VALIDATION_FAILURE_MESSAGE = "Address validation failure.";

	@InjectMocks
	private AddressServiceImpl addressServiceImpl;

	@Mock 
	private Validator validator;
	@Mock
	private ConstraintViolationTransformer constraintViolationTransformer;

	/**
	 * Validating with an invalid customer address elicits an EpValidationException.
	 */
	@Test
	public void validateInvalidCustomerAddressThrowsException() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		EpValidationException exception = new EpValidationException(ADDRESS_VALIDATION_FAILURE_MESSAGE, Collections.emptyList());
		when(validator.validate(customerAddress)).thenThrow(exception);

		assertThatThrownBy(() -> addressServiceImpl.validateCustomerAddress(customerAddress))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining("Address validation failure.");
	}

	/**
	 * Saving an address with an invalid address throws an EpValidationException.
	 */
	@Test
	public void saveAddressWithInvalidAddressThrowsException() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		EpValidationException exception = new EpValidationException(ADDRESS_VALIDATION_FAILURE_MESSAGE, Collections.emptyList());
		when(validator.validate(customerAddress)).thenThrow(exception);

		when(customerAddress.isPersisted()).thenReturn(false);

		assertThatThrownBy(() -> addressServiceImpl.save(customerAddress))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining("Address validation failure.");
	}

	/**
	 * Updating a customer with invalid properties elicits an EpValidationException with message and list of structured error messages.
	 */
	@Test
	public void updateCustomerWithValidationViolationsThrowsException() {
		CustomerAddress customerAddress = mock(CustomerAddress.class);

		Set<ConstraintViolation<CustomerAddress>> addressViolations = new HashSet<>();
		final ConstraintViolation<CustomerAddress> constraintViolation = (ConstraintViolation<CustomerAddress>) mock(ConstraintViolation.class);
		addressViolations.add(constraintViolation);

		when(validator.validate(customerAddress)).thenReturn((addressViolations));

		when(constraintViolationTransformer.transform(addressViolations))
				.thenReturn(Collections.singletonList(createStructuredErrorMessageForId()));

		assertThatThrownBy(() -> addressServiceImpl.save(customerAddress))
				.isInstanceOf(EpValidationException.class)
				.hasMessageContaining(ADDRESS_VALIDATION_FAILURE_MESSAGE)
				.hasFieldOrPropertyWithValue(STRUCTURED_ERROR_MESSAGES, Collections.singletonList(createStructuredErrorMessageForId()));
	}

	/**
	 * Helper method returning a new <code>StructuredErrorMessage</code> indicating the shared Id already exists.
	 */
	private StructuredErrorMessage createStructuredErrorMessageForId() {
		return new StructuredErrorMessage(
				CustomerMessageIds.SHAREDID_ALREADY_EXISTS,
				ID_EXISTS_ERROR_MESSAGE,
				ImmutableMap.of(SHARED_ID_FIELD, SHARED_ID));
	}

}
