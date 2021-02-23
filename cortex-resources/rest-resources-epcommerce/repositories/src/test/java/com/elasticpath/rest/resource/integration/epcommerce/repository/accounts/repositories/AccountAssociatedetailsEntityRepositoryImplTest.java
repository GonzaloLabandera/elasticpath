/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.accounts.repositories;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatedetailsEntity;
import com.elasticpath.rest.definition.accounts.AssociatedetailsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.service.customer.CustomerService;

@RunWith(MockitoJUnitRunner.class)
public class AccountAssociatedetailsEntityRepositoryImplTest {

	private static final String ASSOCIATE_GUID = "ASSOCIATE_GUID";

	@Mock
	private CustomerService customerService;

	@Mock
	private Customer customer;

	@Mock
	private AssociatedetailsIdentifier associatedetailsIdentifier;

	@Mock
	private AssociateIdentifier associateIdentifier;

	@InjectMocks
	private AccountAssociatedetailsEntityRepositoryImpl accountAssociatedetailsEntityRepositoryImpl;

	@Test
	public void findOne() {
		when(associatedetailsIdentifier.getAssociate()).thenReturn(associateIdentifier);
		when(associateIdentifier.getAssociateId()).thenReturn(StringIdentifier.of(ASSOCIATE_GUID));
		when(customerService.findByGuid(ASSOCIATE_GUID)).thenReturn(customer);
		when(customer.getEmail()).thenReturn("email");
		when(customer.getFirstName()).thenReturn("firstName");
		when(customer.getFirstName()).thenReturn("lastName");

		AssociatedetailsEntity associatedetailsEntity = AssociatedetailsEntity.builder()
				.withEmail(customer.getEmail())
				.withFirstName(customer.getFirstName())
				.withLastName(customer.getLastName())
				.build();

		accountAssociatedetailsEntityRepositoryImpl.findOne(associatedetailsIdentifier)
				.test()
				.assertValue(associatedetailsEntity);
	}
}