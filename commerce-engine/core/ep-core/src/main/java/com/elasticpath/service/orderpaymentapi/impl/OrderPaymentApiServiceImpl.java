/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT;
import static com.elasticpath.commons.constants.ContextIdNames.ORDER_PAYMENT_AMOUNTS;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_ADDRESS_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_CUSTOMER_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_MONEY_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_ORDER_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PAYMENT_ORDER_SKU_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PIC_FIELDS_REQUEST_CONTEXT_DTO;
import static com.elasticpath.commons.constants.ContextIdNames.PIC_REQUEST_CONTEXT_DTO;
import static com.elasticpath.domain.order.OrderShipmentStatus.SHIPPED;
import static com.elasticpath.provider.payment.service.PaymentsExceptionMessageId.PAYMENT_FAILED;
import static com.elasticpath.provider.payment.service.PaymentsExceptionMessageId.PAYMENT_METHOD_MISSING;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.event.OrderEventHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPaymentStatus;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.impl.OrderSkuImpl;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentData;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.impl.CustomerContext;
import com.elasticpath.domain.orderpaymentapi.impl.OrderPaymentDataImpl;
import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.domain.shoppingcart.ShoppingItemTaxSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.AddressDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.CustomerContextDTO;
import com.elasticpath.plugin.payment.provider.dto.CustomerContextDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.plugin.payment.provider.dto.OrderContextBuilder;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderSkuDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTO;
import com.elasticpath.plugin.payment.provider.dto.PICRequestContextDTOBuilder;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequest;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryRequestBuilder;
import com.elasticpath.provider.payment.domain.history.PaymentEventHistoryResponse;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequest;
import com.elasticpath.provider.payment.domain.transaction.cancel.CancelAllReservationsRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.charge.ChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.CreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ManualCreditRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequest;
import com.elasticpath.provider.payment.domain.transaction.credit.ReverseChargeRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequest;
import com.elasticpath.provider.payment.domain.transaction.modify.ModifyReservationRequestBuilder;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequestBuilder;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.PaymentsExceptionMessageId;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.event.PaymentEventBuilder;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTOBuilder;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.provider.payment.workflow.PaymentAPIWorkflow;
import com.elasticpath.provider.payment.workflow.PaymentHistoryWorkflow;
import com.elasticpath.provider.payment.workflow.PaymentInstrumentWorkflow;
import com.elasticpath.service.customer.dao.CustomerAddressDao;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;

/**
 * Default implementation for {@link OrderPaymentApiService}.
 */
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects"})
public class OrderPaymentApiServiceImpl implements OrderPaymentApiService {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderPaymentApiServiceImpl.class);
	private static final String ERROR_REASON = "reason";

	private final PaymentAPIWorkflow paymentAPIWorkflow;
	private final BeanFactory beanFactory;
	private final OrderPaymentInstrumentService orderPaymentInstrumentService;
	private final OrderPaymentService orderPaymentService;
	private final OrderEventHelper orderEventHelper;
	private final PaymentInstrumentWorkflow paymentInstrumentWorkflow;
	private final PricingSnapshotService pricingSnapshotService;
	private final TaxSnapshotService taxSnapshotService;
	private final CustomerAddressDao customerAddressDao;
	private final PaymentHistoryWorkflow paymentHistoryWorkflow;

	/**
	 * Constructor.
	 *
	 * @param paymentAPIWorkflow            payment API workflow service
	 * @param beanFactory                   EP bean factory
	 * @param orderPaymentInstrumentService order payment instrument service
	 * @param orderPaymentService           order payment service
	 * @param orderEventHelper              order event helper
	 * @param paymentInstrumentWorkflow     payment instrument workflow
	 * @param pricingSnapshotService        pricingSnapshot service
	 * @param taxSnapshotService            taxSnapshot service
	 * @param customerAddressDao            customer address DAO
	 * @param paymentHistoryWorkflow        payment history workflow
	 */
	@SuppressWarnings({"PMD.ExcessiveParameterList", "checkstyle:parameternumber", "squid:S00107"})
	public OrderPaymentApiServiceImpl(final PaymentAPIWorkflow paymentAPIWorkflow,
									  final BeanFactory beanFactory,
									  final OrderPaymentInstrumentService orderPaymentInstrumentService,
									  final OrderPaymentService orderPaymentService,
									  final OrderEventHelper orderEventHelper,
									  final PaymentInstrumentWorkflow paymentInstrumentWorkflow,
									  final PricingSnapshotService pricingSnapshotService,
									  final TaxSnapshotService taxSnapshotService,
									  final CustomerAddressDao customerAddressDao,
									  final PaymentHistoryWorkflow paymentHistoryWorkflow) {
		this.paymentAPIWorkflow = paymentAPIWorkflow;
		this.beanFactory = beanFactory;
		this.orderPaymentInstrumentService = orderPaymentInstrumentService;
		this.orderPaymentService = orderPaymentService;
		this.orderEventHelper = orderEventHelper;
		this.paymentInstrumentWorkflow = paymentInstrumentWorkflow;
		this.pricingSnapshotService = pricingSnapshotService;
		this.taxSnapshotService = taxSnapshotService;
		this.customerAddressDao = customerAddressDao;
		this.paymentHistoryWorkflow = paymentHistoryWorkflow;
	}

	@Override
	public void orderCreated(final Order order) {
		ReserveRequest reserveRequest = createReserveRequest(order);
		reserveSanityCheck(reserveRequest);

		final PaymentAPIResponse paymentAPIResponse = paymentAPIWorkflow.reserve(reserveRequest);

		savePaymentEvents(paymentAPIResponse.getEvents(), order);
		logFailedPaymentEvents(paymentAPIResponse.getEvents(), order);
		checkFailedStatus(paymentAPIResponse);
	}

	/**
	 * Ensure there always only 1 unlimited payment instrument and the total limit amounts are less than the order amount.
	 *
	 * @param reserveRequest the reservation request
	 */
	protected void reserveSanityCheck(final ReserveRequest reserveRequest) {
		if (BigDecimal.ZERO.compareTo(reserveRequest.getAmount().getAmount()) == 0) { //free
			return;
		}

		long unlimitedCount = reserveRequest.getSelectedOrderPaymentInstruments().stream()
				.filter(orderPaymentInstrumentDTO -> !orderPaymentInstrumentDTO.hasLimit())
				.count();
		if (unlimitedCount == 0) {
			throw new PaymentsException(PAYMENT_METHOD_MISSING,
					ImmutableMap.of(ERROR_REASON, "At least 1 unlimited Order Payment Instrument required"));
		} else if (unlimitedCount > 1) {
			throw new PaymentsException(PAYMENT_FAILED,
					ImmutableMap.of(ERROR_REASON, "Only 1 unlimited Order Payment Instrument allowed"));
		}

		BigDecimal limitsTotal = reserveRequest.getSelectedOrderPaymentInstruments().stream()
				.filter(OrderPaymentInstrumentDTO::hasLimit)
				.map(orderPaymentInstrumentDTO -> orderPaymentInstrumentDTO.getLimit().getAmount())
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal amountAfter = reserveRequest.getAmount().getAmount().subtract(limitsTotal);

		if (BigDecimal.ZERO.compareTo(amountAfter) > 0) {
			throw new PaymentsException(PAYMENT_FAILED, ImmutableMap.of(ERROR_REASON,
					"Totalled limit amounts: " + limitsTotal + ", exceed order total: " + reserveRequest.getAmount().getAmount()));
		}
	}

	@Override
	public void rollbackOrderCreated(final Order order) {
		CancelAllReservationsRequest cancelAllReservationRequest = createCancelAllReservationRequest(order);
		final PaymentAPIResponse response = paymentAPIWorkflow.cancelAllReservations(cancelAllReservationRequest);
		savePaymentEvents(response.getEvents(), order);
		logFailedPaymentEvents(response.getEvents(), order);
		checkFailedStatus(response);
	}

	@Override
	public List<PaymentEvent> shipmentCompleted(final OrderShipment orderShipment) {
		final Order order = orderShipment.getOrder();
		final BigDecimal completedShipmentsTotal = calculateCompletedShipmentsTotal(orderShipment.getOrder());
		final MoneyDTO totalChargeableAmount = createMoneyDTO(completedShipmentsTotal.add(orderShipment.getTotal()), getCurrencyCode(orderShipment));
		final boolean isFinalPayment = isLastShipment(order, orderShipment);
		final boolean isSingleChargePerPI = isSingleReservePerPI(order);
		final ChargeRequest chargeRequest = createChargeRequest(order, totalChargeableAmount, isFinalPayment, isSingleChargePerPI);

		final PaymentAPIResponse response = paymentAPIWorkflow.chargePayment(chargeRequest);

		savePaymentEvents(response.getEvents(), order);
		logFailedPaymentEvents(response.getEvents(), order);
		checkFailedStatus(response);

		logOrderPaymentEvents(order, response.getEvents());
		return response.getEvents();
	}

	/**
	 * Create order notes for payment events.
	 *
	 * @param order         order
	 * @param paymentEvents payment events
	 */
	protected void logOrderPaymentEvents(final Order order, final List<PaymentEvent> paymentEvents) {
		for (PaymentEvent paymentEvent : paymentEvents) {
			if (paymentEvent != null && paymentEvent.getPaymentStatus() == PaymentStatus.APPROVED) {
				if (paymentEvent.getPaymentType() == TransactionType.CHARGE) {
					orderEventHelper.logOrderPaymentCaptured(order, paymentEvent);
				}
				if (paymentEvent.getPaymentType() == TransactionType.CREDIT) {
					orderEventHelper.logOrderPaymentRefund(order, paymentEvent);
				}
				if (paymentEvent.getPaymentType() == TransactionType.MANUAL_CREDIT) {
					orderEventHelper.logOrderPaymentManualRefund(order, paymentEvent);
				}
			}
		}
	}

	@Override
	public void rollbackShipmentCompleted(final OrderShipment orderShipment, final List<PaymentEvent> paymentEvents) {
		final Order order = orderShipment.getOrder();
		ReverseChargeRequest reverseChargeRequest = createReverseChargeRequest(order, paymentEvents);
		final PaymentAPIResponse response = paymentAPIWorkflow.reverseCharge(reverseChargeRequest);

		savePaymentEvents(response.getEvents(), order);
		logFailedPaymentEvents(response.getEvents(), order);
		checkFailedStatus(response);
	}

	@Override
	public void shipmentCanceled(final OrderShipment orderShipment) {
		if (orderShipment.getShipmentStatus().equals(SHIPPED)) {
			throw new IllegalArgumentException("Shipment has already been shipped: " + orderShipment.getShipmentNumber());
		}
		final Order order = orderShipment.getOrder();
		final boolean isSingleReservePerPI = isSingleReservePerPI(order);
		final boolean isLastShipment = isLastShipment(order, orderShipment);

		final PaymentAPIResponse paymentAPIResponse;
		if (isSingleReservePerPI && isLastShipment) {
			paymentAPIResponse = cancelShipmentsAndChargeFinalPayment(order);
		} else {
			final BigDecimal newAmount = subtractTotals(order.getAdjustedOrderTotalMoney().getAmount(), orderShipment.getTotal());
			final ModifyReservationRequest modifyRequest = createModifyReservationRequest(order, newAmount, isLastShipment, isSingleReservePerPI);
			paymentAPIResponse = paymentAPIWorkflow.modifyReservation(modifyRequest);
		}

		savePaymentEvents(paymentAPIResponse.getEvents(), order);
		logFailedPaymentEvents(paymentAPIResponse.getEvents(), order);
		checkFailedStatus(paymentAPIResponse);
	}

	@Override
	public void orderCanceled(final Order order) {
		final CancelAllReservationsRequest cancelAllReservationRequest = createCancelAllReservationRequest(order);
		final PaymentAPIResponse paymentAPIResponse = paymentAPIWorkflow.cancelAllReservations(cancelAllReservationRequest);

		savePaymentEvents(paymentAPIResponse.getEvents(), order);
		logFailedPaymentEvents(paymentAPIResponse.getEvents(), order);
		checkFailedStatus(paymentAPIResponse);
	}

	@Override
	public void orderModified(final Order order, final List<PaymentInstrumentDTO> paymentInstruments, final Money newTotal) {
		if (newTotal.getAmount().signum() < 0) {
			throw new IllegalArgumentException("New order amount cannot be negative: " + newTotal);
		}

		final ModifyReservationRequest request = createModifyReservationRequest(order, newTotal.getAmount(), false, isSingleReservePerPI(order));
		final PaymentAPIResponse response = paymentAPIWorkflow.modifyReservation(request);
		if (!response.isSuccess() && response.getEvents().isEmpty()) {
			throw new PaymentsException(PaymentsExceptionMessageId.PAYMENT_DECLINED,
					createExceptionMap(response.getExternalMessage(), response.getInternalMessage()));
		}

		savePaymentEvents(response.getEvents(), order);
		logFailedPaymentEvents(response.getEvents(), order);
		checkFailedStatus(response);
	}

	@Override
	public void refund(final Order order, final List<PaymentInstrumentDTO> paymentInstruments, final Money amountToRefund) {
		if (amountToRefund.getAmount().signum() < 0) {
			throw new IllegalArgumentException("Cannot refund a negative amount: " + amountToRefund);
		}

		final CreditRequest creditRequest = createCreditRequest(order, amountToRefund);
		final PaymentAPIResponse paymentAPIResponse = paymentAPIWorkflow.credit(creditRequest);
		savePaymentEvents(paymentAPIResponse.getEvents(), order);
		checkFailedStatus(paymentAPIResponse);
		logOrderPaymentEvents(order, paymentAPIResponse.getEvents());
	}

	@Override
	public void manualRefund(final Order order, final Money amountToRefund) {
		if (amountToRefund.getAmount().signum() < 0) {
			throw new IllegalArgumentException("Cannot refund a negative amount: " + amountToRefund);
		}

		final ManualCreditRequest manualCreditRequest = createManualCreditRequest(order, amountToRefund);
		final PaymentAPIResponse paymentAPIResponse = paymentAPIWorkflow.manualCredit(manualCreditRequest);
		savePaymentEvents(paymentAPIResponse.getEvents(), order);
		logOrderPaymentEvents(order, paymentAPIResponse.getEvents());
		checkFailedStatus(paymentAPIResponse);
	}

	@Override
	public PICInstructionsFieldsDTO getPICInstructionsFields(final String paymentProviderConfigGuid,
															 final PICFieldsRequestContext pICFieldsRequestContext) {
		return paymentAPIWorkflow.getPICInstructionFields(paymentProviderConfigGuid, createPICFieldsRequestContextDTO(pICFieldsRequestContext));
	}

	@Override
	public PICInstructionsDTO getPICInstructions(final String paymentProviderConfigGuid,
												 final Map<String, String> instructionsForm,
												 final PICRequestContext context) {
		return paymentAPIWorkflow.getPICInstructions(paymentProviderConfigGuid, instructionsForm, createPICRequestContextDTO(context));
	}

	@Override
	public PaymentInstrumentCreationFieldsDTO getPICFields(final String paymentProviderConfigGuid,
														   final PICFieldsRequestContext pICFieldsRequestContext) {
		return paymentAPIWorkflow.getPICFields(paymentProviderConfigGuid, createPICFieldsRequestContextDTO(pICFieldsRequestContext));
	}

	@Override
	public String createPI(final String paymentProviderConfigGuid,
						   final Map<String, String> instrumentForm,
						   final PICRequestContext context) {
		return paymentAPIWorkflow.createPI(paymentProviderConfigGuid, instrumentForm, createPICRequestContextDTO(context));
	}

	/**
	 * Cancel shipments and potentially charges final payment for the order.
	 * <br/>
	 * Final payment is a charge for any completed shipments if charge was deferred previously.
	 *
	 * @param order the order
	 * @return Payment API response with list of payment events
	 */
	protected PaymentAPIResponse cancelShipmentsAndChargeFinalPayment(final Order order) {
		final BigDecimal completedShipmentsTotal = calculateCompletedShipmentsTotal(order);
		if (BigDecimal.ZERO.compareTo(completedShipmentsTotal) < 0) {
			final MoneyDTO totalChargeableAmount = createMoneyDTO(completedShipmentsTotal, order.getCurrency().getCurrencyCode());
			final ChargeRequest chargeRequest = createChargeRequest(order, totalChargeableAmount, true, true);
			return paymentAPIWorkflow.chargePayment(chargeRequest);
		} else {
			final ModifyReservationRequest modifyRequest = createModifyReservationRequest(order, BigDecimal.ZERO, true, true);
			return paymentAPIWorkflow.modifyReservation(modifyRequest);
		}
	}

	/**
	 * Check if Payment API response and throw {@link PaymentsException} if it failed.
	 *
	 * @param paymentAPIResponse Payment API response
	 */
	protected void checkFailedStatus(final PaymentAPIResponse paymentAPIResponse) {
		if (!paymentAPIResponse.isSuccess()) {
			paymentAPIResponse.getEvents()
					.stream()
					.filter(paymentEvent -> paymentEvent.getPaymentStatus().equals(PaymentStatus.FAILED))
					.max(Comparator.comparing(PaymentEvent::getDate))
					.ifPresent(paymentEvent -> {
						throw new PaymentsException(PAYMENT_FAILED,
								createExceptionMap(paymentAPIResponse.getExternalMessage(), paymentAPIResponse.getInternalMessage()));
					});
			throw new PaymentsException(PAYMENT_FAILED,
					createExceptionMap(paymentAPIResponse.getExternalMessage(), paymentAPIResponse.getInternalMessage()));
		}
	}

	/**
	 * Checks for failed events in a provided list and logs them using system wide logger with {@code ERROR} level, if any.
	 *
	 * @param paymentEvents list of payment events to check for failed events.
	 * @param order         order, that payment api response relates to.
	 */
	private void logFailedPaymentEvents(final List<PaymentEvent> paymentEvents, final Order order) {
		final Predicate<PaymentEvent> failedStatusPredicate = paymentEvent -> paymentEvent.getPaymentStatus().equals(PaymentStatus.FAILED);
		final Predicate<PaymentEvent> skippedStatusPredicate = paymentEvent -> paymentEvent.getPaymentStatus().equals(PaymentStatus.SKIPPED);
		final Predicate<PaymentEvent> internalMessageIsNotEmptyPredicate = paymentEvent -> StringUtils.isNotEmpty(paymentEvent.getInternalMessage());
		final Predicate<PaymentEvent> externalMessageIsNotEmptyPredicate = paymentEvent -> StringUtils.isNotEmpty(paymentEvent.getExternalMessage());

		Optional<PaymentEvent> failedEvent = paymentEvents
				.stream()
				.filter(failedStatusPredicate
						.or(skippedStatusPredicate
								.and(internalMessageIsNotEmptyPredicate.or(externalMessageIsNotEmptyPredicate))))
				.max(Comparator.comparing(PaymentEvent::getDate));

		if (failedEvent.isPresent()) {
			PaymentEvent paymentEvent = failedEvent.get();
			LOGGER.error("{} transaction has failed for Order # {}: Internal message - \"{}\"; External message - \"{}\"",
					paymentEvent.getPaymentType(), order.getGuid(), paymentEvent.getInternalMessage(), paymentEvent.getExternalMessage());
		}
	}

	/**
	 * Create {@link PaymentsException} details map.
	 *
	 * @param externalMessage external message.
	 * @param internalMessage internal message.
	 * @return exception details map
	 */
	protected Map<String, String> createExceptionMap(final String externalMessage, final String internalMessage) {
		Map<String, String> errorMap = new HashMap<>();
		if (externalMessage != null) {
			errorMap.put("ExternalMessage", externalMessage);
		}
		if (internalMessage != null) {
			errorMap.put("InternalMessage", internalMessage);
		}
		return errorMap;
	}

	@Override
	public boolean requiresBillingAddress(final String configurationGuid) {
		return paymentAPIWorkflow.requiresBillingAddress(configurationGuid);
	}

	@Override
	public OrderPaymentAmounts getOrderPaymentAmounts(final Order order) {
		PaymentEventHistoryRequest paymentEventHistoryRequest = createPaymentEventHistoryRequest(createPaymentEventList(order));
		PaymentEventHistoryResponse paymentEventHistoryAmounts = paymentHistoryWorkflow.getPaymentEventHistoryAmounts(paymentEventHistoryRequest);

		Money amountPaid = createMoney(paymentEventHistoryAmounts.getAmountCharged(), order.getCurrency());
		Money amountRefunded = createMoney(paymentEventHistoryAmounts.getAmountRefunded(), order.getCurrency());

		OrderPaymentAmounts orderPaymentAmounts = beanFactory.getPrototypeBean(ORDER_PAYMENT_AMOUNTS, OrderPaymentAmounts.class);
		orderPaymentAmounts.setAmountPaid(amountPaid);
		orderPaymentAmounts.setAmountDue(order.getAdjustedOrderTotalMoney().subtract(amountPaid));
		orderPaymentAmounts.setAmountRefunded(amountRefunded);
		orderPaymentAmounts.setAmountRefundable(amountPaid.subtract(amountRefunded));

		return orderPaymentAmounts;
	}

	/**
	 * Converts {@link OrderPaymentInstrument} entity to DTO in Payment API namespace.
	 *
	 * @param orderPaymentInstrument instrument entity
	 * @param order                  order corresponding to the instrument
	 * @return dto
	 */
	protected OrderPaymentInstrumentDTO buildOrderPaymentInstrumentDTO(final OrderPaymentInstrument orderPaymentInstrument,
																	   final Order order) {
		final PaymentInstrumentDTO paymentInstrumentDTO =
				paymentInstrumentWorkflow.findByGuid(orderPaymentInstrument.getPaymentInstrumentGuid());

		final AddressDTO addressDTO = Optional.ofNullable(customerAddressDao.findByGuid(paymentInstrumentDTO.getBillingAddressGuid()))
				.map(this::createAddressDTO)
				.orElseGet(() -> createAddressDTO(order.getBillingAddress()));

		return OrderPaymentInstrumentDTOBuilder.builder()
				.withGuid(orderPaymentInstrument.getGuid())
				.withBillingAddress(addressDTO)
				.withCustomerEmail(order.getCustomer().getEmail())
				.withLimit(createMoneyDTO(orderPaymentInstrument.getLimitAmount(), orderPaymentInstrument.getCurrency().getCurrencyCode()))
				.withOrderNumber(order.getOrderNumber())
				.withOrderPaymentInstrumentData(Collections.emptyMap())
				.withPaymentInstrument(paymentInstrumentDTO)
				.build(beanFactory);
	}

	/**
	 * Converts {@link Address} entity to DTO in Payment API namespace.
	 *
	 * @param orderAddress entity
	 * @return dto
	 */
	protected AddressDTO createAddressDTO(final Address orderAddress) {
		return AddressDTOBuilder.builder()
				.withGuid(orderAddress.getGuid())
				.withCity(orderAddress.getCity())
				.withCountry(orderAddress.getCountry())
				.withFirstName(orderAddress.getFirstName())
				.withLastName(orderAddress.getLastName())
				.withPhoneNumber(orderAddress.getPhoneNumber())
				.withStreet1(orderAddress.getStreet1())
				.withStreet2(orderAddress.getStreet2())
				.withSubCountry(orderAddress.getSubCountry())
				.withZipOrPostalCode(orderAddress.getZipOrPostalCode())
				.build(beanFactory.getPrototypeBean(PAYMENT_ADDRESS_DTO, AddressDTO.class));
	}

	/**
	 * Converts {@link CustomerContext} Order Payment API object to DTO in Payment API namespace.
	 *
	 * @param customerContext customer context
	 * @return dto
	 */
	protected CustomerContextDTO createCustomerContextDTO(final CustomerContext customerContext) {
		return CustomerContextDTOBuilder.builder()
				.withFirstName(customerContext.getFirstName())
				.withLastName(customerContext.getLastName())
				.withUserId(customerContext.getCustomerId())
				.withEmail(customerContext.getEmailAddress())
				.build(beanFactory.getPrototypeBean(PAYMENT_CUSTOMER_CONTEXT_DTO, CustomerContextDTO.class));
	}

	/**
	 * Converts {@link PICRequestContext} Order Payment API object to DTO in Payment API namespace.
	 *
	 * @param pICRequestContext PIC request context
	 * @return dto
	 */
	protected PICRequestContextDTO createPICRequestContextDTO(final PICRequestContext pICRequestContext) {
		return PICRequestContextDTOBuilder.builder()
				.withLocale(pICRequestContext.getLocale())
				.withCurrency(pICRequestContext.getCurrency())
				.withAddressDTO(createAddressDTO(pICRequestContext.getBillingAddress()))
				.withCustomerContextDTO(createCustomerContextDTO(pICRequestContext.getCustomerContext()))
				.build(beanFactory.getPrototypeBean(PIC_REQUEST_CONTEXT_DTO, PICRequestContextDTO.class));
	}

	/**
	 * Converts {@link PICFieldsRequestContext} Order Payment API object to DTO in Payment API namespace.
	 *
	 * @param picFieldsRequestContext PIC fields request context
	 * @return dto
	 */
	protected PICFieldsRequestContextDTO createPICFieldsRequestContextDTO(final PICFieldsRequestContext picFieldsRequestContext) {
		return PICFieldsRequestContextDTOBuilder.builder()
				.withLocale(picFieldsRequestContext.getLocale())
				.withCurrency(picFieldsRequestContext.getCurrency())
				.withCustomerContextDTO(createCustomerContextDTO(picFieldsRequestContext.getCustomerContext()))
				.build(beanFactory.getPrototypeBean(PIC_FIELDS_REQUEST_CONTEXT_DTO, PICFieldsRequestContextDTO.class));
	}

	/**
	 * Converts {@link com.elasticpath.common.dto.AddressDTO} ep-core object to DTO in Payment API namespace.
	 *
	 * @param addressDTO address DTO in ep-core namespace
	 * @return DTO in Payment API namespace
	 */
	protected AddressDTO createAddressDTO(final com.elasticpath.common.dto.AddressDTO addressDTO) {
		return addressDTO == null ? null : AddressDTOBuilder.builder()
				.withGuid(addressDTO.getGuid())
				.withCity(addressDTO.getCity())
				.withCountry(addressDTO.getCountry())
				.withFirstName(addressDTO.getFirstName())
				.withLastName(addressDTO.getLastName())
				.withPhoneNumber(addressDTO.getPhoneNumber())
				.withStreet1(addressDTO.getStreet1())
				.withStreet2(addressDTO.getStreet2())
				.withSubCountry(addressDTO.getSubCountry())
				.withZipOrPostalCode(addressDTO.getZipOrPostalCode())
				.build(beanFactory.getPrototypeBean(PAYMENT_ADDRESS_DTO, AddressDTO.class));
	}

	/**
	 * Creates {@link OrderPayment} entity from {@link PaymentEvent}.
	 *
	 * @param paymentEvent payment event
	 * @param order        order
	 * @return entity
	 */
	protected OrderPayment buildOrderPayments(final PaymentEvent paymentEvent, final Order order) {
		final OrderPayment orderPayment = beanFactory.getPrototypeBean(ORDER_PAYMENT, OrderPayment.class);
		orderPayment.setAmount(paymentEvent.getAmount().getAmount());
		orderPayment.setCurrency(Currency.getInstance(paymentEvent.getAmount().getCurrencyCode()));
		orderPayment.setOrderPaymentStatus(OrderPaymentStatus.valueOf(paymentEvent.getPaymentStatus().name()));
		orderPayment.setPaymentInstrumentGuid(paymentEvent.getOrderPaymentInstrumentDTO().getPaymentInstrument().getGUID());
		orderPayment.setTransactionType(paymentEvent.getPaymentType());
		orderPayment.setCreatedDate(paymentEvent.getDate());
		orderPayment.setOriginalPI(paymentEvent.isOriginalPaymentInstrument());

		if (paymentEvent.getGuid() != null) {
			orderPayment.setGuid(paymentEvent.getGuid());
		}

		if (paymentEvent.getPaymentType() == TransactionType.RESERVE) {
			orderPayment.setParentOrderPaymentGuid(null);
		} else {
			orderPayment.setParentOrderPaymentGuid(paymentEvent.getParentGuid());
		}
		orderPayment.setOrderNumber(order.getOrderNumber());
		orderPayment.setOrderPaymentData(paymentEvent.getPaymentEventData().entrySet().stream().map(entry -> {
			OrderPaymentData orderPaymentData = new OrderPaymentDataImpl();
			orderPaymentData.setKey(entry.getKey());
			orderPaymentData.setValue(entry.getValue());
			return orderPaymentData;
		}).collect(Collectors.toSet()));

		return orderPayment;
	}

	/**
	 * Converts {@link OrderPayment} entity to {@link PaymentEvent}.
	 *
	 * @param orderPayment entity
	 * @param order        order
	 * @return payment event
	 */
	@Override
	public PaymentEvent buildPaymentEvent(final OrderPayment orderPayment, final Order order) {
		final OrderPaymentInstrument instrument = orderPaymentInstrumentService.findByOrderPayment(orderPayment);

		return PaymentEventBuilder.aPaymentEvent()
				.withAmount(createMoneyDTO(orderPayment.getAmount(), orderPayment.getCurrency().getCurrencyCode()))
				.withPaymentStatus(PaymentStatus.valueOf(orderPayment.getOrderPaymentStatus().name()))
				.withPaymentType(orderPayment.getTransactionType())
				.withDate(orderPayment.getCreatedDate())
				.withParentGuid(orderPayment.getParentOrderPaymentGuid())
				.withPaymentEventData(orderPayment.getOrderPaymentData().stream()
						.collect(Collectors.toMap(OrderPaymentData::getKey, OrderPaymentData::getValue)))
				.withOriginalPaymentInstrument(true)
				.withGuid(orderPayment.getGuid())
				.withReferenceId(orderPayment.getOrderNumber())
				.withOrderPaymentInstrumentDTO(buildOrderPaymentInstrumentDTO(instrument, order))
				.build(beanFactory);
	}

	/**
	 * Finds in DB and converts all {@link PaymentEvent}s associated with the order.
	 *
	 * @param order the order
	 * @return payment events
	 */
	protected List<PaymentEvent> createPaymentEventList(final Order order) {
		return orderPaymentService.findByOrder(order).stream()
				.map(orderPayment -> buildPaymentEvent(orderPayment, order))
				.collect(Collectors.toList());
	}

	/**
	 * Finds in DB and converts all {@link OrderPaymentInstrumentDTO}s associated with the order.
	 *
	 * @param order the order
	 * @return order payment instruments
	 */
	protected List<OrderPaymentInstrumentDTO> createOrderPaymentInstrumentDTOList(final Order order) {
		return orderPaymentInstrumentService.findByOrder(order).stream()
				.map(orderPaymentInstrument -> buildOrderPaymentInstrumentDTO(orderPaymentInstrument, order))
				.collect(Collectors.toList());
	}

	/**
	 * Creates {@link MoneyDTO} from the amount and currency code.
	 *
	 * @param amount       amount
	 * @param currencyCode currency code
	 * @return money DTO
	 */
	protected MoneyDTO createMoneyDTO(final BigDecimal amount, final String currencyCode) {
		return MoneyDTOBuilder.builder()
				.withAmount(amount)
				.withCurrencyCode(currencyCode)
				.build(beanFactory.getPrototypeBean(PAYMENT_MONEY_DTO, MoneyDTO.class));
	}

	/**
	 * Creates {@link Money} from a {@link MoneyDTO} and {@link Currency}.
	 *
	 * @param moneyDto money DTO
	 * @param currency currency
	 * @return money
	 */
	protected Money createMoney(final MoneyDTO moneyDto, final Currency currency) {
		if (moneyDto.getAmount() == null) {
			return Money.valueOf(BigDecimal.ZERO, currency);
		}
		return Money.valueOf(moneyDto.getAmount(), currency);
	}

	/**
	 * Creates Payment API reservation request for the whole order amount.
	 *
	 * @param order the order
	 * @return reservation request
	 */
	protected ReserveRequest createReserveRequest(final Order order) {
		return ReserveRequestBuilder.builder()
				.withSelectedOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withAmount(createMoneyDTO(order.getTotal(), order.getCurrency().getCurrencyCode()))
				.withOrderContext(createOrderContext(order))
				.withCustomRequestData(Collections.emptyMap())
				.build(beanFactory);
	}

	/**
	 * Creates Payment API reservation modification request for specified amount.
	 *
	 * @param order                the order
	 * @param newAmount            new reserved amount
	 * @param isFinalPayment       true if this is the last payment request for the order
	 * @param isSingleReservePerPI true if there should only be one reservation or charge per order
	 * @return reservation modification request
	 */
	protected ModifyReservationRequest createModifyReservationRequest(final Order order,
																	  final BigDecimal newAmount,
																	  final boolean isFinalPayment,
																	  final boolean isSingleReservePerPI) {
		return ModifyReservationRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withOrderContext(createOrderContext(order))
				.withAmount(createMoneyDTO(newAmount, order.getCurrency().getCurrencyCode()))
				.withLedger(createPaymentEventList(order))
				.withCustomRequestData(Collections.emptyMap())
				.withFinalPayment(isFinalPayment)
				.withSingleReservePerPI(isSingleReservePerPI)
				.build(beanFactory);
	}

	/**
	 * Creates Payment API request to cancel all reservations on the order.
	 *
	 * @param order the order
	 * @return cancel reservations request
	 */
	protected CancelAllReservationsRequest createCancelAllReservationRequest(final Order order) {
		return CancelAllReservationsRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withLedger(createPaymentEventList(order))
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext(order))
				.build(beanFactory);
	}

	private BigDecimal subtractTotals(final BigDecimal operand1, final BigDecimal operand2) {
		return (operand1.subtract(operand2).signum() < 0) ? BigDecimal.ZERO : operand1.subtract(operand2);
	}

	/**
	 * Creates order context for Payment API.
	 *
	 * @param order the order
	 * @return order context
	 */
	protected OrderContext createOrderContext(final Order order) {
		final Collection<OrderSku> allShoppingItems = order.getAllShoppingItems();
		final List<OrderSkuDTO> orderSkus = new ArrayList<>(allShoppingItems.size());
		for (OrderSku sku : allShoppingItems) {
			OrderSkuImpl skuImpl = (OrderSkuImpl) sku;
			int quantity = skuImpl.getQuantity();
			BigDecimal unitPrice = skuImpl.getUnitPrice();
			BigDecimal taxAmount = skuImpl.getTaxAmount();
			String displayName = skuImpl.getDisplayName();
			String skuCode = skuImpl.getSkuCode();
			final ShoppingItemTaxSnapshot shoppingItemTaxSnapshot = taxSnapshotService.getTaxSnapshotForOrderSku(skuImpl,
					pricingSnapshotService.getPricingSnapshotForOrderSku(skuImpl));
			BigDecimal total = taxAmount.compareTo(BigDecimal.ZERO) == 0
					? pricingSnapshotService.getPricingSnapshotForOrderSku(skuImpl).getPriceCalc().withCartDiscounts().getAmount()
					: shoppingItemTaxSnapshot.getTaxPriceCalculator().withCartDiscounts().getAmount();
			orderSkus.add(OrderSkuDTOBuilder.builder()
					.withDisplayName(displayName)
					.withQuantity(quantity)
					.withPrice(unitPrice)
					.withTaxAmount(taxAmount)
					.withTotal(total)
					.withSkuCode(skuCode)
					.build(beanFactory.getPrototypeBean(PAYMENT_ORDER_SKU_DTO, OrderSkuDTO.class)));
		}
		return OrderContextBuilder.builder()
				.withOrderSkus(orderSkus)
				.withOrderNumber(order.getOrderNumber())
				.withOrderTotal(createMoneyDTO(order.getTotal(), order.getCurrency().getCurrencyCode()))
				.withBillingAddress(createAddressDTO(order.getBillingAddress()))
				.withCustomerEmail(order.getCustomer().getEmail())
				.build(beanFactory.getPrototypeBean(PAYMENT_ORDER_CONTEXT_DTO, OrderContext.class));
	}

	/**
	 * Creates Payment API charge request for specified shipment.
	 *
	 * @param order                 the order
	 * @param totalChargeableAmount total order amount to be charged at this moment
	 * @param isFinalPayment        true if this is the last charge for the order
	 * @param isSingleReservePerPI  true if there should only be one reservation or charge per order
	 * @return charge request
	 */
	protected ChargeRequest createChargeRequest(final Order order,
												final MoneyDTO totalChargeableAmount,
												final boolean isFinalPayment,
												final boolean isSingleReservePerPI) {
		return ChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withTotalChargeableAmount(totalChargeableAmount)
				.withLedger(createPaymentEventList(order))
				.withOrderContext(createOrderContext(order))
				.withCustomRequestData(Collections.emptyMap())
				.withSingleReservePerPI(isSingleReservePerPI)
				.withFinalPayment(isFinalPayment)
				.build(beanFactory);
	}

	/**
	 * Create the ReverseChargeRequest.
	 *
	 * @param order         order
	 * @param paymentEvents list of payment events.
	 * @return the reverse charge request
	 */
	protected ReverseChargeRequest createReverseChargeRequest(final Order order, final List<PaymentEvent> paymentEvents) {
		return ReverseChargeRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withSelectedPaymentEvents(paymentEvents)
				.withLedger(createPaymentEventList(order))
				.withCustomRequestData(Collections.emptyMap())
				.withOrderContext(createOrderContext(order))
				.build(beanFactory);
	}

	/**
	 * Create the PaymentEventHistoryRequest.
	 *
	 * @param paymentEvents list of payment events.
	 * @return the payment event history request
	 */
	protected PaymentEventHistoryRequest createPaymentEventHistoryRequest(final List<PaymentEvent> paymentEvents) {
		return PaymentEventHistoryRequestBuilder.builder()
				.withLedger(paymentEvents)
				.build(beanFactory);
	}

	private static String getCurrencyCode(final OrderShipment orderShipment) {
		return orderShipment.getTotalMoney().getCurrency().getCurrencyCode();
	}

	private static BigDecimal calculateCompletedShipmentsTotal(final Order order) {
		return order.getAllShipments().stream()
				.filter(shipment -> shipment.getShipmentStatus().equals(SHIPPED))
				.map(OrderShipment::getTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Checks whether the shipment is the last shippable shipment for the order.
	 *
	 * @param order   associated order.
	 * @param current current order shipment which should be completed.
	 * @return boolean indicating whether the shipment is the last shippable shipment for the order.
	 */
	protected boolean isLastShipment(final Order order, final OrderShipment current) {
		for (final OrderShipment orderShipment : order.getAllShipments()) {
			if (!orderShipment.getShipmentNumber().equals(current.getShipmentNumber())) {
				final OrderShipmentStatus shipmentStatus = orderShipment.getShipmentStatus();
				if (!shipmentStatus.equals(SHIPPED) && !shipmentStatus.equals(OrderShipmentStatus.CANCELLED)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Creates Payment API credit request for specified amount.
	 *
	 * @param order          associated order
	 * @param amountToRefund amount to credit/refund
	 * @return credit request
	 */
	protected CreditRequest createCreditRequest(final Order order, final Money amountToRefund) {
		return CreditRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withSelectedOrderPaymentInstruments(Collections.emptyList())
				.withLedger(createPaymentEventList(order))
				.withCustomRequestData(Collections.emptyMap())
				.withAmount(createMoneyDTO(amountToRefund.getAmount(), amountToRefund.getCurrency().getCurrencyCode()))
				.withOrderContext(createOrderContext(order))
				.build(beanFactory);
	}

	/**
	 * Creates Payment API manual credit request for specified amount.
	 *
	 * @param order          associated order
	 * @param amountToRefund amount to credit/refund
	 * @return manual credit request
	 */
	protected ManualCreditRequest createManualCreditRequest(final Order order, final Money amountToRefund) {
		return ManualCreditRequestBuilder.builder()
				.withOrderPaymentInstruments(createOrderPaymentInstrumentDTOList(order))
				.withLedger(createPaymentEventList(order))
				.withCustomRequestData(Collections.emptyMap())
				.withAmount(createMoneyDTO(amountToRefund.getAmount(), amountToRefund.getCurrency().getCurrencyCode()))
				.withOrderContext(createOrderContext(order))
				.build(beanFactory);
	}

	/**
	 * Checks order has at least one single reserve per payment instrument.
	 *
	 * @param order associated order.
	 * @return boolean  true if order has single reserve per payment instrument.
	 */
	protected boolean isSingleReservePerPI(final Order order) {
		return orderPaymentInstrumentService.findByOrder(order).stream()
				.map(orderPaymentInstrument -> paymentInstrumentWorkflow.findByGuid(orderPaymentInstrument.getPaymentInstrumentGuid()))
				.anyMatch(PaymentInstrumentDTO::isSingleReservePerPI); // this is referencing plugin @SingleReservePerPI annotation
	}

	/**
	 * Persists payment events as {@link OrderPayment} entities.
	 *
	 * @param paymentEvents payment events
	 * @param order         the order
	 */
	protected void savePaymentEvents(final List<PaymentEvent> paymentEvents, final Order order) {
		paymentEvents.stream()
				.map(paymentEvent -> buildOrderPayments(paymentEvent, order))
				.forEach(orderPaymentService::saveOrUpdate);
	}

	protected PaymentAPIWorkflow getPaymentAPIWorkflow() {
		return paymentAPIWorkflow;
	}

	protected BeanFactory getBeanFactory() {
		return beanFactory;
	}

	protected OrderPaymentInstrumentService getOrderPaymentInstrumentService() {
		return orderPaymentInstrumentService;
	}

	protected OrderPaymentService getOrderPaymentService() {
		return orderPaymentService;
	}

	protected OrderEventHelper getOrderEventHelper() {
		return orderEventHelper;
	}

	protected PaymentInstrumentWorkflow getPaymentInstrumentWorkflow() {
		return paymentInstrumentWorkflow;
	}

	protected PricingSnapshotService getPricingSnapshotService() {
		return pricingSnapshotService;
	}

	protected TaxSnapshotService getTaxSnapshotService() {
		return taxSnapshotService;
	}

	protected CustomerAddressDao getCustomerAddressDao() {
		return customerAddressDao;
	}

	protected PaymentHistoryWorkflow getPaymentHistoryWorkflow() {
		return paymentHistoryWorkflow;
	}

}
