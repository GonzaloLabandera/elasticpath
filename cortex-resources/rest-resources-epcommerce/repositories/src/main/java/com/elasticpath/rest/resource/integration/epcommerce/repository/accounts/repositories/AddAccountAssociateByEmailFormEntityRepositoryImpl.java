/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.List;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.repository.CreatorRepository;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Repository for adding associates by email to account.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AddAccountAssociateByEmailFormEntityRepositoryImpl<E extends AddAssociateFormEntity, I extends AssociateIdentifier>
		implements CreatorRepository<AddAssociateFormEntity, AddAssociateFormIdentifier, AssociateIdentifier> {

	private UserAccountAssociationService userAccountAssociationService;

	private AccountAssociateValidator accountAssociateValidator;

	private CustomerRepository customerRepository;

	@Override
	public Single<SubmitResult<AssociateIdentifier>> submit(
			final AddAssociateFormEntity addAssociateFormEntity, final AddAssociateFormIdentifier addAssociateFormIdentifier) {
		return accountAssociateValidator.validateAddAssociateByEmailFormFilled(addAssociateFormEntity)
				.andThen(findCustomersByProfileAttributeKeyAndValue(addAssociateFormEntity))
				.flatMap(customer -> addAssociate(addAssociateFormEntity, addAssociateFormIdentifier, customer));
	}

	private Single<SubmitResult<AssociateIdentifier>> addAssociate(final AddAssociateFormEntity addAssociateFormEntity,
			final AddAssociateFormIdentifier addAssociateFormIdentifier, final Customer customer) {

		AccountIdentifier accountIdentifier = addAssociateFormIdentifier.getAssociates().getAccount();
		UserAccountAssociation userAccountAssociation = userAccountAssociationService.findAssociationForUserAndAccount(
				customer.getGuid(),
				accountIdentifier.getAccountId().getValue());

		boolean isNewlyCreated = false;
		if (userAccountAssociation == null) {
			userAccountAssociation = userAccountAssociationService.associateUserToAccount(
					customer.getGuid(),
					accountIdentifier.getAccountId().getValue(),
					addAssociateFormEntity.getRole());

			isNewlyCreated = true;
		}

		AssociateIdentifier associateIdentifier = getAssociateIdentifier(accountIdentifier, userAccountAssociation);

		return Single.just(
				SubmitResult.<AssociateIdentifier>builder()
						.withIdentifier(associateIdentifier)
						.withStatus(isNewlyCreated
								? SubmitStatus.CREATED
								: SubmitStatus.EXISTING)
						.build());
	}

	private Single<Customer> validateAndRetrieveCustomer(final AddAssociateFormEntity addAssociateFormEntity,
			final ExecutionResult<List<Customer>> customerResult) {
		return accountAssociateValidator.validateAddAssociateByEmailFormData(addAssociateFormEntity, customerResult)
				.toSingle(() -> customerResult.getData().get(0));
	}

	private Single<Customer> findCustomersByProfileAttributeKeyAndValue(final AddAssociateFormEntity addAssociateFormEntity) {
		return Single.just(customerRepository.
				findCustomersByProfileAttributeKeyAndValue(CustomerImpl.ATT_KEY_CP_EMAIL, addAssociateFormEntity.getEmail()))
				.flatMap(customerResult -> validateAndRetrieveCustomer(addAssociateFormEntity, customerResult));
	}

	private AssociateIdentifier getAssociateIdentifier(final AccountIdentifier accountIdentifier,
			final UserAccountAssociation userAccountAssociation) {
		AssociatesIdentifier associatesIdentifier = AssociatesIdentifier.builder()
				.withAccount(accountIdentifier)
				.build();

		return AssociateIdentifier.builder()
				.withAssociateId(StringIdentifier.of(userAccountAssociation.getUserGuid()))
				.withAssociates(associatesIdentifier)
				.build();
	}

	@Reference
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	@Reference
	public void setAddAccountAssociateByEmailFormValidator(
			final AccountAssociateValidator accountAssociateValidator) {
		this.accountAssociateValidator = accountAssociateValidator;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}
}
