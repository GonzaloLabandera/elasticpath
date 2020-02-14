/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.plugin.payment.provider;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityResponse;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapabilityRequestBuilder;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.AddressDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.CustomerContextDTO;
import com.elasticpath.plugin.payment.provider.dto.CustomerContextDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTOBuilder;

/**
 * Common functionality for all payment plugin implementations.
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidFieldNameMatchingMethodName"})
public abstract class AbstractPaymentPluginTest {
	private static final String CURRENCY_CODE = "USD";
	private static final String EMAIL = "john.doe@elasticpath.com";
	private static final String FIRST_NAME = "john";
	private static final String LAST_NAME = "doe";
	private static final String USER_ID = "user_id";
	private static final String ORDER_NUMBER = "20000-1";
	private static final String SKU_DISPLAY_NAME = "Item";
	private static final int QUANTITY = 2;
	private static final BigDecimal PRICE = BigDecimal.valueOf(4);
	private static final BigDecimal TAX_AMOUNT = BigDecimal.valueOf(1);
	private static final BigDecimal TOTAL = BigDecimal.valueOf(5);
	private static final String SKU_CODE = "item_sku";
	private static final BigDecimal MODIFIED_AMOUNT = BigDecimal.valueOf(15);

	private PaymentInstrumentCreationResponse paymentInstrument;
	private Map<String, String> pluginConfigData;

	@Before
	public void createPaymentInstrument() throws Exception {
		pluginConfigData = createPluginConfigData();

		final PICInstructionsFields picInstructionsFields = ((PICClientInteractionRequestCapability) getPlugin())
				.getPaymentInstrumentCreationInstructionsFields(createPICFieldsRequestContextDTO());

		final PICInstructions picInstructions = ((PICClientInteractionRequestCapability) getPlugin())
				.getPaymentInstrumentCreationInstructions(createPICInstructionsRequest(picInstructionsFields));

		final PaymentInstrumentCreationFields instrumentCreationFields = ((PICCapability) getPlugin())
				.getPaymentInstrumentCreationFields(createPICFieldsRequestContextDTO());

		final Map<String, String> formData = populatePICForm(instrumentCreationFields, picInstructions);

		final PaymentInstrumentCreationRequest instrumentCreationRequest = createInstrumentCreationRequest(formData);
		paymentInstrument = ((PICCapability) getPlugin()).createPaymentInstrument(instrumentCreationRequest);
	}

	@Test
	public void testDigitalPurchaseAndCredit() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		credit(chargeResponse, getCreditAmount());
	}

	@Test
	public void testPhysicalPurchaseWithModifyAndCredit() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final MoneyDTO modifiedAmount = getModifiedAmount();
		final PaymentCapabilityResponse modificationResponse = modify(reservationResponse, modifiedAmount);
		final PaymentCapabilityResponse chargeResponse = charge(modificationResponse, modifiedAmount);
		credit(chargeResponse, modifiedAmount);
	}

	@Test
	public void testDigitalPurchaseAndReverseCharge() throws PaymentCapabilityRequestFailedException {
		final PaymentCapabilityResponse reservationResponse = reserve();
		final PaymentCapabilityResponse chargeResponse = charge(reservationResponse, getChargeAmount());
		reverseCharge(chargeResponse);
	}

	@Test
	public void testPurchaseCancel() throws PaymentCapabilityRequestFailedException {
		PaymentCapabilityResponse reservationResponse = reserve();
		cancel(reservationResponse, getReservationAmount());
	}

	protected PaymentCapabilityResponse reserve() throws PaymentCapabilityRequestFailedException {
		final ReserveCapabilityRequest reserveRequest = createReserveCapabilityRequest(getReservationAmount());
		return ((ReserveCapability) getPlugin()).reserve(reserveRequest);
	}

	protected PaymentCapabilityResponse modify(final PaymentCapabilityResponse reservationResponse, final MoneyDTO newAmount)
			throws PaymentCapabilityRequestFailedException {
		final ModifyCapabilityRequest modifyRequest = createModifyCapabilityRequest(reservationResponse, newAmount);
		return ((ModifyCapability) getPlugin()).modify(modifyRequest);
	}

	protected PaymentCapabilityResponse cancel(final PaymentCapabilityResponse reservationResponse, final MoneyDTO amount)
			throws PaymentCapabilityRequestFailedException {
		final CancelCapabilityRequest cancelRequest = createCancelCapabilityRequest(reservationResponse, amount);
		return ((CancelCapability) getPlugin()).cancel(cancelRequest);
	}

	protected PaymentCapabilityResponse charge(final PaymentCapabilityResponse reservationResponse, final MoneyDTO amount)
			throws PaymentCapabilityRequestFailedException {
		final ChargeCapabilityRequest chargeRequest = createChargeCapabilityRequest(reservationResponse, amount);
		return ((ChargeCapability) getPlugin()).charge(chargeRequest);
	}

	protected PaymentCapabilityResponse reverseCharge(final PaymentCapabilityResponse chargeResponse)
			throws PaymentCapabilityRequestFailedException {
		final ReverseChargeCapabilityRequest reverseChargeRequest = createReverseChargeCapabilityRequest(chargeResponse);
		return ((ReverseChargeCapability) getPlugin()).reverseCharge(reverseChargeRequest);
	}

	protected PaymentCapabilityResponse credit(final PaymentCapabilityResponse chargeResponse, final MoneyDTO amount)
			throws PaymentCapabilityRequestFailedException {
		final CreditCapabilityRequest creditRequest = createCreditCapabilityRequest(chargeResponse, amount);
		return ((CreditCapability) getPlugin()).credit(creditRequest);
	}

	protected abstract PaymentProviderPlugin getPlugin();

	protected abstract Map<String, String> populatePICForm(PaymentInstrumentCreationFields fields, PICInstructions instructions) throws Exception;

	protected abstract Map<String, String> populatePICInstructionsForm(PICInstructionsFields fields);

	protected CancelCapabilityRequest createCancelCapabilityRequest(final PaymentCapabilityResponse reservationOrModificationResponse,
																	final MoneyDTO money) {
		final CancelCapabilityRequest cancelCapabilityRequest = CancelCapabilityRequestBuilder.builder()
				.withReservationData(reservationOrModificationResponse.getData())
				.withAmount(money)
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.build(new CancelCapabilityRequest());
		cancelCapabilityRequest.setPluginConfigData(pluginConfigData);
		return cancelCapabilityRequest;
	}

	protected CreditCapabilityRequest createCreditCapabilityRequest(final PaymentCapabilityResponse chargeResponse, final MoneyDTO money) {
		final CreditCapabilityRequest creditCapabilityRequest = CreditCapabilityRequestBuilder.builder()
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.withChargeData(chargeResponse.getData())
				.withOrderContext(createOrderContext())
				.withAmount(money)
				.withCustomRequestData(Collections.emptyMap())
				.build(new CreditCapabilityRequest());
		creditCapabilityRequest.setPluginConfigData(pluginConfigData);
		return creditCapabilityRequest;
	}

	protected ReverseChargeCapabilityRequest createReverseChargeCapabilityRequest(final PaymentCapabilityResponse chargeResponse) {
		final ReverseChargeCapabilityRequest reverseChargeCapabilityRequest = ReverseChargeCapabilityRequestBuilder.builder()
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.withChargeData(chargeResponse.getData())
				.build(new ReverseChargeCapabilityRequest());
		reverseChargeCapabilityRequest.setPluginConfigData(pluginConfigData);
		return reverseChargeCapabilityRequest;
	}

	protected String pluginConfigData(final PluginConfigurationKey key) {
		return "";
	}

	protected PICFieldsRequestContextDTO createPICFieldsRequestContextDTO() {
		final PICFieldsRequestContextDTO context = PICFieldsRequestContextDTOBuilder.builder()
				.withCurrency(Currency.getInstance(CURRENCY_CODE))
				.withLocale(Locale.ENGLISH)
				.withCustomerContextDTO(createCustomerContext())
				.build(new PICFieldsRequestContextDTO());
		context.setPluginConfigData(pluginConfigData);
		return context;
	}

	protected MoneyDTO createMoney(final BigDecimal value) {
		MoneyDTO moneyDTO = new MoneyDTO();
		moneyDTO.setAmount(value);
		moneyDTO.setCurrencyCode(CURRENCY_CODE);
		return moneyDTO;
	}

	protected AddressDTO createBillingAddress() {
		return AddressDTOBuilder.builder()
				.withStreet1("123 Main Street")
				.withStreet2("2 My Apartment")
				.withCity("Beverly Hills")
				.withCountry("US")
				.withSubCountry("CA")
				.withZipOrPostalCode("90210")
				.withPhoneNumber("12345")
				.withFirstName(FIRST_NAME)
				.withLastName(LAST_NAME)
				.withGuid(UUID.randomUUID().toString())
				.build(new AddressDTO());
	}

	protected PICRequestContextDTO createPICRequestContextDTO() {
		return PICRequestContextDTOBuilder.builder()
				.withAddressDTO(createBillingAddress())
				.withCurrency(Currency.getInstance(CURRENCY_CODE))
				.withCustomerContextDTO(createCustomerContext())
				.withLocale(Locale.ENGLISH)
				.build(new PICRequestContextDTO());
	}

	protected CustomerContextDTO createCustomerContext() {
		return CustomerContextDTOBuilder.builder()
				.withEmail(EMAIL)
				.withFirstName(FIRST_NAME)
				.withLastName(LAST_NAME)
				.withUserId(USER_ID)
				.build(new CustomerContextDTO());
	}

	protected OrderContext createOrderContext() {
		final OrderSkuDTO orderSkuDto = OrderSkuDTOBuilder.builder()
				.withDisplayName(SKU_DISPLAY_NAME)
				.withQuantity(QUANTITY)
				.withPrice(PRICE)
				.withTaxAmount(TAX_AMOUNT)
				.withTotal(TOTAL)
				.withSkuCode(SKU_CODE)
				.build(new OrderSkuDTO());
		return OrderContextBuilder.builder()
				.withBillingAddress(createBillingAddress())
				.withCustomerEmail(EMAIL)
				.withOrderNumber(ORDER_NUMBER)
				.withOrderTotal(createMoney(BigDecimal.TEN))
				.withOrderSkus(Collections.singletonList(orderSkuDto))
				.build(new OrderContext());
	}

	protected ChargeCapabilityRequest createChargeCapabilityRequest(final PaymentCapabilityResponse reservationOrModificationResponse,
																	final MoneyDTO money) {
		final ChargeCapabilityRequest request = ChargeCapabilityRequestBuilder.builder()
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.withReservationData(reservationOrModificationResponse.getData())
				.withAmount(money)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(Collections.emptyMap())
				.build(new ChargeCapabilityRequest());
		request.setPluginConfigData(pluginConfigData);
		return request;
	}

	protected ReserveCapabilityRequest createReserveCapabilityRequest(final MoneyDTO money) {
		final ReserveCapabilityRequest request = ReserveCapabilityRequestBuilder.builder()
				.withAmount(money)
				.withOrderContext(createOrderContext())
				.withCustomRequestData(Collections.emptyMap())
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.build(new ReserveCapabilityRequest());
		request.setPluginConfigData(pluginConfigData);
		return request;
	}

	protected ModifyCapabilityRequest createModifyCapabilityRequest(final PaymentCapabilityResponse reservationResponse, final MoneyDTO money) {
		final ModifyCapabilityRequest request = ModifyCapabilityRequestBuilder.builder()
				.withPaymentInstrumentData(paymentInstrument.getDetails())
				.withReservationData(reservationResponse.getData())
				.withAmount(money)
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext())
				.build(new ModifyCapabilityRequest());
		request.setPluginConfigData(pluginConfigData);
		return request;
	}

	protected PICInstructionsRequest createPICInstructionsRequest(final PICInstructionsFields fields) {
		final PICInstructionsRequest request = new PICInstructionsRequest();
		request.setFormData(populatePICInstructionsForm(fields));
		request.setPICRequestContextDTO(createPICRequestContextDTO());
		request.setPluginConfigData(pluginConfigData);
		return request;
	}

	protected PaymentInstrumentCreationRequest createInstrumentCreationRequest(final Map<String, String> formData) {
		final PaymentInstrumentCreationRequest request = new PaymentInstrumentCreationRequest();
		request.setFormData(formData);
		request.setPICRequestContextDTO(createPICRequestContextDTO());
		request.setPluginConfigData(pluginConfigData);
		return request;
	}

	private Map<String, String> createPluginConfigData() {
		return getPlugin().getConfigurationKeys()
				.stream()
				.collect(Collectors.toMap(PluginConfigurationKey::getKey, this::pluginConfigData));
	}

	protected MoneyDTO getReservationAmount() {
		return createMoney(BigDecimal.TEN);
	}

	protected MoneyDTO getModifiedAmount() {
		return createMoney(MODIFIED_AMOUNT);
	}

	protected MoneyDTO getChargeAmount() {
		return getReservationAmount();
	}

	protected MoneyDTO getCreditAmount() {
		return getChargeAmount();
	}

	protected Map<String, String> getPluginConfigData() {
		return pluginConfigData;
	}

	protected PaymentInstrumentCreationResponse getPaymentInstrument() {
		return paymentInstrument;
	}

}
