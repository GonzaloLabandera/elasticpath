/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tools.sync.job.transaction.impl;

import java.util.Iterator;
import java.util.List;

import com.elasticpath.tools.sync.job.descriptor.TransactionJobDescriptorEntry;

/**
 * Interface for transaction list iterator.
 */
public interface TransactionListIterator extends Iterator<List<TransactionJobDescriptorEntry>> {

	/**
	 * Initializes transaction list iterator.
	 *
	 * @param jobDescriptorEntriesList the job descriptor entries list
	 * @param params params for iterator
	 */
	void initialize(List<TransactionJobDescriptorEntry> jobDescriptorEntriesList, List<String> params);

}