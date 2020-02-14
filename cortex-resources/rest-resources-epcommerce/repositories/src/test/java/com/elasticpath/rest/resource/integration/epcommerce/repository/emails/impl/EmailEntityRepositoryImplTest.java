/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.emails.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Completable;
import io.reactivex.Single;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.emails.EmailEntity;
import com.elasticpath.rest.definition.emails.EmailIdentifier;
import com.elasticpath.rest.definition.emails.EmailsIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.impl.CustomerRepositoryImpl;

/**
 * Test for {@link EmailEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailEntityRepositoryImplTest {

	private static final String SCOPE = "SCOPE";
	private static final String EXISTING_EMAIL = "Godzilla@lizard.com";
	private static final String NEW_EMAIL = "Rex@lizard.com";
	private static final String USER_ID = "Godzilla";
	private static final String USER_GUID = "abc-123-def-456";
	private static final String CUSTOMER_WAS_NOT_FOUND = "Customer was not found.";

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private Customer customer;

	@Mock
	private CustomerRepositoryImpl customerRepository;

	@InjectMocks
	private EmailEntityRepositoryImpl<EmailEntity, EmailIdentifier> emailEntityRepository;

	@Before
	public void initialize() {
		when(resourceOperationContext.getUserIdentifier()).thenReturn(USER_GUID);
		when(customerRepository.getCustomer(USER_GUID)).thenReturn(Single.just(customer));
		when(customer.getEmail()).thenReturn(EXISTING_EMAIL);
		when(customer.getUserId()).thenReturn(USER_ID);
		when(customerRepository.updateCustomer(customer)).thenReturn(Completable.complete());
		emailEntityRepository.setResourceOperationContext(resourceOperationContext);
		emailEntityRepository.setCustomerRepository(customerRepository);
	}

	@Test
	public void findOneProducesCorrectCustomerWithValidEmailIdentifier() {
		emailEntityRepository.findOne(createEmailIdentifier(EXISTING_EMAIL))
				.test()
				.assertNoErrors()
				.assertValue(emailEntity -> emailEntity.getEmail().equals(EXISTING_EMAIL));
	}

	@Test
	public void findOneDoesNotCompleteAndThrowsResourceOperationFailureWhenNoCustomerFound() {
		when(customerRepository.getCustomer(USER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(CUSTOMER_WAS_NOT_FOUND)));
		emailEntityRepository.findOne(createEmailIdentifier(EXISTING_EMAIL))
				.test()
				.assertNotComplete()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void findOneDoesNotCompleteAndThrowsResourceOperationFailureWhenNoEmailFound() {
		when(customer.getEmail()).thenReturn(null);
		emailEntityRepository.findOne(createEmailIdentifier(EXISTING_EMAIL))
				.test()
				.assertNotComplete()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void findAllProducesCorrectEmailIdentifier() {
		emailEntityRepository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoErrors()
				.assertValue(createEmailIdentifier(EXISTING_EMAIL));
	}

	@Test
	public void findAllDoesNotReturnWhenEmailDoesNotExist() {
		when(customer.getEmail()).thenReturn(null);
		emailEntityRepository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void findAllDoesNotCompleteAndFailsWhenCustomerNotFound() {
		when(customerRepository.getCustomer(USER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(CUSTOMER_WAS_NOT_FOUND)));
		emailEntityRepository.findAll(StringIdentifier.of(SCOPE))
				.test()
				.assertNotComplete()
				.assertError(ResourceOperationFailure.class);
	}

	@Test
	public void creatingValidEmailSucceedsAndReturnsCorrectEmailIdentifier() {
		emailEntityRepository.submit(createEmailEntity(NEW_EMAIL), StringIdentifier.of(SCOPE))
				.test()
				.assertNoErrors()
				.assertValue(createSubmitResult(NEW_EMAIL));
	}

	@Test
	public void creatingValidEmailSetsEmailOnlyWhenUserIdUnlinked() {
		when(customer.getEmail()).thenReturn(EXISTING_EMAIL);
		when(customer.getUserId()).thenReturn(USER_ID);
		emailEntityRepository.submit(createEmailEntity(NEW_EMAIL), StringIdentifier.of(SCOPE))
				.test()
				.assertNoErrors();
		verify(customer).setEmail(NEW_EMAIL);
		verify(customer, never()).setUserId(any());
	}

	@Test
	public void creatingValidEmailSetsEmailAndUserIdWhenUserIdLinked() {
		when(customer.getEmail()).thenReturn(EXISTING_EMAIL);
		when(customer.getUserId()).thenReturn(EXISTING_EMAIL);
		emailEntityRepository.submit(createEmailEntity(NEW_EMAIL), StringIdentifier.of(SCOPE))
				.test()
				.assertNoErrors();
		verify(customer).setEmail(NEW_EMAIL);
		verify(customer).setUserId(NEW_EMAIL);
	}

	@Test
	public void createDoesNotCompleteAndFailsWhenCustomerNotFound() {
		when(customerRepository.getCustomer(USER_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(CUSTOMER_WAS_NOT_FOUND)));
		emailEntityRepository.submit(createEmailEntity(NEW_EMAIL), StringIdentifier.of(SCOPE))
				.test()
				.assertNotComplete()
				.assertError(ResourceOperationFailure.class);
	}

	private SubmitResult<EmailIdentifier> createSubmitResult(final String email) {
		return SubmitResult.<EmailIdentifier>builder()
				.withIdentifier(createEmailIdentifier(email))
				.withStatus(SubmitStatus.CREATED)
				.build();
	}

	private EmailIdentifier createEmailIdentifier(final String email) {
		return EmailIdentifier.builder()
				.withEmailId(StringIdentifier.of(email))
				.withEmails(EmailsIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build())
				.build();
	}

	private EmailEntity createEmailEntity(final String email) {
		return EmailEntity.builder()
				.withEmailId(USER_GUID)
				.withEmail(email)
				.build();
	}
}
