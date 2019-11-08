/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
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
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.BillingAddressSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.selector.Choice;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.rest.selector.SelectResult;
import com.elasticpath.rest.selector.SelectStatus;
import com.elasticpath.rest.selector.SelectorChoice;

/**
 * Implement a selector repository for the Profile-Defaults Billing-Address Selector.
 *
 * @param <SI> The selector identifier
 * @param <CI> The Billing-Address Selector Choice Identifier
 */
@Component
public class BillingAddressSelectorRepositoryImpl<
		SI extends BillingAddressSelectorIdentifier,
		CI extends BillingAddressSelectorChoiceIdentifier>
		implements SelectorRepository<BillingAddressSelectorIdentifier, BillingAddressSelectorChoiceIdentifier> {

	private Repository<AddressEntity, AddressIdentifier> addressEntityRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private CartOrderRepository cartOrderRepository;

	@Override
	public Single<Choice> getChoice(final BillingAddressSelectorChoiceIdentifier selectorChoiceId) {
		AddressIdentifier addressIdentifier = selectorChoiceId.getAddress();
		String addressId = addressIdentifier.getAddressId().getValue();

		return getSelectedAddress()
				.map(selectedAddress -> buildChoice(selectorChoiceId, getChoiceStatus(addressId, selectedAddress.getGuid())))
				.switchIfEmpty(Single.just(buildChoice(selectorChoiceId, ChoiceStatus.CHOOSABLE)));
	}

	@Override
	public Observable<SelectorChoice> getChoices(final BillingAddressSelectorIdentifier selectorIdentifier) {
		IdentifierPart<String> scope = selectorIdentifier.getBillingAddresses().getAddresses().getScope();

		return getSelectedAddress()
				.flatMapObservable(selectedAddress -> getChoicesWithSelectedAddress(selectorIdentifier, scope, selectedAddress.getGuid()))
				.switchIfEmpty(getChoicesWithSelectedAddress(selectorIdentifier, scope, ""));
	}

	@Override
	public Single<SelectResult<BillingAddressSelectorIdentifier>> selectChoice(final BillingAddressSelectorChoiceIdentifier
																						   billingAddressSelectorChoiceIdentifier) {
		String addressId = billingAddressSelectorChoiceIdentifier.getAddress().getAddressId().getValue();
		return getCustomer()
				.flatMap(customer -> setBillingAddress(customer, addressId))
				.map(selectStatus -> SelectResult.<BillingAddressSelectorIdentifier>builder()
						.withIdentifier(billingAddressSelectorChoiceIdentifier.getBillingAddressSelector())
						.withStatus(selectStatus)
						.build());
	}

	/**
	 * Set the order's billing address.
	 *
	 * @param customer customer
	 * @param addressId addressId
	 * @return CartOrder
	 */
	protected Single<SelectStatus> setBillingAddress(final Customer customer, final String addressId) {
		if (customer.getPreferredBillingAddress() != null && addressId.equals(customer.getPreferredBillingAddress().getGuid())) {
			return Single.just(SelectStatus.EXISTING);
		} else {
			return updateCustomerPreferredAddress(customer, addressId)
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
		return getCustomer().flatMapMaybe(customer -> Maybe.fromCallable(customer::getPreferredBillingAddress));
	}

	/**
	 * Build the choice.
	 *
	 * @param billingAddressSelectorChoiceIdentifier     billingAddressSelectorChoiceIdentifier
	 * @param choiceStatus                               choiceStatus
	 * @return the choice
	 */
	protected Choice buildChoice(
			final BillingAddressSelectorChoiceIdentifier billingAddressSelectorChoiceIdentifier,
			final ChoiceStatus choiceStatus) {
		return Choice.builder()
				.withDescription(billingAddressSelectorChoiceIdentifier.getAddress())
				.withAction(billingAddressSelectorChoiceIdentifier)
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the selector choice.
	 *
	 * @param billingAddressSelectorIdentifier 	   billingAddressSelectorIdentifier
	 * @param addressIdentifier                    addressIdentifier
	 * @param choiceStatus                         choiceStatus
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(
			final BillingAddressSelectorIdentifier billingAddressSelectorIdentifier,
			final AddressIdentifier addressIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildBillingAddressChoiceIdentifier(billingAddressSelectorIdentifier, addressIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the billing address choice identifier.
	 *
	 * @param billingAddressSelectorIdentifier     billingAddressSelectorIdentifier
	 * @param addressIdentifier                    addressIdentifier
	 * @return the billing address choice identifier
	 */
	protected BillingAddressSelectorChoiceIdentifier buildBillingAddressChoiceIdentifier(
			final BillingAddressSelectorIdentifier billingAddressSelectorIdentifier, final AddressIdentifier addressIdentifier) {
		return BillingAddressSelectorChoiceIdentifier.builder()
				.withAddress(addressIdentifier)
				.withBillingAddressSelector(billingAddressSelectorIdentifier)
				.build();
	}

	private Observable<SelectorChoice> getChoicesWithSelectedAddress(final BillingAddressSelectorIdentifier billingAddressSelectorIdentifier,
																	 final IdentifierPart<String> scope, final String selectedAddress) {
		return addressEntityRepository.findAll(scope)
				.map(addressIdentifier -> buildSelectorChoice(billingAddressSelectorIdentifier, addressIdentifier,
						getChoiceStatus(addressIdentifier.getAddressId().getValue(), selectedAddress)));
	}

	private ChoiceStatus getChoiceStatus(final String addressId, final String selectedAddress) {
		return selectedAddress.equals(addressId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	private Single<Customer> updateCustomerPreferredAddress(final Customer customer, final String addressGuid) {
		final CustomerAddress customerAddress = customer.getAddressByGuid(addressGuid);
		customer.setPreferredBillingAddress(customerAddress);
		return customerRepository.update(customer);
	}

	private Single<Customer> getCustomer() {
		final String userGuid = resourceOperationContext.getUserIdentifier();
		return customerRepository.getCustomer(userGuid);
	}

	private Completable updateAllCartOrdersBillingAddresses(final Customer customer) {
		return cartOrderRepository.findCartOrderGuidsByCustomerAsObservable(customer.getStoreCode(), customer.getGuid())
				.flatMapSingle(cartOrderGuid -> cartOrderRepository.findByGuidAsSingle(customer.getStoreCode(), cartOrderGuid))
				.flatMapCompletable(cartOrder -> updateCartOrderBillingAddress(customer.getPreferredBillingAddress(), cartOrder));
	}

	private Completable updateCartOrderBillingAddress(final CustomerAddress address, final CartOrder cartOrder) {
		cartOrder.setBillingAddressGuid(address.getGuid());
		return cartOrderRepository.saveCartOrderAsSingle(cartOrder).ignoreElement();
	}

	@Reference(target = "(name=addressEntityRepositoryImpl)")
	public void setAddressEntityRepository(
			final Repository<AddressEntity, AddressIdentifier> addressEntityRepository) {
		this.addressEntityRepository = addressEntityRepository;
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

}
