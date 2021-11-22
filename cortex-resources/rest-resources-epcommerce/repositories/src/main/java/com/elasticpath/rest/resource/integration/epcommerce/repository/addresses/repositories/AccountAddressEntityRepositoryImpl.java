/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Account Address Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@SuppressWarnings("deprecation")
@Component(property = "name=accountAddressEntityRepositoryImpl")
public class AccountAddressEntityRepositoryImpl<E extends AddressEntity, I extends AccountAddressIdentifier>
		implements Repository<AddressEntity, AccountAddressIdentifier> {

	private ResourceOperationContext resourceOperationContext;

	private AddressRepository addressRepository;

	private CustomerRepository customerRepository;

	@Override
	public Single<AddressEntity> findOne(final AccountAddressIdentifier identifier) {
		String addressGuid = identifier.getAccountAddressId().getValue();
		String accountGuid = getAccountId().getValue();
		return addressRepository.getAddressEntity(addressGuid, accountGuid);
	}

	@Override
	public Observable<AccountAddressIdentifier> findAll(final IdentifierPart<String> scope) {
		IdentifierPart<String> accountId = getAccountId();
		if (accountId == null) {
			return Observable.empty();
		}
		return addressRepository.findAllAddresses(accountId.getValue())
				.map(this::buildAccountAddressIdentifier);
	}

	@Override
	public Completable update(final AddressEntity entity, final AccountAddressIdentifier identifier) {
		String addressGuid = identifier.getAccountAddressId().getValue();
		String accountGuid = getAccountId().getValue();
		return addressRepository.update(accountGuid, addressGuid, entity);
	}

	@Override
	public Single<SubmitResult<AccountAddressIdentifier>> submit(final AddressEntity entity, final IdentifierPart<String> scope) {
		return addressRepository.validateAddressEntity(entity).andThen(createNewAddress(entity, scope));
	}

	@Override
	public Completable delete(final AccountAddressIdentifier identifier) {
		return addressRepository.deleteAddress(identifier.getAccountAddressId().getValue(),
				getAccountId().getValue());
	}

	private Single<SubmitResult<AccountAddressIdentifier>> createNewAddress(final AddressEntity entity,
																			final IdentifierPart<String> scope) {
		String accountGuid = getAccountId().getValue();
		CustomerAddress customerAddress = addressRepository.convertAddressEntityToCustomerAddress(entity);
		Customer accountCustomer = customerRepository.getCustomer(accountGuid).blockingGet();
		customerAddress.setCustomerUidPk(accountCustomer.getUidPk());
		Optional<CustomerAddress> existingAddressOptional = addressRepository.getExistingAddressMatchingAddress(customerAddress,
				accountCustomer);

		if (existingAddressOptional.isPresent()) {
			return Single.just(buildAccountAddressIdentifier(scope.getValue(), existingAddressOptional.get(), false));
		}

		return addressRepository.addAccountAddress(customerAddress, accountCustomer, scope.getValue())
				.map(address -> buildAccountAddressIdentifier(scope.getValue(), address, true));
	}

	/**
	 * Builds the AddressIdentifier given the scope and address.
	 *
	 * @param scope           scope
	 * @param customerAddress customerAddress
	 * @param isNewlyCreated  true if address was just created
	 * @return submit result of address identifier
	 */
	protected SubmitResult<AccountAddressIdentifier> buildAccountAddressIdentifier(
			final String scope, final CustomerAddress customerAddress, final boolean isNewlyCreated) {

		AccountAddressIdentifier identifier = buildAccountAddressIdentifier(scope, customerAddress);

		return SubmitResult.<AccountAddressIdentifier>builder()
				.withIdentifier(identifier)
				.withStatus(isNewlyCreated
						? SubmitStatus.CREATED
						: SubmitStatus.EXISTING)
				.build();
	}

	/**
	 * Builds the AddressIdentifier given the scope and address.
	 *
	 * @param scope           scope
	 * @param customerAddress customerAddress
	 * @return the AddressIdentifier
	 */
	protected AccountAddressIdentifier buildAccountAddressIdentifier(final String scope, final CustomerAddress customerAddress) {
		return AccountAddressIdentifier.builder()
				.withAccountAddressId(StringIdentifier.of(customerAddress.getGuid()))
				.withAccountAddresses(AccountAddressesIdentifier.builder()
						.withAccountId(getAccountId())
						.withAddresses(AddressesIdentifier.builder().withScope(StringIdentifier.of(scope)).build())
						.build())
				.build();
	}

	/**
	 * Build Account Address Identifier.
	 *
	 * @param customerAddress the customer address
	 * @return the account address identifier
	 */
	protected AccountAddressIdentifier buildAccountAddressIdentifier(final CustomerAddress customerAddress) {
		return AccountAddressIdentifier.builder()
				.withAccountAddressId(StringIdentifier.of(customerAddress.getGuid()))
				.withAccountAddresses(addressRepository.getAccountAddressesIdentifier(resourceOperationContext))
				.build();
	}

	private IdentifierPart<String> getAccountId() {
		return addressRepository.getAccountAddressesIdentifier(resourceOperationContext).getAccountId();
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setAddressRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

}
