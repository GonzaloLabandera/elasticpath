/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.fulfillment.event;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.cmclient.core.CmSingletonUtil;
import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.event.SearchResultEvent;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;

/**
 * Event service for sending notifications on occurring events.
 */
public final class FulfillmentEventService {

	private final List<AccountEventListener> accountListeners;

	private final List<CustomerEventListener> customerListeners;

	private final List<OrderEventListener> orderListeners;

	/**
	 * Private constructor following the singleton pattern.
	 */
	private FulfillmentEventService() {
		super();
		accountListeners = new ArrayList<>();
		customerListeners = new ArrayList<>();
		orderListeners = new ArrayList<>();
	}

	/**
	 * Gets a session instance of <code>FulfillmentEventService</code>.
	 * 
	 * @return session instance of <code>FulfillmentEventService</code>
	 */
	public static FulfillmentEventService getInstance() {
		return CmSingletonUtil.getSessionInstance(FulfillmentEventService.class);
	}

	/**
	 * Notifies all the listeners with an <code>SearchResultEvent</code> event.
	 * 
	 * @param event the search result event
	 */
	public void fireOrderSearchResultEvent(final SearchResultEvent<Order> event) {
		for (final OrderEventListener eventListener : orderListeners) {
			eventListener.searchResultsUpdate(event);
		}
	}

	/**
	 * Notifies all the listeners with an <code>SearchResultEvent</code> event.
	 * 
	 * @param event the search result event
	 */
	public void fireCustomerSearchResultEvent(final SearchResultEvent<Customer> event) {
		for (final CustomerEventListener eventListener : customerListeners) {
			eventListener.searchResultsUpdate(event);
		}
	}

	/**
	 * Notifies all the listeners with an <code>SearchResultEvent</code> event.
	 *
	 * @param event the search result event
	 */
	public void fireAccountSearchResultEvent(final SearchResultEvent<Customer> event) {
		for (final AccountEventListener eventListener : accountListeners) {
			eventListener.searchResultsUpdate(event);
		}
	}

	/**
	 * Notifies all the listeners with an <code>CustomerChangeEvent</code> event.
	 * 
	 * @param event the customer change event
	 */
	public void fireCustomerChangeEvent(final ItemChangeEvent<Customer> event) {
		for (final CustomerEventListener eventListener : customerListeners) {
			eventListener.customerChanged(event);
		}
	}

	/**
	 * Notifies all the listeners with an <code>OrderChangeEvent</code> event.
	 * 
	 * @param event the order change event
	 */
	public void fireOrderChangeEvent(final ItemChangeEvent<Order> event) {
		for (final OrderEventListener eventListener : orderListeners) {
			eventListener.orderChanged(event);
		}
	}

	/**
	 * Registers a <code>FulfillmentEventListener</code> listener.
	 * 
	 * @param listener the fulfillment event listener
	 */
	public void registerOrderEventListener(final OrderEventListener listener) {
		if (!orderListeners.contains(listener)) {
			orderListeners.add(listener);
		}
	}

	/**
	 * Registers a <code>FulfillmentEventListener</code> listener.
	 * 
	 * @param listener the fulfillment event listener
	 */
	public void registerCustomerEventListener(final CustomerEventListener listener) {
		if (!customerListeners.contains(listener)) {
			customerListeners.add(listener);
		}
	}

	/**
	 * Registers a <code>FulfillmentEventListener</code> listener.
	 *
	 * @param listener the account event listener
	 */
	public void registerAccountEventListener(final AccountEventListener listener) {
		if (!accountListeners.contains(listener)) {
			accountListeners.add(listener);
		}
	}

	/**
	 * Unregisters a <code>FulfillmentEventListener</code> listener.
	 * 
	 * @param listener the fulfillment event listener
	 */
	public void unregisterOrderEventListener(final OrderEventListener listener) {
		if (orderListeners.contains(listener)) {
			orderListeners.remove(listener);
		}
	}

	/**
	 * Unregisters a <code>FulfillmentEventListener</code> listener.
	 * 
	 * @param listener the fulfillment event listener
	 */
	public void unregisterCustomerEventListener(final CustomerEventListener listener) {
		if (customerListeners.contains(listener)) {
			customerListeners.remove(listener);
		}
	}

	/**
	 * Unregisters a <code>FulfillmentEventListener</code> listener.
	 *
	 * @param listener the account event listener
	 */
	public void unregisterAccountEventListener(final AccountEventListener listener) {
		if (accountListeners.contains(listener)) {
			accountListeners.remove(listener);
		}
	}

}
