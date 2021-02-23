/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.builders.impl;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.search.index.pipeline.IndexingPipeline;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.solr.SolrIndexConstants;

/**
 * An implementation of <code>IndexBuilder</code> to create index for customer.
 */
public class CustomerIndexBuilder extends AbstractIndexBuilder {

	private static final Date OLDEST_POSSIBLE_CUSTOMER_MODIFICATION_DATE = getDate(2015, 1, 1);

	private static final int INDEXABLE_UIDS_MAX_RESULT = 1000;

	private CustomerService customerService;

	private IndexingPipeline<Collection<Long>> customerIndexingPipeline;


	private static Date getDate(final int year, final int month, final int date) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, date);
		return calendar.getTime();
	}

	/**
	 * Returns index build service name.
	 * 
	 * @return index build service name
	 */
	@Override
	public String getName() {
		return SolrIndexConstants.CUSTOMER_SOLR_CORE;
	}

	/**
	 * Retrieve deleted UIDs.
	 * 
	 * @param lastBuildDate the last build date
	 * @return deleted UIDs.
	 */
	@Override
	public List<Long> findDeletedUids(final Date lastBuildDate) {
		return Collections.emptyList();
	}

	/**
	 * Retrieve added or modified UIDs since last build.
	 * 
	 * @param lastBuildDate the last build date
	 * @return added or modified UIDs
	 */
	@Override
	public List<Long> findAddedOrModifiedUids(final Date lastBuildDate) {
		return customerService.findIndexableUidsPaginated(lastBuildDate, 0, Integer.MAX_VALUE);
	}

	/**
	 * Retrieves a paginated list of all searchable <code>Customer</code> uids.
	 *
	 * @param page the current page of the list to retrieve
	 * @return a paginated list of indexable <code>Customer</code> uids whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findIndexableUidsPaginated(final int page) {
		final int firstResult = INDEXABLE_UIDS_MAX_RESULT * page;
		return customerService.findIndexableUidsPaginated(OLDEST_POSSIBLE_CUSTOMER_MODIFICATION_DATE, firstResult, INDEXABLE_UIDS_MAX_RESULT - 1);
	}

	/**
	 * To be used with findIndexableUidsPaginated method and inform use of pagination.
	 *
	 * @return false as default, otherwise override it to use pagination.
	 */
	@Override
	public boolean canPaginate() {
		return true;
	}

	/**
	 * Get the customer from the persistence layer.
	 * 
	 * @param uid the UID for the customer
	 * @return the customer with the given UID, or null if one cannot be found
	 */
	protected Customer getCustomer(final long uid) {
		return customerService.get(uid);
	}

	/**
	 * Publishes updates to the Solr server for the specified {@link Customer} uids.
	 * 
	 * @param uids the {@link Customer} uids to publish.
	 */

	@Override
	public void submit(final Collection<Long> uids) {
		customerIndexingPipeline.start(uids);
	}

	/**
	 * Sets the customer service.
	 * 
	 * @param customerService the customer service
	 */
	public void setCustomerService(final CustomerService customerService) {
		this.customerService = customerService;
	}

	/**
	 * Return the index type this class builds.
	 * 
	 * @return the index type this class builds.
	 */
	@Override
	public IndexType getIndexType() {
		return IndexType.CUSTOMER;
	}

	@Override
	public Collection<Long> findUidsByNotification(final IndexNotification notification) {
		throw new UnsupportedOperationException("not supported");
	}

	/**
	 * @param customerIndexingPipeline the customerIndexingPipeline to set
	 */
	public void setCustomerIndexingPipeline(final IndexingPipeline<Collection<Long>> customerIndexingPipeline) {
		this.customerIndexingPipeline = customerIndexingPipeline;
	}

	/**
	 * @return the customerIndexingPipeline
	 */
	public IndexingPipeline<Collection<Long>> getCustomerIndexingPipeline() {
		return customerIndexingPipeline;
	}
}
