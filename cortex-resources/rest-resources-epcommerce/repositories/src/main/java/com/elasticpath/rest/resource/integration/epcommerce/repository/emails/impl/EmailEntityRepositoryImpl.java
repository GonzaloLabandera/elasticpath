/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.emails.impl;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.emails.EmailEntity;
import com.elasticpath.rest.definition.emails.EmailIdentifier;
import com.elasticpath.rest.definition.emails.EmailsIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Email Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class EmailEntityRepositoryImpl<E extends EmailEntity, I extends EmailIdentifier>
		implements Repository<EmailEntity, EmailIdentifier> {

	private static final String EMAIL_NOT_FOUND = "Email not found";

	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;

	@Override
	public Single<SubmitResult<EmailIdentifier>> submit(final EmailEntity emailEntity, final IdentifierPart<String> scope) {
		String email = emailEntity.getEmail();

		return customerRepository.getCustomer(resourceOperationContext.getUserIdentifier())
				.flatMapCompletable(customer -> updateCustomerWithEmail(customer, email))
				.toSingle(() -> SubmitResult.<EmailIdentifier>builder()
						.withIdentifier(buildEmailIdentifier(email, scope))
						.withStatus(SubmitStatus.CREATED)
						.build());
	}

	private Completable updateCustomerWithEmail(final Customer customer, final String email) {
		customer.setEmail(email);

		return customerRepository.updateCustomer(customer);
	}

	@Override
	public Single<EmailEntity> findOne(final EmailIdentifier identifier) {
		String emailId = identifier.getEmailId().getValue();

		return customerRepository.getCustomer(resourceOperationContext.getUserIdentifier())
				.flatMapMaybe(customer -> Maybe.fromCallable(customer::getEmail))
				.map(email -> EmailEntity.builder()
						.withEmailId(emailId)
						.withEmail(email)
						.build())
				.switchIfEmpty(Single.error(ResourceOperationFailure.notFound(EMAIL_NOT_FOUND)));
	}

	@Override
	public Observable<EmailIdentifier> findAll(final IdentifierPart<String> scope) {
		return customerRepository.getCustomer(resourceOperationContext.getUserIdentifier())
				.flatMapMaybe(customer -> Maybe.fromCallable(customer::getEmail))
				.map(email -> buildEmailIdentifier(email, scope))
				.toObservable();
	}

	private EmailIdentifier buildEmailIdentifier(final String email, final IdentifierPart<String> scope) {
		return EmailIdentifier.builder()
				.withEmailId(StringIdentifier.of(email))
				.withEmails(EmailsIdentifier.builder()
						.withScope(scope)
						.build())
				.build();
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

}
