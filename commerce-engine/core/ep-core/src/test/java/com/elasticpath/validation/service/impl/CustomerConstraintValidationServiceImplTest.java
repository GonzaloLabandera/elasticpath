package com.elasticpath.validation.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.validation.Validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.validation.groups.AccountValidation;
import com.elasticpath.validation.groups.UserValidation;

/**
 * Test class for {@link CustomerConstraintValidationServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerConstraintValidationServiceImplTest {

	@Mock
	private Validator validator;

	@Mock
	private Customer customer;

	@InjectMocks
	private CustomerConstraintValidationServiceImpl customerConstraintValidationService;

	@Test
	public void testValidateAnonymousUser() {
		when(customer.isAnonymous()).thenReturn(true);

		customerConstraintValidationService.validate(customer);
		verify(validator).validateProperty(customer, "username", Customer.class);
		verify(validator).validateProperty(customer, "email", Customer.class);
	}

	@Test
	public void testValidateAccountUser() {
		when(customer.getCustomerType()).thenReturn(CustomerType.ACCOUNT);

		customerConstraintValidationService.validate(customer);
		verify(validator).validate(customer, AccountValidation.class);
	}

	@Test
	public void testValidateRegisteredUser() {
		when(customer.getCustomerType()).thenReturn(CustomerType.REGISTERED_USER);

		customerConstraintValidationService.validate(customer);
		verify(validator).validate(customer, UserValidation.class);
	}
}
