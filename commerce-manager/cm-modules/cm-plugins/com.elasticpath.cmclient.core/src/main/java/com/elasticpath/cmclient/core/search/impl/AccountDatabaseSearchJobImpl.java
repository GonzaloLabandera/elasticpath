/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.cmclient.core.search.impl;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import com.elasticpath.cmclient.core.BeanLocator;
import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.CorePlugin;
import com.elasticpath.cmclient.core.helpers.SearchItemsLocator;
import com.elasticpath.cmclient.core.search.SearchJob;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.search.query.AccountSearchCriteria;

/**
 * Search for account using database queries.
 */
public class AccountDatabaseSearchJobImpl extends AbstractSearchJobImpl implements SearchJob {

	private static final Logger LOG = LogManager.getLogger(AccountDatabaseSearchJobImpl.class);

	private static final int COUNT_UNITS_WORK = 2;

	private static final int SEARCH_UNITS_WORK = 5;

	private static final int FIRE_ITEMS_WORK = 1;

	private static final int WORK_UNITS = COUNT_UNITS_WORK + SEARCH_UNITS_WORK + FIRE_ITEMS_WORK;

	private final CustomerService customerService;
	private final Display display;
	private final String jobName;

	/**
	 * Constructor that takes a search items locator.
	 *
	 * @param locator locator for items
	 * @param display the display
	 */
	public AccountDatabaseSearchJobImpl(final SearchItemsLocator<Customer> locator, final Display display) {
		super(locator);
		this.display = display;
		customerService = BeanLocator.getSingletonBean(ContextIdNames.CUSTOMER_SERVICE, CustomerService.class);
		jobName = CoreMessages.get().SearchProgress_StatusBarMessage_StartSearch;
	}

	@Override
	protected IStatus run(final IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		try {

			monitor.beginTask(jobName, WORK_UNITS);
			checkMonitorCancelled(monitor);
			
			final int startIndex = getStartIndexQueue().poll();
			AccountSearchCriteria searchCriteria;
			synchronized (getSearchCriteriaQueue()) {
				searchCriteria = (AccountSearchCriteria) getCriteria();
			}
			if (searchCriteria.getLocale() == null) {
				display.syncExec(() -> searchCriteria.setLocale(CorePlugin.getDefault().getDefaultLocale()));
			}

			long totalItemsCount = customerService.getAccountCountBySearchCriteria(searchCriteria);
			monitor.worked(COUNT_UNITS_WORK);
			
			if (LOG.isDebugEnabled()) {
				LOG.debug("Searching for items"); //$NON-NLS-1$
			}
			List items = customerService.findAccountsBySearchCriteria(searchCriteria, startIndex, getPagination());
			monitor.worked(SEARCH_UNITS_WORK);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Searching complete. Found " + items.size() + " items."); //$NON-NLS-1$ //$NON-NLS-2$
			}

			display.asyncExec(() -> getLocator().fireItemsUpdated(items, startIndex, (int) totalItemsCount));
			monitor.worked(FIRE_ITEMS_WORK);
		} finally {
			monitor.done();
		}
		return result;
	}


}
