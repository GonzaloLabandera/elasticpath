/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.core.messaging.catalog;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;
import com.elasticpath.messaging.spi.EventTypeLookup;

/**
 * An enum representing CATALOG-based {@link EventType}s.
 */
public class CatalogEventType extends AbstractExtensibleEnum<CatalogEventType> implements EventType {

	private static final long serialVersionUID = -2185999554178045228L;

	/**
	 * Ordinal constant for OPTIONS_UPDATED.
	 */
	public static final int OPTIONS_UPDATED_ORDINAL = 0;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType OPTIONS_UPDATED = new CatalogEventType(OPTIONS_UPDATED_ORDINAL, "OPTIONS_UPDATED");

	/**
	 * Ordinal constant for BRANDS_UPDATED.
	 */
	public static final int BRANDS_UPDATED_ORDINAL = 1;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType BRANDS_UPDATED = new CatalogEventType(BRANDS_UPDATED_ORDINAL, "BRANDS_UPDATED");

	/**
	 * Ordinal constant for FIELD_METADATA_UPDATED.
	 */
	public static final int FIELD_METADATA_UPDATED_ORDINAL = 2;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType FIELD_METADATA_UPDATED = new CatalogEventType(FIELD_METADATA_UPDATED_ORDINAL, "FIELD_METADATA_UPDATED");

	/**
	 * Ordinal constant for ATTRIBUTES_UPDATED.
	 */
	public static final int ATTRIBUTES_UPDATED_ORDINAL = 3;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType ATTRIBUTES_UPDATED = new CatalogEventType(ATTRIBUTES_UPDATED_ORDINAL, "ATTRIBUTES_UPDATED");

	/**
	 * Ordinal constant for OFFERS_UPDATED.
	 */
	public static final int OFFERS_UPDATED_ORDINAL = 4;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType OFFERS_UPDATED = new CatalogEventType(OFFERS_UPDATED_ORDINAL, "OFFERS_UPDATED");

	/**
	 * Ordinal constant for CATEGORIES_UPDATED.
	 */
	public static final int CATEGORIES_UPDATED_ORDINAL = 5;

	/**
	 * Signals that a catalog has registered.
	 */
	public static final CatalogEventType CATEGORIES_UPDATED = new CatalogEventType(CATEGORIES_UPDATED_ORDINAL, "CATEGORIES_UPDATED");

	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected CatalogEventType(final int ordinal, final String name) {
		super(ordinal, name, CatalogEventType.class);
	}

	/**
	 * Find the enum value with the specified name.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static CatalogEventType valueOf(final String name) {
		return valueOf(name, CatalogEventType.class);
	}

	@Override
	protected Class<CatalogEventType> getEnumType() {
		return CatalogEventType.class;
	}

	@JsonIgnore
	@Override
	public int getOrdinal() {
		return super.getOrdinal();
	}

	/**
	 * CatalogEventType implementation of lookup interface.
	 */
	public static class CatalogEventTypeLookup implements EventTypeLookup<CatalogEventType> {
		@Override
		public CatalogEventType lookup(final String name) {
			try {
				return CatalogEventType.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new NoSuchEventTypeException(e);
			}
		}
	}
}
