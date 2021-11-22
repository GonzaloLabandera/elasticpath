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

import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.repository.Repository;
import com.elasticpath.repository.SelectorRepository;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.addresses.ShippingAddressSelectorIdentifier;
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
 * Implement a selector repository for the Profile-Defaults Shipping-Address Selector.
 *
 * @param <SI> The selector identifier
 * @param <CI> The Shipping-Address Selector Choice Identifier
 */
@Component
public class ShippingAddressSelectorRepositoryImpl<
		SI extends ShippingAddressSelectorIdentifier,
		CI extends ShippingAddressSelectorChoiceIdentifier>
		implements SelectorRepository<ShippingAddressSelectorIdentifier, ShippingAddressSelectorChoiceIdentifier> {

	private Repository<AddressEntity, AddressIdentifier> addressEntityRepository;
	private CustomerRepository customerRepository;
	private ResourceOperationContext resourceOperationContext;
	private CartOrderRepository cartOrderRepository;
	private AddressRepository addressRepository;

	@Override
	public Single<Choice> getChoice(final ShippingAddressSelectorChoiceIdentifier selectorChoiceId) {
		AddressIdentifier addressIdentifier = selectorChoiceId.getAddress();
		String addressId = addressIdentifier.getAddressId().getValue();

		return getSelectedAddress()
				.map(selectedAddress -> buildChoice(selectorChoiceId, getChoiceStatus(addressId, selectedAddress.getGuid())))
				.switchIfEmpty(Single.just(buildChoice(selectorChoiceId, ChoiceStatus.CHOOSABLE)));
	}

	@Override
	public Observable<SelectorChoice> getChoices(final ShippingAddressSelectorIdentifier selectorIdentifier) {
		IdentifierPart<String> scope = selectorIdentifier.getShippingAddresses().getAddresses().getScope();

		return getSelectedAddress()
				.flatMapObservable(selectedAddress -> getChoicesWithSelectedAddress(selectorIdentifier, scope, selectedAddress.getGuid()))
				.switchIfEmpty(getChoicesWithSelectedAddress(selectorIdentifier, scope, ""));
	}

	@Override
	public Single<SelectResult<ShippingAddressSelectorIdentifier>> selectChoice(final ShippingAddressSelectorChoiceIdentifier
																					   shippingAddressSelectorChoiceIdentifier) {
		String addressId = shippingAddressSelectorChoiceIdentifier.getAddress().getAddressId().getValue();
		return getCustomer()
				.flatMap(customer -> setShippingAddress(customer, addressId))
				.map(selectStatus -> SelectResult.<ShippingAddressSelectorIdentifier>builder()
						.withIdentifier(shippingAddressSelectorChoiceIdentifier.getShippingAddressSelector())
						.withStatus(selectStatus)
						.build());
	}

	/**
	 * Set the order's shipping address.
	 *
	 * @param customer customer
	 * @param addressId addressId
	 * @return CartOrder
	 */
	protected Single<SelectStatus> setShippingAddress(final Customer customer, final String addressId) {
		if (customer.getPreferredShippingAddress() != null && addressId.equals(customer.getPreferredShippingAddress().getGuid())) {
			return Single.just(SelectStatus.EXISTING);
		} else {
			return updateCustomerPreferredAddress(customer, addressId)
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
		return getCustomer().flatMapMaybe(customer -> Maybe.fromCallable(customer::getPreferredShippingAddress));
	}

	/**
	 * Build the choice.
	 *
	 * @param shippingAddressSelectorChoiceIdentifier    shippingAddressSelectorChoiceIdentifier
	 * @param choiceStatus                               choiceStatus
	 * @return the choice
	 */
	protected Choice buildChoice(
			final ShippingAddressSelectorChoiceIdentifier shippingAddressSelectorChoiceIdentifier,
			final ChoiceStatus choiceStatus) {
		return Choice.builder()
				.withDescription(shippingAddressSelectorChoiceIdentifier.getAddress())
				.withAction(shippingAddressSelectorChoiceIdentifier)
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the selector choice.
	 *
	 * @param shippingAddressSelectorIdentifier    shippingAddressSelectorIdentifier
	 * @param addressIdentifier                    addressIdentifier
	 * @param choiceStatus                         choiceStatus
	 * @return the selector choice
	 */
	protected SelectorChoice buildSelectorChoice(
			final ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier,
			final AddressIdentifier addressIdentifier,
			final ChoiceStatus choiceStatus) {
		return SelectorChoice.builder()
				.withChoice(buildShippingAddressChoiceIdentifier(shippingAddressSelectorIdentifier, addressIdentifier))
				.withStatus(choiceStatus)
				.build();
	}

	/**
	 * Build the shipping address choice identifier.
	 *
	 * @param shippingAddressSelectorIdentifier     shippingAddressSelectorIdentifier
	 * @param addressIdentifier                    addressIdentifier
	 * @return the shipping address choice identifier
	 */
	protected ShippingAddressSelectorChoiceIdentifier buildShippingAddressChoiceIdentifier(
			final ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier, final AddressIdentifier addressIdentifier) {
		return ShippingAddressSelectorChoiceIdentifier.builder()
				.withAddress(addressIdentifier)
				.withShippingAddressSelector(shippingAddressSelectorIdentifier)
				.build();
	}

	private Observable<SelectorChoice> getChoicesWithSelectedAddress(final ShippingAddressSelectorIdentifier shippingAddressSelectorIdentifier,
																	 final IdentifierPart<String> scope, final String selectedAddress) {
		return addressEntityRepository.findAll(scope)
				.map(addressIdentifier -> buildSelectorChoice(shippingAddressSelectorIdentifier, addressIdentifier,
						getChoiceStatus(addressIdentifier.getAddressId().getValue(), selectedAddress)));
	}

	private ChoiceStatus getChoiceStatus(final String addressId, final String selectedAddress) {
		return selectedAddress.equals(addressId) ? ChoiceStatus.CHOSEN : ChoiceStatus.CHOOSABLE;
	}

	private Single<Customer>  updateCustomerPreferredAddress(final Customer customer, final String addressGuid) {
		final CustomerAddress customerAddress = addressRepository.getExistingAddressByGuid(addressGuid, customer).blockingGet();
		customer.setPreferredShippingAddress(customerAddress);
		return customerRepository.update(customer);
	}

	private Single<Customer> getCustomer() {
		final String userGuid = resourceOperationContext.getUserIdentifier();
		return customerRepository.getCustomer(userGuid);
	}

	private Completable updateAllCartOrdersShippingAddresses(final Customer customer) {
		final String storeCode = customer.getStoreCode();
		return cartOrderRepository.findCartOrderGuidsByCustomer(customer.getStoreCode(), customer.getGuid())
				.flatMapCompletable(cartOrderGuid ->
						cartOrderRepository.updateShippingAddressOnCartOrder(
								customer.getPreferredShippingAddress().getGuid(), cartOrderGuid, storeCode).ignoreElement());
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
	@Reference
	public void setAddressRepository(final AddressRepository addressRepository) {
		this.addressRepository = addressRepository;
	}
}
