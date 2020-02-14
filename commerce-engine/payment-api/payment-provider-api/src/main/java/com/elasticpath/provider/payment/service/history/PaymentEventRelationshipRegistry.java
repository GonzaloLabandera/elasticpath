/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Represents a relationship registry between payment events.
 */
public class PaymentEventRelationshipRegistry {

	private final Map<String, List<PaymentEvent>> relationshipRegistry = new HashMap<>();

	/**
	 * Add list of payment events to registry.
	 *
	 * @param paymentEvents source list of payment events.
	 */
	public void addPaymentEvents(final List<PaymentEvent> paymentEvents) {
		for (final PaymentEvent paymentEvent : paymentEvents) {
			final String parentGuid = paymentEvent.getParentGuid();
			if (Objects.nonNull(parentGuid)) {
				if (relationshipRegistry.containsKey(parentGuid)) {
					relationshipRegistry.get(parentGuid).add(paymentEvent);
				} else {
					final List<PaymentEvent> relatedPaymentEvent = new ArrayList<>();
					relatedPaymentEvent.add(paymentEvent);
					relationshipRegistry.put(parentGuid, relatedPaymentEvent);
				}
			}
		}
	}

	/**
	 * Check if payment event has at least one child.
	 *
	 * @param paymentEvent source payment event.
	 * @return true if source payment event has children false - otherwise.
	 */
	public boolean hasChild(final PaymentEvent paymentEvent) {
		return relationshipRegistry.containsKey(paymentEvent.getGuid()) && Objects.nonNull(relationshipRegistry.get(paymentEvent.getGuid()));
	}

	/**
	 * Gets list of children for payment event.
	 *
	 * @param paymentEvent source payment event.
	 * @return list of children for payment event.
	 */
	public List<PaymentEvent> getChildren(final PaymentEvent paymentEvent) {
		return relationshipRegistry.get(paymentEvent.getGuid());
	}

}
