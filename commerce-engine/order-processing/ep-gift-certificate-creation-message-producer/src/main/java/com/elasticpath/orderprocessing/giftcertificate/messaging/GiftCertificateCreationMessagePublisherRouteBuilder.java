/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.orderprocessing.giftcertificate.messaging;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.ExchangeProperty;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.Simple;
import org.apache.camel.spi.DataFormat;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.core.messaging.giftcertificate.GiftCertificateEventType;
import com.elasticpath.domain.catalog.GiftCertificate;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePredicate;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.money.Money;
import com.elasticpath.service.order.OrderService;

/**
 * Route that responds to an {@code RESEND_CONFIRMATION} event message and creates
 * a {@code GIFT_CERTIFICATE_CREATED} message per Gift Certificate.
 */
public class GiftCertificateCreationMessagePublisherRouteBuilder extends RouteBuilder {

	private Endpoint incomingEndpoint;

	private String transactionPropagationRef;

	private EventMessagePredicate eventMessagePredicateFilter;

	private OrderService orderService;

	private DataFormat eventMessageDataFormat;

	private EventMessageFactory eventMessageFactory;

	private EventMessagePublisher eventMessagePublisher;

	private String routeId;

	@Override
	public void configure() throws Exception {
		from(getIncomingEndpoint())
			.routeId(getRouteId())
			.transacted(getTransactionPropagationRef())
			.log(LoggingLevel.DEBUG, getClass().getName(), "Received message ${body}")

			.unmarshal(getEventMessageDataFormat())

				.filter().method(getEventMessagePredicateFilter())
					.log(LoggingLevel.DEBUG, getClass().getName(), "Find order by order number [${body.guid}]")
					.setProperty("emailAddress").simple("${body.data['emailAddress']}")

					//fetch the order
					.bean(getOrderService(), "findOrderByOrderNumber(${body.guid})")

					//split electronic shipments
					.split().simple("${body.electronicShipments}")
						.log(LoggingLevel.DEBUG, getClass().getName(),
								"Preparing GIFT_CERTIFICATE_CREATED event message for Shipment [${body.shipmentNumber}]")

						//for each e-shipment, get shipment order SKUs and split them
						.split().simple("${body.shipmentOrderSkus}")
							.log(LoggingLevel.DEBUG, getClass().getName(), "Shipment Order Sku ${body}")

							//for each order sku, check if it's GC
							.filter().simple("${body.isGiftCertificate()}")
								//if yes, prepare GIFT_CERTIFICATE_CREATED message
								.bean(getGiftCertificateCreatedEventMessageFactory(), "createEventMessage")
								.log(LoggingLevel.DEBUG, getClass().getName(), "Publishing GIFT_CERTIFICATE_CREATED event message ${body}")
								//and publish it
								.bean(getEventMessagePublisher());
	}


	/**
	 * Factory method that returns a new GiftCertificateCreatedEventMessageFactory instance.
	 *
	 * @return a new OrderShipmentCreatedEventMessageFactory instance
	 */
	protected Object getGiftCertificateCreatedEventMessageFactory() {
		return new GiftCertificateCreatedEventMessageFactory();
	}

	/**
	 * Creates Event Messages with event type {@link GiftCertificateEventType#GIFT_CERTIFICATE_CREATED}.
	 */
	protected class GiftCertificateCreatedEventMessageFactory {
		/**
		 * Creates a new Event Message with event type {@link GiftCertificateEventType#GIFT_CERTIFICATE_CREATED}.
		 * @param emailAddress the email address
		 * @param orderShipment the order shipment
		 * @param orderSkuGuid the order sku guid
		 * @param orderSkuTotal the order sku total
		 * @param gcFields the order sku fields
		 * @return a new {@link com.elasticpath.messaging.EventMessage}
		 */
		public EventMessage createEventMessage(
				@ExchangeProperty("emailAddress") final String emailAddress,
				@Simple("${body.shipment}") final OrderShipment orderShipment,
				@Simple("${body.guid}") final String orderSkuGuid,
				@Simple("${body.total}") final Money orderSkuTotal,
				@Simple("${body.modifierFields.map}") final Map<String, Object> gcFields) {

			Order order = orderShipment.getOrder();

			final Map<String, Object> data = new HashMap<>(8);
			data.put("orderLocale", order.getLocale());
			data.put("orderStoreCode", order.getStoreCode());
			data.put("shipmentNumber", orderShipment.getShipmentNumber());
			data.put("shipmentType", orderShipment.getOrderShipmentType().toString());
			data.put("orderSkuGuid", orderSkuGuid);
			data.put("orderSkuTotalAmount", orderSkuTotal.getAmount().toString());
			data.put("orderSkuTotalCurrency", orderSkuTotal.getCurrency());
			data.put("gcFields", gcFields);

			if (StringUtils.isNotBlank(emailAddress)) {
				data.put("emailAddress", emailAddress);
			}

			String giftCertificateGuid = (String) gcFields.get(GiftCertificate.KEY_GUID);

			return getEventMessageFactory().createEventMessage(GiftCertificateEventType.GIFT_CERTIFICATE_CREATED, giftCertificateGuid, data);
		}
	}

	public void setIncomingEndpoint(final Endpoint incomingEndpoint) {
		this.incomingEndpoint = incomingEndpoint;
	}

	protected Endpoint getIncomingEndpoint() {
		return incomingEndpoint;
	}

	/**
	 * Sets the registry reference ID (Spring bean name) of the transaction policy to use in this route. May be {@code null} if the default
	 * transaction (PROPAGATION_REQUIRED) is sufficient.
	 *
	 * @param transactionPropagationRef the reference to the transaction policy bean, or {@code null} to use the default transaction policy
	 */
	public void setTransactionPropagationRef(final String transactionPropagationRef) {
		this.transactionPropagationRef = transactionPropagationRef;
	}

	protected String getTransactionPropagationRef() {
		return transactionPropagationRef;
	}

	public void setEventMessagePredicateFilter(final EventMessagePredicate eventMessagePredicateFilter) {
		this.eventMessagePredicateFilter = eventMessagePredicateFilter;
	}

	protected EventMessagePredicate getEventMessagePredicateFilter() {
		return eventMessagePredicateFilter;
	}

	public void setOrderService(final OrderService orderService) {
		this.orderService = orderService;
	}

	protected OrderService getOrderService() {
		return orderService;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	protected DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	protected EventMessageFactory getEventMessageFactory() {
		return eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	protected EventMessagePublisher getEventMessagePublisher() {
		return eventMessagePublisher;
	}

	protected String getRouteId() {
		return routeId;
	}

	public void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

}

