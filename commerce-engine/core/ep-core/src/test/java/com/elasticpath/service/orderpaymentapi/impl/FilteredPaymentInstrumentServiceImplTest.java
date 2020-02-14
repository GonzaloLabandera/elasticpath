/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * Tests for {@link FilteredPaymentInstrumentServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FilteredPaymentInstrumentServiceImplTest {

	private static final String PAYMENT_INSTRUMENT_GUID = "paymentInstrumentGuid";
	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "paymentProviderConfigurationGuid";
	private static final String STORE_WITH_PAYMENT_CONFIGURATION = "storeWithActivePaymentConfiguration";
	private static final String STORE_WITHOUT_PAYMENT_CONFIGURATION = "storeWithoutActivePaymentConfiguration";

	private final CustomerPaymentInstrument customerPaymentInstrument = mockCustomerPaymentInstrument(PAYMENT_INSTRUMENT_GUID);

	@Mock
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Mock
	private PaymentInstrumentManagementService paymentInstrumentManagementService;

	@Mock
	private StorePaymentProviderConfigService storePaymentProviderConfigService;

	@Mock
	private CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService;

	@InjectMocks
	private FilteredPaymentInstrumentServiceImpl filteredPaymentInstrumentService;

	@Mock
	private Customer customer;

	@Before
	public void setUp() {
        when(customerPaymentInstrumentService.findByCustomer(customer)).thenReturn(Collections.singletonList(customerPaymentInstrument));
        when(customerDefaultPaymentInstrumentService.getDefaultForCustomer(customer)).thenReturn(customerPaymentInstrument);

        final PaymentInstrumentDTO paymentInstrumentDTO = createPaymentInstrumentDTO(PAYMENT_PROVIDER_CONFIGURATION_GUID);
        when(paymentInstrumentManagementService.getPaymentInstrument(PAYMENT_INSTRUMENT_GUID)).thenReturn(paymentInstrumentDTO);

        final StorePaymentProviderConfig storePaymentProviderConfig = mockStorePaymentProviderConfig(STORE_WITH_PAYMENT_CONFIGURATION);
        when(storePaymentProviderConfigService.findByPaymentProviderConfigGuid(PAYMENT_PROVIDER_CONFIGURATION_GUID))
                .thenReturn(Collections.singletonList(storePaymentProviderConfig));
    }

	@Test
	public void findCustomerPaymentInstrumentsForCustomerAndStoreShouldReturnCustomerPaymentInstrumentWhenConfigurationIsActivatedForStore() {
		final Collection<CustomerPaymentInstrument> customerPaymentInstruments =
				filteredPaymentInstrumentService.findCustomerPaymentInstrumentsForCustomerAndStore(customer, STORE_WITH_PAYMENT_CONFIGURATION);

		assertThat(customerPaymentInstruments).containsOnly(customerPaymentInstrument);
	}

	@Test
	public void findCustomerPaymentInstrumentsForCustomerAndStoreShouldReturnEmptyCollectionWhenConfigurationNotActivatedForStore() {
		final Collection<CustomerPaymentInstrument> customerPaymentInstruments =
				filteredPaymentInstrumentService.findCustomerPaymentInstrumentsForCustomerAndStore(customer, STORE_WITHOUT_PAYMENT_CONFIGURATION);

		assertThat(customerPaymentInstruments).isEmpty();
	}

	@Test
	public void findDefaultPaymentInstrumentForCustomerAndStoreShouldReturnCustomerPaymentInstrumentWhenConfigurationIsActivatedForStore() {
		final CustomerPaymentInstrument customerPaymentInstruments =
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_WITH_PAYMENT_CONFIGURATION);

		assertThat(customerPaymentInstruments).isEqualTo(customerPaymentInstrument);
	}

	@Test
	public void findDefaultPaymentInstrumentForCustomerAndStoreShouldReturnNullWhenConfigurationNotActivatedForStore() {
		final CustomerPaymentInstrument customerPaymentInstruments =
				filteredPaymentInstrumentService.findDefaultPaymentInstrumentForCustomerAndStore(customer, STORE_WITHOUT_PAYMENT_CONFIGURATION);

		assertThat(customerPaymentInstruments).isNull();
	}

	private CustomerPaymentInstrument mockCustomerPaymentInstrument(final String paymentInstrumentGuid) {
		final CustomerPaymentInstrument paymentInstrument = mock(CustomerPaymentInstrument.class);
		when(paymentInstrument.getPaymentInstrumentGuid()).thenReturn(paymentInstrumentGuid);

        return paymentInstrument;
    }

    private PaymentInstrumentDTO createPaymentInstrumentDTO(final String paymentProviderConfigurationGuid) {
        final PaymentInstrumentDTO paymentInstrumentDTO = new PaymentInstrumentDTO();
        paymentInstrumentDTO.setPaymentProviderConfigurationGuid(paymentProviderConfigurationGuid);

        return paymentInstrumentDTO;
    }

    private StorePaymentProviderConfig mockStorePaymentProviderConfig(final String storeCode) {
        final StorePaymentProviderConfig paymentProviderConfig = mock(StorePaymentProviderConfig.class);
        when(paymentProviderConfig.getStoreCode()).thenReturn(storeCode);

        return paymentProviderConfig;
    }

}
