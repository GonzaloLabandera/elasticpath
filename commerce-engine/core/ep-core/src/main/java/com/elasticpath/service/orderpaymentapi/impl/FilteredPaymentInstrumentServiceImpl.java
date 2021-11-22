/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.FilteredPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * Default implementation for {@link FilteredPaymentInstrumentService}.
 */
public class FilteredPaymentInstrumentServiceImpl implements FilteredPaymentInstrumentService {

	private final CustomerPaymentInstrumentService customerPaymentInstrumentService;
	private final PaymentInstrumentManagementService paymentInstrumentManagementService;
	private final StorePaymentProviderConfigService storePaymentProviderConfigService;
	private final CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService;
	private final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	/**
	 * Constructor.
	 *
	 * @param customerPaymentInstrumentService        customer payment instrument service.
	 * @param paymentInstrumentManagementService      payment instrument management service.
	 * @param storePaymentProviderConfigService       store payment provider configuration service.
	 * @param customerDefaultPaymentInstrumentService service to retrieve default CustomerPaymentInstrument.
	 * @param cartOrderPaymentInstrumentService       cart order payment instrument service
	 */
	public FilteredPaymentInstrumentServiceImpl(final CustomerPaymentInstrumentService customerPaymentInstrumentService,
												final PaymentInstrumentManagementService paymentInstrumentManagementService,
												final StorePaymentProviderConfigService storePaymentProviderConfigService,
												final CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService,
												final CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService) {
		this.customerPaymentInstrumentService = customerPaymentInstrumentService;
		this.paymentInstrumentManagementService = paymentInstrumentManagementService;
		this.storePaymentProviderConfigService = storePaymentProviderConfigService;
		this.customerDefaultPaymentInstrumentService = customerDefaultPaymentInstrumentService;
		this.cartOrderPaymentInstrumentService = cartOrderPaymentInstrumentService;
	}

	@Override
	public Collection<CustomerPaymentInstrument> findCustomerPaymentInstrumentsForCustomerAndStore(final Customer customer, final String storeCode) {
		final Collection<CustomerPaymentInstrument> customerPaymentInstruments = customerPaymentInstrumentService.findByCustomer(customer);

		return filterCustomerPaymentInstrumentsByStore(customerPaymentInstruments, storeCode);
	}

	@Override
	public CustomerPaymentInstrument findDefaultPaymentInstrumentForCustomerGuidAndStore(final String customerGuid, final String storeCode) {
		final CustomerPaymentInstrument customerDefaultPaymentInstrument =
				customerDefaultPaymentInstrumentService.getDefaultForCustomerGuid(customerGuid);

		return Optional.ofNullable(customerDefaultPaymentInstrument)
				.filter(customerPaymentInstrument -> isPaymentInstrumentAvailableInStore(customerPaymentInstrument.getPaymentInstrumentGuid(),
						storeCode))
				.orElse(null);
	}

	@Override
	public CustomerPaymentInstrument findDefaultPaymentInstrumentForCustomerAndStore(final Customer customer, final String storeCode) {
		final CustomerPaymentInstrument customerDefaultPaymentInstrument = customerDefaultPaymentInstrumentService.getDefaultForCustomer(customer);

		return Optional.ofNullable(customerDefaultPaymentInstrument)
				.filter(customerPaymentInstrument -> isPaymentInstrumentAvailableInStore(customerPaymentInstrument.getPaymentInstrumentGuid(),
						storeCode))
				.orElse(null);
	}

	@Override
	public Collection<CartOrderPaymentInstrument> findCartOrderPaymentInstrumentsForCartOrderAndStore(final CartOrder cartOrder,
																									  final String storeCode) {
		return findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(cartOrder.getGuid(), storeCode);
	}

	@Override
	public Collection<CartOrderPaymentInstrument> findCartOrderPaymentInstrumentsForCartOrderGuidAndStore(final String cartOrderGuid,
																										  final String storeCode) {
		final Collection<CartOrderPaymentInstrument> cartOrderPaymentInstruments =
				cartOrderPaymentInstrumentService.findByCartOrderGuid(cartOrderGuid);
		return filterCartOrderPaymentInstrumentsByStore(cartOrderPaymentInstruments, storeCode);
	}

	/**
	 * Gets customer payment instrument service.
	 *
	 * @return customer payment instrument service.
	 */
	protected CustomerPaymentInstrumentService getCustomerPaymentInstrumentService() {
		return customerPaymentInstrumentService;
	}

	/**
	 * Gets payment instrument management service.
	 *
	 * @return payment instrument management service.
	 */
	protected PaymentInstrumentManagementService getPaymentInstrumentManagementService() {
		return paymentInstrumentManagementService;
	}

	/**
	 * Gets store payment provider configuration service.
	 *
	 * @return store payment provider configuration service.
	 */
	protected StorePaymentProviderConfigService getStorePaymentProviderConfigService() {
		return storePaymentProviderConfigService;
	}

	/**
	 * Gets service to retrieve default CustomerPaymentInstrument.
	 *
	 * @return service to retrieve default CustomerPaymentInstrument.
	 */
	protected CustomerDefaultPaymentInstrumentService getCustomerDefaultPaymentInstrumentService() {
		return customerDefaultPaymentInstrumentService;
	}

	/**
	 * Gets service to retrieve CartOrderPaymentInstrument.
	 *
	 * @return service to retrieve CartOrderPaymentInstrument.
	 */
	protected CartOrderPaymentInstrumentService getCartOrderPaymentInstrumentService() {
		return cartOrderPaymentInstrumentService;
	}

	/**
	 * Filters {@link CustomerPaymentInstrument}s by store where they are enabled/available.
	 *
	 * @param customerPaymentInstruments unfiltered instruments
	 * @param storeCode                  store code
	 * @return filtered instruments
	 */
	protected Collection<CustomerPaymentInstrument> filterCustomerPaymentInstrumentsByStore(
			final Collection<CustomerPaymentInstrument> customerPaymentInstruments, final String storeCode) {

		return customerPaymentInstruments.stream()
				.filter(customerPaymentInstrument -> isPaymentInstrumentAvailableInStore(customerPaymentInstrument.getPaymentInstrumentGuid(),
						storeCode))
				.collect(Collectors.toList());
	}

	/**
	 * Filters {@link CartOrderPaymentInstrument}s by store where they are enabled/available.
	 *
	 * @param cartOrderPaymentInstruments unfiltered instruments
	 * @param storeCode                   store code
	 * @return filtered instruments
	 */
	protected Collection<CartOrderPaymentInstrument> filterCartOrderPaymentInstrumentsByStore(
			final Collection<CartOrderPaymentInstrument> cartOrderPaymentInstruments, final String storeCode) {

		return cartOrderPaymentInstruments.stream()
                .filter(cartOrderPaymentInstrument -> isPaymentInstrumentAvailableInStore(
                        cartOrderPaymentInstrument.getPaymentInstrumentGuid(), storeCode))
				.collect(Collectors.toList());
	}

	/**
	 * Checks if {@link com.elasticpath.provider.payment.domain.PaymentInstrument} is available for the store.
	 *
	 * @param paymentInstrumentGuid payment instrument guid
	 * @param storeCode             store code
	 * @return true if instrument is available in the store
	 */
	protected boolean isPaymentInstrumentAvailableInStore(
			final String paymentInstrumentGuid, final String storeCode) {
		final PaymentInstrumentDTO paymentInstrumentDTO = paymentInstrumentManagementService.getPaymentInstrument(paymentInstrumentGuid);
		final String paymentProviderConfigurationGuid = paymentInstrumentDTO.getPaymentProviderConfigurationGuid();
		final Collection<StorePaymentProviderConfig> storePaymentProviderConfigs =
				storePaymentProviderConfigService.findByPaymentProviderConfigGuid(paymentProviderConfigurationGuid);

        return storePaymentProviderConfigs.stream()
                .anyMatch(storePaymentProviderConfig -> storePaymentProviderConfig.getStoreCode().equalsIgnoreCase(storeCode));
	}

}
