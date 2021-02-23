/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Address Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component(property = "name=addressEntityRepositoryImpl")
@SuppressWarnings("PMD.GodClass")
public class AddressEntityRepositoryImpl<E extends AddressEntity, I extends AddressIdentifier>
		implements Repository<AddressEntity, AddressIdentifier> {

	/**
	 * Error message when address cannot be found.
	 */
	@VisibleForTesting
	public static final String ADDRESS_NOT_FOUND = "Address not found.";

	private CustomerRepository customerRepository;

	private ResourceOperationContext resourceOperationContext;

	private AddressRepository addressRepository;

	@Override
	public Single<SubmitResult<AddressIdentifier>> submit(final AddressEntity addressEntity, final IdentifierPart<String> scope) {
		return addressRepository.validateAddressEntity(addressEntity).andThen(createNewAddress(addressEntity, scope));
	}

	@Override
	public Single<AddressEntity> findOne(final AddressIdentifier identifier) {
		String addressGuid = identifier.getAddressId().getValue();
		return addressRepository.getAddressEntity(addressGuid, resourceOperationContext.getUserIdentifier());
	}

	@Override
	public Observable<AddressIdentifier> findAll(final IdentifierPart<String> scope) {
		return addressRepository.findAllAddresses(resourceOperationContext.getUserIdentifier())
				.map(customerAddress -> buildAddressIdentifier(scope.getValue(), customerAddress));
	}

	@Override
	public Completable update(final AddressEntity addressEntity, final AddressIdentifier identifier) {
		String addressGuid = identifier.getAddressId().getValue();
		return addressRepository.update(resourceOperationContext.getUserIdentifier(), addressGuid, addressEntity);
	}

	@Override
	public Completable delete(final AddressIdentifier identifier) {
		String addressGuid = identifier.getAddressId().getValue();
		String customerId = resourceOperationContext.getUserIdentifier();
		return addressRepository.deleteAddress(addressGuid, customerId);
	}

	/**
	 * Create a new address from address entity and scope.
	 *
	 * @param addressEntity address entity.
	 * @param scope         scope.
	 * @return Address Identifier
	 */
	protected Single<SubmitResult<AddressIdentifier>> createNewAddress(final AddressEntity addressEntity, final IdentifierPart<String> scope) {
		String userIdentifier = resourceOperationContext.getUserIdentifier();
		Customer customer = customerRepository.getCustomer(userIdentifier).blockingGet();
		CustomerAddress customerAddress = addressRepository.convertAddressEntityToCustomerAddress(addressEntity);

		Optional<CustomerAddress> existingAddressOptional = addressRepository.getExistingAddressMatchingAddress(customerAddress,
				customer);

		if (existingAddressOptional.isPresent()) {
			return Single.just(buildAddressIdentifier(scope.getValue(), existingAddressOptional.get(), false));
		}

		return addressRepository.addAddress(customerAddress, customer, scope.getValue()).map(address -> buildAddressIdentifier(scope.getValue(),
				address, true));
	}

	/**
	 * Builds the AddressIdentifier given the scope and address.
	 *
	 * @param scope           scope
	 * @param customerAddress customerAddress
	 * @return the AddressIdentifier
	 */
	protected AddressIdentifier buildAddressIdentifier(final String scope, final CustomerAddress customerAddress) {
		return AddressIdentifier.builder()
				.withAddressId(StringIdentifier.of(customerAddress.getGuid()))
				.withAddresses(AddressesIdentifier.builder()
						.withScope(StringIdentifier.of(scope))
						.build())
				.build();
	}

	/**
	 * Builds the AddressIdentifier given the scope and address.
	 *
	 * @param scope           scope
	 * @param customerAddress customerAddress
	 * @param isNewlyCreated  true if address was just created
	 * @return submit result of address identifier
	 */
	protected SubmitResult<AddressIdentifier> buildAddressIdentifier(
			final String scope, final CustomerAddress customerAddress, final boolean isNewlyCreated) {

		AddressIdentifier identifier = buildAddressIdentifier(scope, customerAddress);

		return SubmitResult.<AddressIdentifier>builder()
				.withIdentifier(identifier)
				.withStatus(isNewlyCreated
						? SubmitStatus.CREATED
						: SubmitStatus.EXISTING)
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

	@Reference
	public void setAddressRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}

}
