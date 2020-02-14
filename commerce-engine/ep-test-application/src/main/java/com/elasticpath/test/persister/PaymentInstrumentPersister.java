/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.test.persister;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_PAYMENT_INSTRUMENT;
import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT;
import static com.elasticpath.commons.constants.ContextIdNames.STORE_PAYMENT_PROVIDER_CONFIG;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.AddressDTO;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CartOrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.orderpaymentapi.impl.CustomerContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTOBuilder;
import com.elasticpath.service.cartorder.CartOrderPopulationStrategy;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Persister allows to create and save into database payment instrument dependent domain objects.
 */
public class PaymentInstrumentPersister {

	public static final String PAYMENT_INSTRUMENT_NAME = "payment-instrument-for-integration-testing";

	@Autowired
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	@Autowired
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Autowired
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Autowired
	private BeanFactory beanFactory;

	@Autowired
	private CartOrderService cartOrderService;

	@Autowired
	private OrderPaymentApiService orderPaymentApiService;

	@Autowired
	private CartOrderPopulationStrategy cartOrderPopulationStrategy;

	/**
	 * Creates a persisted payment instrument for a shopping cart.
	 *
	 * @param shoppingCart shopping cart
	 * @return cart order payment instrument
	 */
	public CartOrderPaymentInstrument persistPaymentInstrument(final ShoppingCart shoppingCart) {
		CartOrder cartOrder = cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());
		if (cartOrder == null) {
			cartOrder = cartOrderPopulationStrategy.createCartOrder(shoppingCart);
			cartOrder = cartOrderService.saveOrUpdate(cartOrder);
		}

		Customer customer = shoppingCart.getShopper().getCustomer();
		Address billingAddress = shoppingCart.getBillingAddress();

		final CartOrderPaymentInstrument instrument = beanFactory.getPrototypeBean(CART_ORDER_PAYMENT_INSTRUMENT, CartOrderPaymentInstrument.class);
		instrument.setCartOrderUid(cartOrder.getUidPk());
		instrument.setPaymentInstrumentGuid(createPaymentInstrumentAndReturnItsGuid(customer, billingAddress));
		instrument.setLimitAmount(BigDecimal.ZERO);
		instrument.setCurrency(Currency.getInstance("CAD"));
		return cartOrderPaymentInstrumentService.saveOrUpdate(instrument);
	}

	/**
	 * Creates a persisted payment instrument for a customer profile.
	 *
	 * @param customer        customer
	 * @param customerAddress customer address
	 * @return customer payment instrument
	 */
	public CustomerPaymentInstrument persistPaymentInstrument(final Customer customer, final Address customerAddress) {
        final CustomerPaymentInstrument instrument = beanFactory.getPrototypeBean(CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class);
        instrument.setCustomerUid(customer.getUidPk());
        instrument.setPaymentInstrumentGuid(createPaymentInstrumentAndReturnItsGuid(customer, customerAddress));
        return customerPaymentInstrumentService.saveOrUpdate(instrument);
    }

	private String createPaymentInstrumentAndReturnItsGuid(final Customer customer, final Address customerAddress) {
		final String configName = "Test Configuration " + UUID.randomUUID().toString();
		final PaymentProviderConfigDTO configDTO =
				paymentProviderConfigManagementService.saveOrUpdate(PaymentProviderConfigDTOBuilder.builder()
						.withGuid(UUID.randomUUID().toString())
						.withConfigurationName(configName)
						.withPaymentProviderPluginBeanName("paymentProviderPluginForIntegrationTesting")
						.withPaymentConfigurationData(Collections.emptyMap())
						.withDefaultDisplayName(configName)
						.withStatus(PaymentProviderConfigurationStatus.ACTIVE)
						.build(beanFactory));

		final PICRequestContext picRequestContext = createPICRequestContext(customer, customerAddress);
		persistStorePaymentProviderConfig(customer, configDTO);
		return orderPaymentApiService.createPI(configDTO.getGuid(),
				ImmutableMap.of("display-name", PAYMENT_INSTRUMENT_NAME),
				picRequestContext);
	}

	private void persistStorePaymentProviderConfig(Customer customer, PaymentProviderConfigDTO configDTO) {
        final StorePaymentProviderConfig storePaymentProviderConfig = beanFactory
                .getPrototypeBean(STORE_PAYMENT_PROVIDER_CONFIG, StorePaymentProviderConfig.class);
        storePaymentProviderConfig.setStoreCode(customer.getStoreCode());
        storePaymentProviderConfig.setPaymentProviderConfigGuid(configDTO.getGuid());

        storePaymentProviderConfigService.saveOrUpdate(storePaymentProviderConfig);
    }

	private PICRequestContext createPICRequestContext(final Customer customer, final Address customerAddress) {
		final CustomerContext customerContext = new CustomerContext(customer.getUserId(), customer.getFirstName(),
				customer.getLastName(), customer.getEmail());

		AddressDTO addressDTO = null;

		if (customerAddress != null) {
			addressDTO = new AddressDTO();
			addressDTO.setGuid(customerAddress.getGuid());
			addressDTO.setCity(customerAddress.getCity());
			addressDTO.setCountry(customerAddress.getCountry());
			addressDTO.setFirstName(customerAddress.getFirstName());
			addressDTO.setLastName(customerAddress.getLastName());
			addressDTO.setPhoneNumber(customerAddress.getPhoneNumber());
			addressDTO.setStreet1(customerAddress.getStreet1());
			addressDTO.setStreet2(customerAddress.getStreet2());
			addressDTO.setSubCountry(customerAddress.getSubCountry());
			addressDTO.setZipOrPostalCode(customerAddress.getZipOrPostalCode());
		}

		return new PICRequestContext(Currency.getInstance(Locale.CANADA),
				Locale.CANADA, customerContext, addressDTO);
	}

}
