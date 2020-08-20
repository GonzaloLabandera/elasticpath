/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.SharedAccountIdIdentifier;
import com.elasticpath.rest.definition.accounts.SharedIdEntity;
import com.elasticpath.service.customer.CustomerService;

/**
 * Shared Id Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SharedIdEntityRepositoryImpl<E extends SharedIdEntity, I extends SharedAccountIdIdentifier>
		implements Repository<SharedIdEntity, SharedAccountIdIdentifier> {

	private CustomerService customerService;

	@Override
	public Single<SharedIdEntity> findOne(final SharedAccountIdIdentifier identifier) {  /* replace with customer.getUserid */
		return Single.just(SharedIdEntity.builder()
				.withSharedId(customerService.findByGuid(identifier.getAccount().getAccountId().getValue()).getUserId())
				.build());
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}
