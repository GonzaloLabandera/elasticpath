/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.event;

import com.elasticpath.cmclient.core.event.ItemChangeEvent;
import com.elasticpath.cmclient.core.event.SearchResultEvent;
import com.elasticpath.domain.customer.Customer;

/**
 * This interface must be implemented by part that need to be notified on search result events.
 */
public interface AccountEventListener {

	/**
	 * The class implementing that interface will receive an event notifying for new results.
	 *
	 * @param event instance of {@link SearchResultEvent}
	 */
	void searchResultsUpdate(SearchResultEvent<Customer> event);

	/**
	 * Notifies for a changed account.
	 *
	 * @param event account change event
	 */
	void accountChanged(ItemChangeEvent<Customer> event);
}
