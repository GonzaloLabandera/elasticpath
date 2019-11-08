/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.message;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;
import com.elasticpath.messaging.spi.EventTypeLookup;

/**
 * An enum representing CATALOG-BATCH-based {@link EventType}s.
 */
public class CatalogBatchEventType extends AbstractExtensibleEnum<CatalogBatchEventType> implements EventType {

	private static final long serialVersionUID = -2185999554178045228L;

	/**
	 * Ordinal constant for START_JOB.
	 */
	public static final int START_JOB_ORDINAL = 0;

	/**
	 * Signals for batch job starting.
	 */
	public static final CatalogBatchEventType START_JOB = new CatalogBatchEventType(START_JOB_ORDINAL, "START_JOB");

	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected CatalogBatchEventType(final int ordinal, final String name) {
		super(ordinal, name, CatalogBatchEventType.class);
	}

	/**
	 * Find the enum value with the specified name.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static CatalogBatchEventType valueOf(final String name) {
		return valueOf(name, CatalogBatchEventType.class);
	}

	@Override
	protected Class<CatalogBatchEventType> getEnumType() {
		return CatalogBatchEventType.class;
	}

	/**
	 * CatalogBatchEventType implementation of lookup interface.
	 */
	public static class CatalogBatchEventTypeLookup implements EventTypeLookup<CatalogBatchEventType> {

		@Override
		public CatalogBatchEventType lookup(final String name) {
			try {
				return CatalogBatchEventType.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new NoSuchEventTypeException(e);
			}
		}
	}

}
