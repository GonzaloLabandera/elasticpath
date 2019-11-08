/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.catalog.batch;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

/**
 * Spy transaction manager.
 */
public class SpyTransactionManager implements PlatformTransactionManager {

	private boolean committed, rolledBack;
	private final PlatformTransactionManager fallbackTransactionManager;

	/**
	 * Constructor.
	 *
	 * @param fallbackTransactionManager fallback transactionManager
	 */
	public SpyTransactionManager(final PlatformTransactionManager fallbackTransactionManager) {
		this.fallbackTransactionManager = fallbackTransactionManager;
	}

	@Override
	public TransactionStatus getTransaction(final TransactionDefinition definition) throws TransactionException {
		return fallbackTransactionManager.getTransaction(definition);
	}

	@Override
	public void commit(final TransactionStatus status) throws TransactionException {
		fallbackTransactionManager.commit(status);
		committed = true;

	}

	@Override
	public void rollback(final TransactionStatus status) throws TransactionException {
		fallbackTransactionManager.rollback(status);
		rolledBack = true;
	}

	public boolean isCommitted() {
		return committed;
	}

	public boolean isRolledBack() {
		return rolledBack;
	}

	/**
	 * Reset counters.
	 */
	public void reset() {
		committed = false;
		rolledBack = false;
	}
}
