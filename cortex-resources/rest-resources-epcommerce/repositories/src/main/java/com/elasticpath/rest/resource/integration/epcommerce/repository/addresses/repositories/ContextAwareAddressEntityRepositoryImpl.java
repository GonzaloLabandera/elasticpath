/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.definition.addresses.ContextAwareAddressIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Context Aware Entity Repository. Makes addresses search for account if it presents, for user in other case.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component(property = "name=contextAwareAddressEntityRepositoryImpl")
public class ContextAwareAddressEntityRepositoryImpl<E extends AddressEntity, I extends ContextAwareAddressIdentifier>
		implements Repository<AddressEntity, ContextAwareAddressIdentifier> {

	private CustomerRepository customerRepository;

	private ResourceOperationContext resourceOperationContext;

	private AddressRepository addressRepository;

	@Override
	public Single<AddressEntity> findOne(final ContextAwareAddressIdentifier identifier) {
		final String addressGuid = identifier.getAddressId().getValue();
		return addressRepository.getAddressEntity(addressGuid, customerRepository.getCustomerGuid(resourceOperationContext.getUserIdentifier(),
				resourceOperationContext.getSubject()));
	}

	@Override
	public Observable<ContextAwareAddressIdentifier> findAll(final IdentifierPart<String> scope) {
		return addressRepository.findAllAddresses(customerRepository.getCustomerGuid(resourceOperationContext.getUserIdentifier(),
				resourceOperationContext.getSubject()))
				.map(customerAddress -> buildAddressIdentifier(scope.getValue(), customerAddress));
	}


	/**
	 * Builds the ContextAwareAddressIdentifier given the scope and address.
	 *
	 * @param scope           scope
	 * @param customerAddress customerAddress
	 * @return the ContextAwareAddressIdentifier
	 */
	protected ContextAwareAddressIdentifier buildAddressIdentifier(final String scope, final CustomerAddress customerAddress) {
		return new ContextAwareAddressIdentifier(AddressIdentifier.builder()
				.withAddressId(StringIdentifier.of(customerAddress.getGuid()))
				.withAddresses(AddressesIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.build())
				.build());
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setAddressRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}

}
