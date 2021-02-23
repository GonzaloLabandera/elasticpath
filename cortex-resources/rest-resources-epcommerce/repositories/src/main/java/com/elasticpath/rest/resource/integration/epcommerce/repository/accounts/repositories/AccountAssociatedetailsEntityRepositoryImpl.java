/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociatedetailsEntity;
import com.elasticpath.rest.definition.accounts.AssociatedetailsIdentifier;
import com.elasticpath.service.customer.CustomerService;

/**
 * Account Associate details entity repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountAssociatedetailsEntityRepositoryImpl<E extends AssociateEntity, I extends AssociatedetailsIdentifier>
		implements Repository<AssociatedetailsEntity, AssociatedetailsIdentifier> {

	private CustomerService customerService;

	@Override
	public Single<AssociatedetailsEntity> findOne(final AssociatedetailsIdentifier identifier) {
		Customer associate = customerService.findByGuid(identifier.getAssociate().getAssociateId().getValue());
		return Single.just(
				AssociatedetailsEntity.builder()
						.withEmail(associate.getEmail())
						.withFirstName(associate.getFirstName())
						.withLastName(associate.getLastName())
						.build());
	}

	@Reference
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}
}
