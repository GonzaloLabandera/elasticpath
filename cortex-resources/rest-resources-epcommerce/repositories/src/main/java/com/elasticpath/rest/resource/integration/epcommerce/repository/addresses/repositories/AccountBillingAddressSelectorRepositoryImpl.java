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

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AccountAddressIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.AccountBillingAddressSelectorIdentifier;
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
 * Implement a selector repository for the Account-Defaults Account-Billing-Address Selector.
 *
 * @param <SI> The selector identifier
 * @param <CI> The Account-Billing-Address Selector Choice Identifier
 */
@Component
public class AccountBillingAddressSelectorRepositoryImpl<
		SI extends AccountBillingAddressSelectorIdentifier,
		CI extends AccountBillingAddressSelectorChoiceIdentifier>
		implements SelectorRepository<AccountBillingAddressSelectorIdentifier, AccountBillingAddressSelectorChoiceIdentifier> {

	private Repository<AddressEntity, AccountAddressIdentifier> accountAddressEntityRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private CartOrderRepository cartOrderRepository;
	private AddressRepository addressRepository;

	@Override
	public Single<Choice> getChoice(final AccountBillingAddressSelectorChoiceIdentifier selectorChoiceId) {
		AccountAddressIdentifier accountAddressIdentifier = selectorChoiceId.getAccountAddress();
		String accountAddressId = accountAddressIdentifier.getAccountAddressId().getValue();

		return getSelectedAddress()
				.map(selectedAddress -> buildChoice(selectorChoiceId, getChoiceStatus(accountAddressId, selectedAddress.getGuid())))
				.switchIfEmpty(Single.just(buildChoice(selectorChoiceId, ChoiceStatus.CHOOSABLE)));
	}

	@Override
	public Observable<SelectorChoice> getChoices(final AccountBillingAddressSelectorIdentifier selectorIdentifier) {
		IdentifierPart<String> scope = selectorIdentifier.getAccountBillingAddresses().getAccountAddresses().getAddresses().getScope();

		return getSelectedAddress()
				.flatMapObservable(selectedAddress -> getChoicesWithSelectedAddress(selectorIdentifier, scope, selectedAddress.getGuid()))
				.switchIfEmpty(getChoicesWithSelectedAddress(selectorIdentifier, scope, ""));
	}

	@Override
	public Single<SelectResult<AccountBillingAddressSelectorIdentifier>> selectChoice(final AccountBillingAddressSelectorChoiceIdentifier
																							  accountBillingAddressSelectorChoiceIdentifier) {
		String addressId = accountBillingAddressSelectorChoiceIdentifier.getAccountAddress().getAccountAddressId().getValue();
		return getAccountCustomer()
				.flatMap(account -> setBillingAddress(account, addressId))
				.map(selectStatus -> SelectResult.<AccountBillingAddressSelectorIdentifier>builder()
						.withIdentifier(accountBillingAddressSelectorChoiceIdentifier.getAccountBillingAddressSelector())
						.withStatus(selectStatus)
						.build());
	}

	/**
	 * Set the order's billing address.
	 *
	 * @param account   account customer
	 * @param addressId addressId
	 * @return CartOrder
	 */
	protected Single<SelectStatus> setBillingAddress(final Customer account, final String addressId) {
		if (account.getPreferredBillingAddress() != null && addressId.equals(account.getPreferredBillingAddress().getGuid())) {
			return Single.just(SelectStatus.EXISTING);
		} else {
			return updateAccountPreferredAddress(account, addressId)
					.flatMapCompletable(this::updateAllCartOrdersBillingAddresses)
					.andThen(Single.just(SelectStatus.SELECTED));
		}
	}

	/**
	 * Get the currently selected address.
	 *
	 * @return currently selected address
	 */
	protected Maybe<Address> getSelectedAddress() {
		return getAccountCustomer().flatMapMaybe(account -> Maybe.fromCallable(account::getPreferredBillingAddress));
	}

	/**
	 * Build the choice.
	 *
	 * @param accountBillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier
	 * @param choiceStatus                                  choiceStatus
	 * @return the choice
	 */
	protected Choice buildChoice(
			final AccountBillingAddressSelectorChoiceIdentifier accountBillingAddressSelectorChoiceIdentifier,
			final ChoiceStatus choiceStatus) {
		return Choice.builder()
				.withDescription(accountBillingAddressSelectorChoiceIdentifier.getAccountAddress())
				.withAction(accountBillingAddressSelectorChoiceIdentifier)
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the selector choice.
	 *
	 * @param accountBillingAddressSelectorIdentifier billingAddressSelectorIdentifier
	 * @param accountAddressIdentifier                addressIdentifier
	 * @param choiceStatus                            choiceStatus
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(
			final AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier,
			final AccountAddressIdentifier accountAddressIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildAccountBillingAddressChoiceIdentifier(accountBillingAddressSelectorIdentifier, accountAddressIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the billing address choice identifier.
	 *
	 * @param accountBillingAddressSelectorIdentifier billingAddressSelectorIdentifier
	 * @param accountAddressIdentifier                addressIdentifier
	 * @return the billing address choice identifier
	 */
	protected AccountBillingAddressSelectorChoiceIdentifier buildAccountBillingAddressChoiceIdentifier(
			final AccountBillingAddressSelectorIdentifier accountBillingAddressSelectorIdentifier,
			final AccountAddressIdentifier accountAddressIdentifier) {
		return AccountBillingAddressSelectorChoiceIdentifier.builder()
				.withAccountAddress(accountAddressIdentifier)
				.withAccountBillingAddressSelector(accountBillingAddressSelectorIdentifier)
				.build();
	}

	private Observable<SelectorChoice> getChoicesWithSelectedAddress(final AccountBillingAddressSelectorIdentifier
																			 accountBillingAddressSelectorIdentifier,
																	 final IdentifierPart<String> scope, final String selectedAddress) {
		return accountAddressEntityRepository.findAll(scope)
				.map(accountAddressIdentifier -> buildSelectorChoice(accountBillingAddressSelectorIdentifier, accountAddressIdentifier,
						getChoiceStatus(accountAddressIdentifier.getAccountAddressId().getValue(), selectedAddress)));
	}

	private ChoiceStatus getChoiceStatus(final String addressId, final String selectedAddress) {
		return selectedAddress.equals(addressId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	private Single<Customer> updateAccountPreferredAddress(final Customer account, final String addressGuid) {
		final CustomerAddress customerAddress = account.getAddressByGuid(addressGuid);
		account.setPreferredBillingAddress(customerAddress);
		return customerRepository.update(account);
	}

	private Single<Customer> getAccountCustomer() {
		String accountGuid = getAccountId().getValue();
		return customerRepository.getCustomer(accountGuid);
	}

	private IdentifierPart<String> getAccountId() {
		return addressRepository.getAccountAddressesIdentifier(resourceOperationContext).getAccountId();
	}

	private Completable updateAllCartOrdersBillingAddresses(final Customer account) {
		return cartOrderRepository.findCartOrderGuidsByAccount(account.getStoreCode(), account.getGuid())
				.flatMapSingle(cartOrderGuid -> cartOrderRepository.findByGuid(account.getStoreCode(), cartOrderGuid))
				.flatMapCompletable(cartOrder -> updateCartOrderBillingAddress(account.getPreferredBillingAddress(), cartOrder));
	}

	private Completable updateCartOrderBillingAddress(final CustomerAddress address, final CartOrder cartOrder) {
		cartOrder.setBillingAddressGuid(address.getGuid());
		return cartOrderRepository.saveCartOrder(cartOrder).ignoreElement();
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
	public void setAddressRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}
}

