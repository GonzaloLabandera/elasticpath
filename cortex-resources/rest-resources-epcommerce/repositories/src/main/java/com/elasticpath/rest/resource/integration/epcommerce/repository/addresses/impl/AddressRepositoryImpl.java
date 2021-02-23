/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.impl;

import java.util.Collection;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.rest.definition.addresses.AccountAddressFormIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressesIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.base.NameEntity;
import com.elasticpath.rest.id.ResourceIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.CartOrdersDefaultAddressPopulator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;

/**
 * Implementation of Address repository.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.GodClass"})
@Component
public class AddressRepositoryImpl implements AddressRepository {

	/**
	 * Error message when address cannot be found.
	 */
	@VisibleForTesting
	public static final String ADDRESS_NOT_FOUND = "Address not found.";

	private AddressValidator addressValidator;

	private CustomerRepository customerRepository;

	private ReactiveAdapter reactiveAdapter;

	private CartOrdersDefaultAddressPopulator cartOrdersDefaultAddressPopulator;

	private ConversionService conversionService;

	@Override
	public Completable update(final String customerGuid, final String addressGuid, final AddressEntity addressEntity) {
		return addressValidator.validate(addressEntity)
				.andThen(customerRepository.getCustomer(customerGuid)
						.flatMapCompletable(customer -> getExistingAddressByGuid(addressGuid, customer)
								.map(address -> updateCustomerAddress(addressEntity, address))
								.flatMapCompletable(address -> customerRepository.updateAddress(customer, address))));
	}

	@Override
	public Single<CustomerAddress> getExistingAddressByGuid(final String addressGuid, final Customer customer) {
		return reactiveAdapter.fromNullableAsSingle(() -> customer.getAddressByGuid(addressGuid), ADDRESS_NOT_FOUND);
	}

	@Override
	public Single<AddressEntity> getAddressEntity(final String addressGuid, final String customerGuid) {
		return customerRepository.getCustomer(customerGuid)
				.flatMap(customer -> getExistingAddressByGuid(addressGuid, customer))
				.map(this::convertCustomerAddressToAddressEntity);
	}

	@Override
	public Observable<CustomerAddress> findAllAddresses(final String customerGuid) {
		return customerRepository.getCustomer(customerGuid)
				.flatMapObservable(customer -> Observable.fromIterable(customer.getAddresses()));
	}

	@Override
	public Single<CustomerAddress> addAddress(final CustomerAddress address, final Customer customer, final String scope) {
		return customerRepository.addAddress(customer, address)
				.flatMap(newAddressCustomer -> setCustomerPreferredAddress(scope, newAddressCustomer, address))
				.flatMap(updatedCustomer -> getExistingAddressByGuid(address.getGuid(), updatedCustomer));
	}

	@Override
	public Single<CustomerAddress> addAccountAddress(final CustomerAddress address, final Customer account, final String scope) {
		return customerRepository.addAddress(account, address)
				.flatMap(newAddressCustomer -> setAccountPreferredAddress(scope, newAddressCustomer, address))
				.flatMap(updatedCustomer -> getExistingAddressByGuid(address.getGuid(), updatedCustomer));
	}

	@Override
	public Optional<CustomerAddress> getExistingAddressMatchingAddress(final CustomerAddress customerAddress, final Customer customer) {
		return customer.getAddresses().stream().filter(existingAddress -> existingAddress.equals(customerAddress)).findFirst();
	}

	@Override
	public CustomerAddress convertAddressEntityToCustomerAddress(final AddressEntity addressEntity) {
		return conversionService.convert(addressEntity, CustomerAddress.class);
	}

	@Override
	public Completable validateAddressEntity(final AddressEntity addressEntity) {
		return addressValidator.validate(addressEntity);
	}

	@Override
	public Single<Customer> setCustomerPreferredAddress(final String scope, final Customer customer, final CustomerAddress address) {
		boolean addPreferredBillingAddress = isAddPreferredBillingAddress(customer, address);

		boolean addPreferredShippingAddress = isAddPreferredShippingAddress(customer, address);

		return updateCartOrdersAddresses(scope, customer, address, addPreferredBillingAddress, addPreferredShippingAddress)
				.andThen(customerRepository.update(customer));
	}

	@Override
	public Single<Customer> setAccountPreferredAddress(final String scope, final Customer account, final CustomerAddress address) {
		boolean addPreferredBillingAddress = isAddPreferredBillingAddress(account, address);

		boolean addPreferredShippingAddress = isAddPreferredShippingAddress(account, address);

		return updateAccountCartOrdersAddresses(scope, account, address, addPreferredBillingAddress, addPreferredShippingAddress)
				.andThen(customerRepository.update(account));
	}

	@Override
	public Completable updateCartOrdersAddresses(final String scope, final Customer customer, final CustomerAddress address,
												 final boolean updatePreferredBillingAddress, final boolean updatePreferredShippingAddress) {
		if (updatePreferredBillingAddress || updatePreferredShippingAddress) {
			return cartOrdersDefaultAddressPopulator.updateAllCartOrdersAddresses(customer, address, scope, updatePreferredBillingAddress,
					updatePreferredShippingAddress);
		}

		return Completable.complete();
	}

	@Override
	public Completable updateAccountCartOrdersAddresses(final String scope, final Customer account, final CustomerAddress address,
														final boolean updatePreferredBillingAddress, final boolean updatePreferredShippingAddress) {
		if (updatePreferredBillingAddress || updatePreferredShippingAddress) {
			return cartOrdersDefaultAddressPopulator.updateAccountCartOrdersAddresses(account, address, scope, updatePreferredBillingAddress,
					updatePreferredShippingAddress);
		}

		return Completable.complete();
	}

	@Override
	public AddressEntity convertCustomerAddressToAddressEntity(final Address address) {
		return conversionService.convert(address, AddressEntity.class);
	}

	@Override
	public Completable deleteAddress(final String addressGuid, final String customerId) {
		return customerRepository.getCustomer(customerId)
				.flatMap(customer -> getExistingAddressByGuid(addressGuid, customer)
						.flatMap(address -> removeCustomerAddress(customer, address))).toCompletable();
	}

	@Override
	public AccountAddressesIdentifier getAccountAddressesIdentifier(final ResourceOperationContext context) {
		Optional<ResourceIdentifier> resourceIdentifierOptional = context.getResourceIdentifier();
		if (resourceIdentifierOptional.isPresent()) {
			ResourceIdentifier resourceIdentifier = resourceIdentifierOptional.get();
			return getAccountAddressesIdentifierFromResourceIdentifier(resourceIdentifier);
		}
		throw new UnsupportedOperationException("Resource Identifier not found.");
	}

	@Override
	public Collection<CustomerAddress> getAccountAddresses(final String accountId) {
		return findAllAddresses(accountId).toList().blockingGet();
	}

	@SuppressWarnings({"PMD.NPathComplexity"})
	private AccountAddressesIdentifier getAccountAddressesIdentifierFromResourceIdentifier(final ResourceIdentifier resourceIdentifier) {
		if (resourceIdentifier instanceof AccountAddressesIdentifier) {
			return (AccountAddressesIdentifier) resourceIdentifier;
		}
		if (resourceIdentifier instanceof AccountBillingAddressesIdentifier) {
			AccountBillingAddressesIdentifier accountBillingAddressesIdentifier = (AccountBillingAddressesIdentifier) resourceIdentifier;
			return accountBillingAddressesIdentifier.getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountShippingAddressesIdentifier) {
			AccountShippingAddressesIdentifier accountShippingAddressesIdentifier = (AccountShippingAddressesIdentifier) resourceIdentifier;
			return accountShippingAddressesIdentifier.getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountShippingAddressSelectorIdentifier) {
			AccountShippingAddressSelectorIdentifier accountShippingAddressesIdentifier =
					(AccountShippingAddressSelectorIdentifier) resourceIdentifier;
			return accountShippingAddressesIdentifier.getAccountShippingAddresses().getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountBillingAddressSelectorIdentifier) {
			AccountBillingAddressSelectorIdentifier accountShippingAddressesIdentifier =
					(AccountBillingAddressSelectorIdentifier) resourceIdentifier;
			return accountShippingAddressesIdentifier.getAccountBillingAddresses().getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountAddressIdentifier) {
			return ((AccountAddressIdentifier) resourceIdentifier).getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountAddressFormIdentifier) {
			return ((AccountAddressFormIdentifier) resourceIdentifier).getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountBillingAddressSelectorChoiceIdentifier) {
			return ((AccountBillingAddressSelectorChoiceIdentifier) resourceIdentifier)
					.getAccountBillingAddressSelector()
					.getAccountBillingAddresses()
					.getAccountAddresses();
		}
		if (resourceIdentifier instanceof AccountShippingAddressSelectorChoiceIdentifier) {
			return ((AccountShippingAddressSelectorChoiceIdentifier) resourceIdentifier)
					.getAccountShippingAddressSelector()
					.getAccountShippingAddresses()
					.getAccountAddresses();
		}
		throw new UnsupportedOperationException(resourceIdentifier.getClass().getName() + " is not supported.");
	}

	/**
	 * Removes the address from the customer.
	 *
	 * @param customer customer
	 * @param address  address
	 * @return the customer
	 */
	protected Single<Customer> removeCustomerAddress(final Customer customer, final CustomerAddress address) {
		customer.removeAddress(address);
		return customerRepository.update(customer);
	}

	/**
	 * Updates the customer address from the given address entity.
	 *
	 * @param addressEntity   addressEntity
	 * @param customerAddress address
	 * @return the updated customer address
	 */
	protected CustomerAddress updateCustomerAddress(final AddressEntity addressEntity, final CustomerAddress customerAddress) {
		com.elasticpath.rest.definition.base.AddressEntity address = addressEntity.getAddress();

		updatePhoneNumber(customerAddress, addressEntity.getPhoneNumber());
		updateOrganization(customerAddress, addressEntity.getOrganization());

		if (address != null) {
			updateCountry(customerAddress, address.getCountryName());
			updateState(customerAddress, address.getRegion());
			updateCity(customerAddress, address.getLocality());
			updatePostal(customerAddress, address.getPostalCode());
			updateStreet1(customerAddress, address.getStreetAddress());
			updateStreet2(customerAddress, address.getExtendedAddress());
		}

		NameEntity nameEntity = addressEntity.getName();
		if (nameEntity != null) {
			updateFirstName(customerAddress, nameEntity.getGivenName());
			updateLastName(customerAddress, nameEntity.getFamilyName());
		}

		return customerAddress;
	}

	/**
	 * Update the last name on the address.
	 *
	 * @param address  address
	 * @param lastName lastName
	 */
	protected void updateLastName(final Address address, final String lastName) {
		if (lastName != null) {
			address.setLastName(StringUtils.trimToNull(lastName));
		}
	}

	/**
	 * Update the first name on the address.
	 *
	 * @param address   address
	 * @param firstName firstName
	 */
	protected void updateFirstName(final Address address, final String firstName) {
		if (firstName != null) {
			address.setFirstName(StringUtils.trimToNull(firstName));
		}
	}

	/**
	 * Update the street on the address.
	 *
	 * @param address address
	 * @param street1 street1
	 */
	protected void updateStreet1(final Address address, final String street1) {
		if (street1 != null) {
			address.setStreet1(StringUtils.trimToNull(street1));
		}
	}

	/**
	 * Update the extended street on the address.
	 *
	 * @param address address
	 * @param street2 street2
	 */
	protected void updateStreet2(final Address address, final String street2) {
		if (street2 != null) {
			address.setStreet2(StringUtils.trimToNull(street2));
		}
	}

	/**
	 * Update the postal code on the address.
	 *
	 * @param address         address
	 * @param zipOrPostalCode zipOrPostalCode
	 */
	protected void updatePostal(final Address address, final String zipOrPostalCode) {
		if (zipOrPostalCode != null) {
			address.setZipOrPostalCode(StringUtils.trimToNull(zipOrPostalCode));
		}
	}

	/**
	 * Update the city on the address.
	 *
	 * @param address address
	 * @param city    city
	 */
	protected void updateCity(final Address address, final String city) {
		if (city != null) {
			address.setCity(StringUtils.trimToNull(city));
		}
	}

	/**
	 * Update the state on the address.
	 *
	 * @param address    address
	 * @param subCountry subCountry
	 */
	protected void updateState(final Address address, final String subCountry) {
		if (subCountry != null) {
			address.setSubCountry(StringUtils.trimToNull(subCountry));
		}
	}

	/**
	 * Update the country on the address.
	 *
	 * @param address address
	 * @param country country
	 */
	protected void updateCountry(final Address address, final String country) {
		if (country != null) {
			address.setCountry(StringUtils.trimToNull(country));
		}
	}

	/**
	 * Update the phone number on the address.
	 *
	 * @param address     address
	 * @param phoneNumber phoneNumber
	 */
	protected void updatePhoneNumber(final Address address, final String phoneNumber) {
		if (phoneNumber != null) {
			address.setPhoneNumber(StringUtils.trimToNull(phoneNumber));
		}
	}

	/**
	 * Update the organization on the address.
	 *
	 * @param address      address
	 * @param organization organization
	 */
	protected void updateOrganization(final Address address, final String organization) {
		if (organization != null) {
			address.setOrganization(StringUtils.trimToNull(organization));
		}
	}

	private boolean isAddPreferredBillingAddress(final Customer customer, final CustomerAddress address) {
		boolean addPreferredBillingAddress = customer.getPreferredBillingAddress() == null;
		if (addPreferredBillingAddress) {
			customer.setPreferredBillingAddress(address);
		}
		return addPreferredBillingAddress;
	}

	private boolean isAddPreferredShippingAddress(final Customer customer, final CustomerAddress address) {
		boolean addPreferredShippingAddress = customer.getPreferredShippingAddress() == null;
		if (addPreferredShippingAddress) {
			customer.setPreferredShippingAddress(address);
		}
		return addPreferredShippingAddress;
	}

	@Reference
	public void setAddressValidator(final AddressValidator addressValidator) {
		this.addressValidator = addressValidator;
	}

	@Reference
	public void setCustomerRepository(final CustomerRepository customerRepository) {
		this.customerRepository = customerRepository;
	}

	@Reference
	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Reference
	public void setCartOrdersDefaultAddressPopulator(final CartOrdersDefaultAddressPopulator cartOrdersDefaultAddressPopulator) {
		this.cartOrdersDefaultAddressPopulator = cartOrdersDefaultAddressPopulator;
	}
}
