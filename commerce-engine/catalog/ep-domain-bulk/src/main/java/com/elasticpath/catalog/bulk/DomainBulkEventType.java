/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;
import com.elasticpath.messaging.spi.EventTypeLookup;

/**
 * An enum representing Domain bulk-based {@link EventType}s.
 */
public class DomainBulkEventType extends AbstractExtensibleEnum<DomainBulkEventType> implements EventType {

	private static final long serialVersionUID = -2185999554178045228L;

	/**
	 * Ordinal constant for BRAND_BULK_UPDATE.
	 */
	public static final int BRAND_BULK_UPDATE_ORDINAL = 0;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType BRAND_BULK_UPDATE = new DomainBulkEventType(BRAND_BULK_UPDATE_ORDINAL, "BRAND_BULK_UPDATE");

	/**
	 * Ordinal constant for OPTION_BULK_UPDATE_ORDINAL.
	 */
	public static final int OPTION_BULK_UPDATE_ORDINAL = 1;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType OPTION_BULK_UPDATE = new DomainBulkEventType(OPTION_BULK_UPDATE_ORDINAL, "OPTION_BULK_UPDATE");

	/**
	 * Ordinal constant for OFFER_BULK_UPDATE.
	 */
	public static final int OFFER_BULK_UPDATE_ORDINAL = 2;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType OFFER_BULK_UPDATE = new DomainBulkEventType(OFFER_BULK_UPDATE_ORDINAL, "OFFER_BULK_UPDATE");

	/**
	 * Ordinal constant for ATTRIBUTE_BULK_UPDATE_ORDINAL.
	 */
	public static final int ATTRIBUTE_BULK_UPDATE_ORDINAL = 3;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType ATTRIBUTE_BULK_UPDATE = new DomainBulkEventType(ATTRIBUTE_BULK_UPDATE_ORDINAL, "ATTRIBUTE_BULK_UPDATE");

	/**
	 * Ordinal constant for ATTRIBUTE_CATEGORY_BULK_UPDATE_ORDINAL.
	 */
	public static final int ATTRIBUTE_CATEGORY_BULK_UPDATE_ORDINAL = 4;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType ATTRIBUTE_CATEGORY_BULK_UPDATE = new DomainBulkEventType(ATTRIBUTE_CATEGORY_BULK_UPDATE_ORDINAL,
			"ATTRIBUTE_CATEGORY_BULK_UPDATE");

	/**
	 * Ordinal constant for ATTRIBUTE_SKU_BULK_UPDATE_ORDINAL.
	 */
	public static final int ATTRIBUTE_SKU_BULK_UPDATE_ORDINAL = 5;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType ATTRIBUTE_SKU_BULK_UPDATE = new DomainBulkEventType(ATTRIBUTE_SKU_BULK_UPDATE_ORDINAL,
			"ATTRIBUTE_SKU_BULK_UPDATE");

	/**
	 * Ordinal constant for CATEGORY_BULK_UPDATE.
	 */
	public static final int CATEGORY_BULK_UPDATE_ORDINAL = 6;

	/**
	 * Signals for batch job starting.
	 */
	public static final DomainBulkEventType CATEGORY_BULK_UPDATE = new DomainBulkEventType(CATEGORY_BULK_UPDATE_ORDINAL, "CATEGORY_BULK_UPDATE");

	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected DomainBulkEventType(final int ordinal, final String name) {
		super(ordinal, name, DomainBulkEventType.class);
	}

	/**
	 * Find the enum value with the specified name.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static DomainBulkEventType valueOf(final String name) {
		return valueOf(name, DomainBulkEventType.class);
	}

	@Override
	protected Class<DomainBulkEventType> getEnumType() {
		return DomainBulkEventType.class;
	}

	/**
	 * DomainBulkEventType implementation of lookup interface.
	 */
	public static class DomainBulkEventTypeLookup implements EventTypeLookup<DomainBulkEventType> {

		@Override
		public DomainBulkEventType lookup(final String name) {
			try {
				return DomainBulkEventType.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new NoSuchEventTypeException(e);
			}
		}
	}

}
