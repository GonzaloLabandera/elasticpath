/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.tools.sync.job.impl;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.tools.sync.job.TransactionJob;
import com.elasticpath.tools.sync.job.TransactionJobUnit;
import com.elasticpath.tools.sync.processing.AbstractAggregationObjectProvider;
import com.elasticpath.tools.sync.processing.SerializableObject;
import com.elasticpath.tools.sync.processing.SingleObjectProvider;

/**
 * Transaction job's default implementation.
 */
public class TransactionJobImpl extends AbstractAggregationObjectProvider<SerializableObject> implements TransactionJob {
	
	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 50000000001L;

	private final List<TransactionJobUnit> transactionJobUnits = new ArrayList<>();

	/**
	 * @return the transactionJobUnit
	 */
	@Override
	public List<TransactionJobUnit> getTransactionJobUnits() {
		return transactionJobUnits;
	}

	@Override
	public int getTransactionJobUnitsNumber() {
		return transactionJobUnits.size();
	}
	

	/**
	 *
	 * @param transactionJobUnit the job unit to add
	 */
	@Override
	public void addTransactionJobUnit(final TransactionJobUnit transactionJobUnit) {
		this.transactionJobUnits.add(transactionJobUnit);
	}

	/**
	 * Builds a list of object providers starting from the {@link TransactionJob} and
	 * continuing with its units and their job entries.
	 *  
	 * @return all the object providers
	 */
	@Override
	protected List<Iterable<SerializableObject>> getAllProviders() {
		List<Iterable<SerializableObject>> result = new ArrayList<>();
		// add the transaction job to be the first object in the list of objects
		result.add(new SingleObjectProvider<>(this));
		for (TransactionJobUnit unit : transactionJobUnits) {
			// add the unit as a serializable object 
			result.add(new SingleObjectProvider<>(unit));
			// and then as an object provider
			result.add(unit);
		}
		return result;
	}
	
}
