/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.orderprocessing.orderaccepted.messaging;

import org.apache.camel.Endpoint;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.DataFormat;

import com.elasticpath.messaging.EventMessagePredicate;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;

/**
 * Route build for order accepted.
 */
public class OrderAcceptedRouteBuilder extends RouteBuilder {

	private Endpoint incomingEndpoint;

	private String routeId;

	private DataFormat eventMessageDataFormat;

	private OrderToPostCaptureCheckoutActionContextTransformer orderToPostCaptureCheckoutActionContextTransformer;

	private PostCaptureCheckoutService postCaptureCheckoutService;

	private EventMessagePredicate orderAcceptedOrderEventPredicate;

	@Override
	public void configure() throws Exception {

		from(getIncomingEndpoint())
				.routeId(getRouteId())
				.unmarshal(getEventMessageDataFormat())
				.filter().method(orderAcceptedOrderEventPredicate)
				.bean(orderToPostCaptureCheckoutActionContextTransformer, "transform")
				.bean(getPostCaptureCheckoutService(), "completeCheckout");
	}

	protected String getRouteId() {
		return routeId;
	}

	protected Endpoint getIncomingEndpoint() {
		return incomingEndpoint;
	}

	protected DataFormat getEventMessageDataFormat() {
		return eventMessageDataFormat;
	}

	protected OrderToPostCaptureCheckoutActionContextTransformer getOrderToPostCaptureCheckoutContextTransformer() {
		return orderToPostCaptureCheckoutActionContextTransformer;
	}

	protected PostCaptureCheckoutService getPostCaptureCheckoutService() {
		return postCaptureCheckoutService;
	}

	public void setIncomingEndpoint(final Endpoint incomingEndpoint) {
		this.incomingEndpoint = incomingEndpoint;
	}

	public void setRouteId(final String routeId) {
		this.routeId = routeId;
	}

	public void setEventMessageDataFormat(final DataFormat eventMessageDataFormat) {
		this.eventMessageDataFormat = eventMessageDataFormat;
	}

	public void setOrderToPostCaptureCheckoutActionContextTransformer(
			final OrderToPostCaptureCheckoutActionContextTransformer orderToPostCaptureCheckoutActionContextTransformer) {
		this.orderToPostCaptureCheckoutActionContextTransformer = orderToPostCaptureCheckoutActionContextTransformer;
	}

	public void setPostCaptureCheckoutService(final PostCaptureCheckoutService postCaptureCheckoutService) {
		this.postCaptureCheckoutService = postCaptureCheckoutService;
	}

	public void setOrderAcceptedOrderEventPredicate(final EventMessagePredicate messagePredicate) {
		this.orderAcceptedOrderEventPredicate = messagePredicate;
	}
}
