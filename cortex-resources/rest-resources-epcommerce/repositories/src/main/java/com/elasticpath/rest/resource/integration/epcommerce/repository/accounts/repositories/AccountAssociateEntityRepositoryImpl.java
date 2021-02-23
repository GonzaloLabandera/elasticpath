/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatedetailsIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Account Associate Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountAssociateEntityRepositoryImpl<E extends AssociateEntity, I extends AssociateIdentifier>
		implements Repository<AssociateEntity, AssociateIdentifier> {

	private UserAccountAssociationService userAccountAssociationService;

	private ReactiveAdapter reactiveAdapter;

	private ResourceOperationContext resourceOperationContext;

	private AccountAssociateValidator accountAssociateValidator;

	@Override
	public Single<AssociateEntity> findOne(final AssociateIdentifier associateIdentifier) {
		UserAccountAssociation userAccountAssociation = getUserAccountAssociationForUserAndAccount(associateIdentifier);

		return Single.just(
				AssociateEntity.builder()
						.withRole(userAccountAssociation.getAccountRole())
						.build());
	}

	@Override
	public Observable<AssociateIdentifier> findAll(final IdentifierPart<String> scope) {
		Optional<AccountIdentifier> accountIdentifier = getAccountIdentifier();
		if (accountIdentifier.isPresent()) {
			return Observable
					.fromIterable(userAccountAssociationService.findAssociationsForAccount(accountIdentifier.get().getAccountId().getValue())
							.stream()
							.map(this::getAssociateIdentifier)
							.filter(Optional::isPresent)
							.map(Optional::get)
							.collect(Collectors.toList()));
		}
		return Observable.empty();
	}

	private Optional<AccountIdentifier> getAccountIdentifier() {
		Optional<ResourceIdentifier> resourceIdentifierOptional = resourceOperationContext.getResourceIdentifier();
		if (resourceIdentifierOptional.isPresent()) {
			ResourceIdentifier resourceIdentifier = resourceIdentifierOptional.get();
			AccountIdentifier accountIdentifier = null;
			if (resourceIdentifier instanceof AccountIdentifier) {
				accountIdentifier = (AccountIdentifier) resourceIdentifier;
			} else if (resourceIdentifier instanceof AssociatesIdentifier) {
				accountIdentifier = ((AssociatesIdentifier) resourceIdentifier).getAccount();
			} else if (resourceIdentifier instanceof AssociateIdentifier) {
				accountIdentifier = ((AssociateIdentifier) resourceIdentifier).getAssociates().getAccount();
			} else if (resourceIdentifier instanceof AssociatedetailsIdentifier) {
				accountIdentifier = ((AssociatedetailsIdentifier) resourceIdentifier).getAssociate().getAssociates().getAccount();
			} else if (resourceIdentifier instanceof AddAssociateFormIdentifier) {
				accountIdentifier = ((AddAssociateFormIdentifier) resourceIdentifier).getAssociates().getAccount();
			}
			return Optional.ofNullable(accountIdentifier);
		}

		return Optional.empty();
	}

	private Optional<AssociateIdentifier> getAssociateIdentifier(final UserAccountAssociation associate) {
		Optional<AccountIdentifier> accountIdentifier = getAccountIdentifier();
		if (accountIdentifier.isPresent()) {
			AssociatesIdentifier associatesIdentifier = AssociatesIdentifier.builder()
					.withAccount(accountIdentifier.get())
					.build();

			return Optional.of(AssociateIdentifier.builder()
					.withAssociateId(StringIdentifier.of(associate.getUserGuid()))
					.withAssociates(associatesIdentifier)
					.build());
		}
		return Optional.empty();
	}

	@Override
	public Completable delete(final AssociateIdentifier associateIdentifier) {
		String currentUserId = resourceOperationContext.getUserIdentifier();
		if (associateIdentifier.getAssociateId().getValue().equals(currentUserId)) {
			return Completable.error(ResourceOperationFailure.badRequestBody("User cannot delete their own association."));
		}

		return reactiveAdapter.fromServiceAsCompletable(
				() -> userAccountAssociationService.remove(
						associateIdentifier.getAssociateId().getValue(),
						associateIdentifier.getAssociates().getAccount().getAccountId().getValue()));
	}

	private UserAccountAssociation getUserAccountAssociationForUserAndAccount(final AssociateIdentifier identifier) {
		return userAccountAssociationService.findAssociationForUserAndAccount(
				identifier.getAssociateId().getValue(),
				identifier.getAssociates().getAccount().getAccountId().getValue());
	}

	@Override
	public Completable update(final AssociateEntity associateEntity, final AssociateIdentifier associateIdentifier) {
		String currentUserId = resourceOperationContext.getUserIdentifier();
		String role = associateEntity.getRole();
		String associateId = associateIdentifier.getAssociateId().getValue();
		return accountAssociateValidator.validateUserRoleUpdate(associateEntity.getRole(), associateId, currentUserId)
				.andThen(reactiveAdapter.fromServiceAsCompletable(() -> userAccountAssociationService.update(
										associateId,
										associateIdentifier.getAssociates().getAccount().getAccountId().getValue(),
										role)));
	}

	@Reference
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setAccountAssociateValidator(final AccountAssociateValidator accountAssociateValidator) {
		this.accountAssociateValidator = accountAssociateValidator;
	}
}
