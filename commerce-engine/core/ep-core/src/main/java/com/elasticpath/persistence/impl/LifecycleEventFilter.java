/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.persistence.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.openjpa.enhance.PersistenceCapable;

import com.elasticpath.base.exception.EpServiceException;

/**
 * Filter for duplicate lifecycle events.
 *
 * There are at least two ways that OpenJPA duplicates lifecycle events:
 *
 * 1) Incremental Flush
 * When entities are persisted to OpenJPA within a transaction, they are not flushed right away. OpenJPA holds them in memory
 * until either the transaction is committed, or a select query is done on a table that could be affected by entities in memory.
 * In this scenario, OpenJPA does an incremental flush to write the objects to the database before doing the select query.
 * Each time this happens for an entity that was updated, OpenJPA fires BEFORE_UPDATE lifecycle events, meaning that the same
 * entity within a transaction could trigger multiple BEFORE_UPDATE events (one each time the select query is run).
 *
 * 2) AFTER_ATTACH vs BEFORE_UPDATE
 * We have noticed that different EP services fire different events when entities are updated. For example, data population and Import/Export
 * only appear to fire the BEFORE_UPDATE event. Commerce Manager fires both AFTER_ATTACH and BEFORE_UPDATE. Other services appear to only
 * fire AFTER_ATTACH. Both of these events align to an entity update.
 *
 * The isDuplicate method in this class keeps a HashSet of past events and returns true if a previous event with the same details is found in the
 * queue. The HashSet is reset on each new transaction when the beginTransaction method is invoked.
 */
public class LifecycleEventFilter {

	// Set of previous events wrapped in a ThreadLocal since each thread could be opening separate transactions.
	private final ThreadLocal<Set<LifecycleEventIdentifier>> lifecycleEventsTL = new ThreadLocal<>();

	/**
	 * Invoke when a new transaction is started to create a new lifecycle events cache.
	 */
	public void beginTransaction() {
		lifecycleEventsTL.set(new HashSet<>());
	}

	/**
	 * Invoke when a transaction is completed or rolled back to erase the lifecycle events cache.
	 */
	public void endTransaction() {
		lifecycleEventsTL.set(null);
	}

	/**
	 * Determines if the lifecycle event has already been received.
	 *
	 * @param entityChangeType the type of change detected
	 * @param entityClass the entity class
	 * @param entityGuid the entity object guid
	 * @return true if the lifecycle event has been seen before
	 */
	public boolean isDuplicate(final EventActionEnum entityChangeType, final Class<PersistenceCapable> entityClass, final String entityGuid) {
		Set<LifecycleEventIdentifier> previousEvents = lifecycleEventsTL.get();
		if (previousEvents == null) {
			throw new EpServiceException("Attempt to call isDuplicate before transaction was started.");
		}
		LifecycleEventIdentifier lifecycleEventIdentifier = new LifecycleEventIdentifier(entityChangeType.getEventActionGroup(),
				entityClass, entityGuid);
		if (previousEvents.contains(lifecycleEventIdentifier)) {
			return true;
		}
		previousEvents.add(lifecycleEventIdentifier);
		return false;
	}
}
