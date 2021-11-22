/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.fulfillment.helpers;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.event.EventType;
import com.elasticpath.cmclient.core.event.SearchResultEvent;
import com.elasticpath.cmclient.core.helpers.AbstractSearchRequestJob;
import com.elasticpath.cmclient.core.search.SearchJob;
import com.elasticpath.cmclient.core.search.impl.AccountDatabaseSearchJobImpl;
import com.elasticpath.cmclient.fulfillment.event.FulfillmentEventService;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.CustomerService;

/**
 * This class represents a job responsible for retrieving Accounts from the database.
 */
public class AccountSearchRequestJob extends AbstractSearchRequestJob<Customer> {

	private static final Logger LOG = LogManager.getLogger(AccountSearchRequestJob.class);

	private final CustomerService customerService;

	/**
	 * Default constructor.
	 *
	 */
	public AccountSearchRequestJob() {
		super();
		customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
	}

	@Override
	public void fireItemsUpdated(final List<Customer> itemList, final int startIndex, final int totalFound) {
		SearchResultEvent<Customer> event = new SearchResultEvent<>(this, itemList, startIndex, totalFound, EventType.SEARCH);
		FulfillmentEventService.getInstance().fireAccountSearchResultEvent(event);
	}

	/**
	 * Gets a list of Account {@link Customer} objects with the given <code>uidList</code>.
	 *
	 * @param uidList a list of Account as {@link Customer} UIDs
	 * @return a list of Accounts (as {@link Customer}s)
	 */
	@Override
	public List<Customer> getItems(final List<Long> uidList) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(String.format("Converting %1$S UID(s) to accounts.", uidList.size())); //$NON-NLS-1$
		}
		return customerService.findByUids(uidList);
	}

	// ---- getSearchJob
	@Override
	protected SearchJob getSearchJob(final Display display) {
		return new AccountDatabaseSearchJobImpl(this, display);
	}
	// ---- getSearchJob

}
