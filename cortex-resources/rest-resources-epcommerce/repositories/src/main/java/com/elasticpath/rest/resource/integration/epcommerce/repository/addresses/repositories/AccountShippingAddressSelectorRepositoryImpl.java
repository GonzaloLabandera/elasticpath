/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.repositories;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountShippingAddressSelectorIdentifier;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement a selector repository for the Account-Defaults Shipping-Address Selector.
 *
 * @param <SI> The selector identifier
 * @param <CI> The Account-Shipping-Address Selector Choice Identifier
 */
@Component
public class AccountShippingAddressSelectorRepositoryImpl<
		SI extends AccountShippingAddressSelectorIdentifier,
		CI extends AccountShippingAddressSelectorChoiceIdentifier>
		implements SelectorRepository<AccountShippingAddressSelectorIdentifier, AccountShippingAddressSelectorChoiceIdentifier> {

	private Repository<AddressEntity, AccountAddressIdentifier> accountAddressEntityRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private CartOrderRepository cartOrderRepository;
	private AddressRepository addressRepository;

	@Override
	public Single<Choice> getChoice(final AccountShippingAddressSelectorChoiceIdentifier selectorChoiceId) {
		AccountAddressIdentifier accountAddressIdentifier = selectorChoiceId.getAccountAddress();
		String addressId = accountAddressIdentifier.getAccountAddressId().getValue();

		return getSelectedAddress()
				.map(selectedAddress -> buildChoice(selectorChoiceId, getChoiceStatus(addressId, selectedAddress.getGuid())))
				.switchIfEmpty(Single.just(buildChoice(selectorChoiceId, ChoiceStatus.CHOOSABLE)));
	}

	@Override
	public Observable<SelectorChoice> getChoices(final AccountShippingAddressSelectorIdentifier selectorIdentifier) {
		IdentifierPart<String> scope = selectorIdentifier.getAccountShippingAddresses().getAccountAddresses().getAddresses().getScope();

		return getSelectedAddress()
				.flatMapObservable(selectedAddress -> getChoicesWithSelectedAddress(selectorIdentifier, scope, selectedAddress.getGuid()))
				.switchIfEmpty(getChoicesWithSelectedAddress(selectorIdentifier, scope, ""));
	}

	@Override
	public Single<SelectResult<AccountShippingAddressSelectorIdentifier>> selectChoice(final AccountShippingAddressSelectorChoiceIdentifier
																							   accountShippingAddressSelectorChoiceIdentifier) {
		String addressId = accountShippingAddressSelectorChoiceIdentifier.getAccountAddress().getAccountAddressId().getValue();
		return getAccountCustomerFromResourceOperationContext()
				.flatMap(account -> setAccountShippingAddress(account, addressId))
				.map(selectStatus -> SelectResult.<AccountShippingAddressSelectorIdentifier>builder()
						.withIdentifier(accountShippingAddressSelectorChoiceIdentifier.getAccountShippingAddressSelector())
						.withStatus(selectStatus)
						.build());
	}

	/**
	 * Set the order's shipping address.
	 *
	 * @param account          customer
	 * @param accountAddressId addressId
	 * @return CartOrder
	 */
	protected Single<SelectStatus> setAccountShippingAddress(final Customer account, final String accountAddressId) {
		if (account.getPreferredShippingAddress() != null && accountAddressId.equals(account.getPreferredShippingAddress().getGuid())) {
			return Single.just(SelectStatus.EXISTING);
		} else {
			return updateAccountPreferredAddress(account, accountAddressId)
					.flatMapCompletable(this::updateAllCartOrdersShippingAddresses)
					.andThen(Single.just(SelectStatus.SELECTED));
		}
	}

	/**
	 * Get the currently selected address.
	 *
	 * @return currently selected address
	 */
	protected Maybe<Address> getSelectedAddress() {
		return getAccountCustomerFromResourceOperationContext().flatMapMaybe(account -> Maybe.fromCallable(account::getPreferredShippingAddress));
	}

	/**
	 * Build the choice.
	 *
	 * @param accountShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier
	 * @param choiceStatus                                   choiceStatus
	 * @return the choice
	 */
	protected Choice buildChoice(
			final AccountShippingAddressSelectorChoiceIdentifier accountShippingAddressSelectorChoiceIdentifier,
			final ChoiceStatus choiceStatus) {
		return Choice.builder()
				.withDescription(accountShippingAddressSelectorChoiceIdentifier.getAccountAddress())
				.withAction(accountShippingAddressSelectorChoiceIdentifier)
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the selector choice.
	 *
	 * @param accountShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier
	 * @param accountAddressIdentifier                 addressIdentifier
	 * @param choiceStatus                             choiceStatus
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(
			final AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier,
			final AccountAddressIdentifier accountAddressIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildAccountShippingAddressChoiceIdentifier(accountShippingAddressSelectorIdentifier, accountAddressIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the shipping address choice identifier.
	 *
	 * @param accountShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier
	 * @param accountAddressIdentifier                 addressIdentifier
	 * @return the shipping address choice identifier
	 */
	protected AccountShippingAddressSelectorChoiceIdentifier buildAccountShippingAddressChoiceIdentifier(
			final AccountShippingAddressSelectorIdentifier accountShippingAddressSelectorIdentifier,
			final AccountAddressIdentifier accountAddressIdentifier) {
		return AccountShippingAddressSelectorChoiceIdentifier.builder()
				.withAccountAddress(accountAddressIdentifier)
				.withAccountShippingAddressSelector(accountShippingAddressSelectorIdentifier)
				.build();
	}

	private Observable<SelectorChoice> getChoicesWithSelectedAddress(final AccountShippingAddressSelectorIdentifier
																			 accountShippingAddressSelectorIdentifier,
																	 final IdentifierPart<String> scope,
																	 final String selectedAddress) {
		return accountAddressEntityRepository.findAll(scope)
				.map(accountAddressIdentifier -> buildSelectorChoice(accountShippingAddressSelectorIdentifier, accountAddressIdentifier,
						getChoiceStatus(accountAddressIdentifier.getAccountAddressId().getValue(), selectedAddress)));
	}

	private ChoiceStatus getChoiceStatus(final String addressId, final String selectedAddress) {
		return selectedAddress.equals(addressId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	private Single<Customer> updateAccountPreferredAddress(final Customer account, final String addressGuid) {
		final CustomerAddress customerAddress = addressRepository.getExistingAddressByGuid(addressGuid, account).blockingGet();
		account.setPreferredShippingAddress(customerAddress);
		return customerRepository.update(account);
	}

	private Single<Customer> getAccountCustomerFromResourceOperationContext() {
		String accountGuid = getAccountId().getValue();
		return customerRepository.getCustomer(accountGuid);
	}

	private IdentifierPart<String> getAccountId() {
		return addressRepository.getAccountAddressesIdentifier(resourceOperationContext).getAccountId();
	}

	private Completable updateAllCartOrdersShippingAddresses(final Customer account) {
		final String storeCode = account.getStoreCode();
		return cartOrderRepository.findCartOrderGuidsByAccount(account.getStoreCode(), account.getGuid())
				.flatMapCompletable(cartOrderGuid ->
						cartOrderRepository.updateShippingAddressOnCartOrder(
								account.getPreferredShippingAddress().getGuid(), cartOrderGuid, storeCode).ignoreElement());
	}

	@Reference(target = "(name=accountAddressEntityRepositoryImpl)")
	public void setAccountAddressEntityRepository(
			final Repository<AddressEntity, AccountAddressIdentifier> accountAddressEntityRepository) {
		this.accountAddressEntityRepository = accountAddressEntityRepository;
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
	public void setCartOrderRepository(final CartOrderRepository cartOrderRepository) {
		this.cartOrderRepository = cartOrderRepository;
	}

	@Reference
	public void setAccountRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}
}
