/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.provider.payment.workflow;

import static com.elasticpath.plugin.payment.provider.dto.PaymentStatus.APPROVED;
import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.domain.PaymentInstrument;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.impl.PaymentInstrumentImpl;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.util.Utils;

public class PaymentAPIWorkflowServiceReserveTest extends BasicSpringContextTest {

	private static final String PAYMENT_PROVIDER_CONFIGURATION_GUID = "paymentProviderPluginForIntegrationTesting";

	private static final MoneyDTO AMOUNT_10 = createMoneyDTO(BigDecimal.TEN);
	private static final MoneyDTO AMOUNT_9 = createMoneyDTO(BigDecimal.valueOf(9));
	private static final MoneyDTO AMOUNT_8 = createMoneyDTO(BigDecimal.valueOf(8));
	private static final MoneyDTO AMOUNT_1 = createMoneyDTO(BigDecimal.ONE);
	private static final MoneyDTO AMOUNT_0 = createMoneyDTO(BigDecimal.ZERO);

	private static final String ORDER_NUMBER = "OrderNumber";
	private static final String CURRENCY_CODE = "CAD";
	private static final String CUSTOMER_EMAIL = "Email";
	private static final String INSTRUMENT_NAME = "InstrumentName";

	@Autowired
	private PaymentAPIWorkflow testee;

	@Autowired
	private PaymentProviderConfigurationService paymentProviderConfigurationService;

	@Autowired
	private PaymentInstrumentService paymentInstrumentService;

	public void setUp() {
		//this test is deliberately set up to not use @DirtiesDatabase by using Utils.uniqueCode()
	}

	/**
	 * Test successful reserve.
	 */
	@Test
	public void testSuccessfulReserveWithUnlimitedAmount() {
		PaymentAPIResponse reserveResponse = testee.reserve(createReserveRequestOPI(AMOUNT_0));

		final PaymentEvent paymentEvent = reserveResponse.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent, AMOUNT_10);

		assertThat(reserveResponse.getEvents().size()).isEqualTo(1);
		assertThat(reserveResponse.isSuccess()).isTrue();
	}

	/**
	 * Test successful reserve with multiple limited order payment instruments.
	 */
	@Test
	public void testSuccessfulReserveWithMultiLimitedOrderPaymentInstruments() {
		ReserveRequest reserveRequest = createReserveRequestOPI(AMOUNT_8, AMOUNT_1, AMOUNT_0);
		PaymentAPIResponse reserveResponse = testee.reserve(reserveRequest);

		assertThat(reserveResponse.getEvents().size()).isEqualTo(reserveRequest.getSelectedOrderPaymentInstruments().size());
		assertThat(reserveResponse.isSuccess()).isTrue();

		final PaymentEvent paymentEvent = reserveResponse.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent, AMOUNT_8);

		final PaymentEvent paymentEvent2 = reserveResponse.getEvents().get(1);
		assertThat(paymentEvent2.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent2.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent2, AMOUNT_1);

		final PaymentEvent paymentEvent3 = reserveResponse.getEvents().get(2);
		assertThat(paymentEvent3.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent3.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent3, AMOUNT_1);
	}

	/**
	 * Test successful reserve with multiple limited order payment instruments.
	 */
	@Test
	public void testSuccessfulReserveWithMultiLimitedOrderPaymentInstruments2() {
		ReserveRequest reserveRequest = createReserveRequestOPI(AMOUNT_9, AMOUNT_1, AMOUNT_0);
		PaymentAPIResponse reserveResponse = testee.reserve(reserveRequest);

		assertThat(reserveResponse.getEvents().size()).isEqualTo(2);
		assertThat(reserveResponse.isSuccess()).isTrue();

		final PaymentEvent paymentEvent = reserveResponse.getEvents().get(0);
		assertThat(paymentEvent.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent, AMOUNT_9);

		final PaymentEvent paymentEvent2 = reserveResponse.getEvents().get(1);
		assertThat(paymentEvent2.getPaymentType()).isEqualTo(RESERVE);
		assertThat(paymentEvent2.getPaymentStatus()).isEqualTo(APPROVED);
		assertPaymentEventFields(paymentEvent2, AMOUNT_1);
	}

	private void assertPaymentEventFields(final PaymentEvent paymentEvent, final MoneyDTO amount) {
		assertThat(paymentEvent.getAmount().getAmount()).isEqualTo(amount.getAmount());
		assertThat(paymentEvent.getAmount().getCurrencyCode()).isEqualTo(amount.getCurrencyCode());
	}

	private ReserveRequest createReserveRequestOPI(final MoneyDTO... limits) {

		PaymentProviderConfiguration configuration = createAndPersistTestPaymentProviderConfiguration(PAYMENT_PROVIDER_CONFIGURATION_GUID);
		final ReserveRequest reserveRequest = new ReserveRequest();
		List<OrderPaymentInstrumentDTO> orderPaymentInstrumentDTOList = new ArrayList<>();

		for (MoneyDTO limit : limits) {
			OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = createOrderPaymentInstrumentDTO(configuration, UUID.randomUUID().toString());
			orderPaymentInstrumentDTO.setLimit(limit);
			orderPaymentInstrumentDTOList.add(orderPaymentInstrumentDTO);
		}

		reserveRequest.setSelectedOrderPaymentInstruments(orderPaymentInstrumentDTOList);
		reserveRequest.setAmount(AMOUNT_10);
		reserveRequest.setCustomRequestData(Collections.emptyMap());
		reserveRequest.setOrderContext(createOrderContext());
		return reserveRequest;
	}

	private OrderPaymentInstrumentDTO createOrderPaymentInstrumentDTO(final PaymentProviderConfiguration configuration, final String guid) {
		final OrderPaymentInstrumentDTO orderInstrument = new OrderPaymentInstrumentDTO();
		orderInstrument.setBillingAddress(new AddressDTO());
		orderInstrument.setCustomerEmail(CUSTOMER_EMAIL);
		orderInstrument.setOrderNumber(ORDER_NUMBER);
		orderInstrument.setPaymentInstrument(createPaymentInstrumentDTO(configuration, guid));
		orderInstrument.setOrderPaymentInstrumentData(Collections.emptyMap());
		return orderInstrument;
	}

	private PaymentInstrumentDTO createPaymentInstrumentDTO(final PaymentProviderConfiguration configuration, final String guid) {

		final PaymentInstrumentDTO paymentInstrumentDTO = new PaymentInstrumentDTO();
		paymentInstrumentDTO.setPaymentProviderConfigurationGuid(configuration.getGuid());
		paymentInstrumentDTO.setData(Collections.emptyMap());
		paymentInstrumentDTO.setName(INSTRUMENT_NAME);
		paymentInstrumentDTO.setGUID(guid);
		paymentInstrumentDTO.setPaymentProviderConfiguration(Collections.emptyMap());
		paymentInstrumentDTO.setSupportingMultiCharges(false);
		paymentInstrumentDTO.setSingleReservePerPI(false);
		final PaymentInstrument paymentInstrument = new PaymentInstrumentImpl();
		paymentInstrument.setPaymentProviderConfiguration(configuration);
		paymentInstrument.setPaymentInstrumentData(Collections.emptySet());
		paymentInstrument.setName(INSTRUMENT_NAME);
		paymentInstrument.setGuid(guid);
		paymentInstrument.setSupportingMultiCharges(false);
		paymentInstrument.setSingleReservePerPI(false);

		paymentInstrumentService.saveOrUpdate(paymentInstrument);
		return paymentInstrumentDTO;
	}

	private PaymentProviderConfiguration createAndPersistTestPaymentProviderConfiguration(final String pluginId) {
		PaymentProviderConfiguration configuration = getBeanFactory().getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION, PaymentProviderConfiguration.class);

		configuration.setConfigurationName(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentProviderPluginId(pluginId);
		configuration.setGuid(Utils.uniqueCode("CONFIGURATION_NAME"));
		configuration.setPaymentConfigurationData(Collections.emptySet());

		paymentProviderConfigurationService.saveOrUpdate(configuration);

		return configuration;
	}

	private static MoneyDTO createMoneyDTO(final BigDecimal amount) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(CURRENCY_CODE)
				.build(new MoneyDTO());
	}

	private OrderContext createOrderContext() {
		final List<OrderSkuDTO> orderSkuDtos = new ArrayList<>();
		int quantity = 2;
		BigDecimal unitPrice = BigDecimal.TEN;
		BigDecimal taxAmount = BigDecimal.ONE;
		BigDecimal discount = BigDecimal.ZERO;
		String displayName = "Device";
		String sku = "skuCode";
		BigDecimal total = unitPrice
				.multiply(new BigDecimal(quantity))
				.add(taxAmount)
				.subtract(discount);
		orderSkuDtos.add(OrderSkuDTOBuilder.builder()
				.withDisplayName(displayName)
				.withQuantity(quantity)
				.withPrice(unitPrice)
				.withTaxAmount(taxAmount)
				.withTotal(total)
				.withSkuCode(sku)
				.build(new OrderSkuDTO()));
		return OrderContextBuilder.builder()
				.withOrderSkus(orderSkuDtos)
				.withOrderTotal(createMoneyDTO(BigDecimal.TEN))
				.withOrderNumber(ORDER_NUMBER)
				.build(new OrderContext());
	}

}
