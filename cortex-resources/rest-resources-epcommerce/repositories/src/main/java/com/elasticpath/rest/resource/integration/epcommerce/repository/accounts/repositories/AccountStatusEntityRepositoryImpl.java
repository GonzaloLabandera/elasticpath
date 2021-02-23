/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountStatusEntity;
import com.elasticpath.rest.definition.accounts.AccountStatusIdentifier;
import com.elasticpath.service.customer.CustomerService;

/**
 * Account Status Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountStatusEntityRepositoryImpl<E extends AccountStatusEntity, I extends AccountStatusIdentifier>
		implements Repository<AccountStatusEntity, AccountStatusIdentifier> {

	private CustomerService customerService;

	@Reference(name = "accountStatusIdToAccountStatusMapHolder")
	private AccountStatusIdToAccountStatusMapHolder accountStatusIdToAccountStatusMapHolder;

	@Override
	public Single<AccountStatusEntity> findOne(final AccountStatusIdentifier identifier) {  /* replace with customer.getUserid */
		return Single.just(AccountStatusEntity.builder()
				.withStatus(
						accountStatusIdToAccountStatusMapHolder.getAccountStatusById(
								customerService.findByGuid(identifier.getAccount().getAccountId().getValue()).getStatus()))
				.build());
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

}
