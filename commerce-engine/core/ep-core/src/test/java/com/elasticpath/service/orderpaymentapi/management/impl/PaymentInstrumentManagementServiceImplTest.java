/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.orderpaymentapi.management.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;

@RunWith(MockitoJUnitRunner.class)
public class PaymentInstrumentManagementServiceImplTest {

	private static final String PAYMENT_INSTRUMENT_GUID = "PAYMENT_INSTRUMENT_GUID";

	@InjectMocks
	private PaymentInstrumentManagementServiceImpl testee;

	@Mock
	private PaymentInstrumentWorkflow paymentInstrumentWorkflow;

	@Mock
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Mock
	private Order order;

	@Before
	public void setUp() {
		when(paymentInstrumentWorkflow.findByGuid(anyString())).then(
				(Answer<PaymentInstrumentDTO>) invocation -> createInstrument(invocation.getArgument(0)));
	}

	private PaymentInstrumentDTO createInstrument(final String guid) {
		final PaymentInstrumentDTO instrumentDTO = new PaymentInstrumentDTO();
		instrumentDTO.setGUID(guid);
		instrumentDTO.setName("My Instrument");
		instrumentDTO.setPaymentProviderConfigurationGuid("payment-instrument-configuration-guid");
		instrumentDTO.setPaymentProviderConfiguration(emptyMap());
		instrumentDTO.setBillingAddressGuid("billing-address-guid");
		return instrumentDTO;
	}

	@Test
	public void testGetCustomerPaymentInstrumentMapsPaymentInstrumentToDTO() {
		PaymentInstrumentDTO dto = testee.getPaymentInstrument(PAYMENT_INSTRUMENT_GUID);

		Assertions.assertThat(dto.getName()).isEqualTo("My Instrument");
	}

	@Test
	public void testFindOrderInstrument() {
		final OrderPaymentInstrument orderPaymentInstrument = mock(OrderPaymentInstrument.class);
		when(orderPaymentInstrument.getPaymentInstrumentGuid()).thenReturn(PAYMENT_INSTRUMENT_GUID);
		when(orderPaymentInstrumentService.findByOrder(order)).thenReturn(singletonList(orderPaymentInstrument));

		final List<PaymentInstrumentDTO> instruments = testee.findOrderInstruments(order);
		assertNotNull(instruments);
		assertNotSame(emptyMap(), instruments);
		assertEquals(1, instruments.size());
		assertEquals("My Instrument", instruments.get(0).getName());
		assertThat(instruments.get(0).getGUID(), equalTo(PAYMENT_INSTRUMENT_GUID));
	}

}