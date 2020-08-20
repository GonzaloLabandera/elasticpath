/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.openjpa.impl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.Transaction;

/**
 * The JPA implementation of a transaction in ElasticPath. It is a wrap of <code>javax.persistence.EntityTransaction</code>.
 *
 * Enable TRACE level logging to see transaction timing information.
 */
public class JpaTransactionImpl implements Transaction {

    private static final Logger LOG = Logger.getLogger(JpaTransactionImpl.class);

	private final PlatformTransactionManager txManager;
	private final TransactionStatus txStatus;

	// tx timing debug information
	private final long transactionStartTime = System.currentTimeMillis();
	private long transactionDuration = -1L;
	private long commitOrRollbackDuration = -1L;

	/**
	 * Create an EP transaction based on the given <code>EntityTransaction</code>.
	 *
	 * @param txManager the <code>PlatformTransactionManager</code>
	 * @param txStatus the status of the transaction
	 */
	public JpaTransactionImpl(final PlatformTransactionManager txManager, final TransactionStatus txStatus) {
		this.txManager = txManager;
		this.txStatus = txStatus;
	}

	/**
	 * Commit changes.
	 *
	 * @throws EpPersistenceException in case of any error
	 */
	@Override
	public void commit() throws EpPersistenceException {
	    markTransactionWorkCompleted("commit");

	    this.txManager.commit(txStatus);

	    markTransactionFinalized("commit");
	}

	/**
	 * Rollback changes.
	 *
	 * @throws EpPersistenceException in case of any error
	 */
	@Override
	public void rollback() throws EpPersistenceException {
	    markTransactionWorkCompleted("rollback");

	    this.txManager.rollback(txStatus);

	    markTransactionFinalized("rollback");
	}

	@Override
	public boolean isRollbackOnly() {
	    return txStatus.isRollbackOnly();
	}

    private void markTransactionFinalized(final String action) {
        if (LOG.isTraceEnabled()) {
            this.commitOrRollbackDuration = System.currentTimeMillis() - (transactionStartTime + transactionDuration);
            LOG.trace("Transaction " + action + " completed. " + toString());
        }
    }

    private void markTransactionWorkCompleted(final String action) {
        if (LOG.isTraceEnabled()) {
            this.transactionDuration = System.currentTimeMillis() - transactionStartTime;
            LOG.trace("Transaction " + action + " requested. " + toString());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("txManager", txManager)
                .append("txStatus", txStatus)
                .append("txAgeMs",  System.currentTimeMillis() - transactionStartTime)
                .append("mainTransactionDurationMs", transactionDuration == -1 ? "n/a" : transactionDuration)
                .append("commitOrRollbackDurationMs", commitOrRollbackDuration == -1 ? "n/a" : commitOrRollbackDuration)
                .toString();
    }
}
