/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.core.messaging.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.exception.NoSuchEventTypeException;
import com.elasticpath.messaging.spi.EventTypeLookup;

/**
 * An enum representing DOMAIN-based {@link EventType}s.
 */
public class DomainEventType extends AbstractExtensibleEnum<DomainEventType> implements EventType {

	private static final long serialVersionUID = -2185999554178045228L;

	/**
	 * Ordinal constant for SKU_OPTION_CREATED.
	 */
	public static final int SKU_OPTION_CREATED_ORDINAL = 0;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType SKU_OPTION_CREATED = new DomainEventType(SKU_OPTION_CREATED_ORDINAL, "SKU_OPTION_CREATED");

	/**
	 * Ordinal constant for SKU_OPTION_UPDATED.
	 */
	public static final int SKU_OPTION_UPDATED_ORDINAL = 1;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType SKU_OPTION_UPDATED = new DomainEventType(SKU_OPTION_UPDATED_ORDINAL, "SKU_OPTION_UPDATED");

	/**
	 * Ordinal constant for SKU_OPTION_DELETED.
	 */
	public static final int SKU_OPTION_DELETED_ORDINAL = 2;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType SKU_OPTION_DELETED = new DomainEventType(SKU_OPTION_DELETED_ORDINAL, "SKU_OPTION_DELETED");

	/**
	 * Ordinal constant for BRAND_CREATED.
	 */
	public static final int BRAND_CREATED_ORDINAL = 3;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType BRAND_CREATED = new DomainEventType(BRAND_CREATED_ORDINAL, "BRAND_CREATED");

	/**
	 * Ordinal constant for BRAND_UPDATED.
	 */
	public static final int BRAND_UPDATED_ORDINAL = 4;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType BRAND_UPDATED = new DomainEventType(BRAND_UPDATED_ORDINAL, "BRAND_UPDATED");

	/**
	 * Ordinal constant for BRAND_DELETED.
	 */
	public static final int BRAND_DELETED_ORDINAL = 5;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType BRAND_DELETED = new DomainEventType(BRAND_DELETED_ORDINAL, "BRAND_DELETED");
	/**
	 * Ordinal constant for MODIFIER_GROUP_CREATED.
	 */
	public static final int MODIFIER_GROUP_CREATED_ORDINAL = 6;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType MODIFIER_GROUP_CREATED = new DomainEventType(MODIFIER_GROUP_CREATED_ORDINAL,
			"MODIFIER_GROUP_CREATED");

	/**
	 * Ordinal constant for MODIFIER_GROUP_UPDATED.
	 */
	public static final int MODIFIER_GROUP_UPDATED_ORDINAL = 7;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType MODIFIER_GROUP_UPDATED = new DomainEventType(MODIFIER_GROUP_UPDATED_ORDINAL,
			"MODIFIER_GROUP_UPDATED");

	/**
	 * Ordinal constant for MODIFIER_GROUP_DELETED.
	 */
	public static final int MODIFIER_GROUP_DELETED_ORDINAL = 8;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType MODIFIER_GROUP_DELETED = new DomainEventType(MODIFIER_GROUP_DELETED_ORDINAL,
			"MODIFIER_GROUP_DELETED");
	/**
	 * Ordinal constant for ATTRIBUTE_CREATED.
	 */
	public static final int ATTRIBUTE_CREATED_ORDINAL = 9;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType ATTRIBUTE_CREATED = new DomainEventType(ATTRIBUTE_CREATED_ORDINAL, "ATTRIBUTE_CREATED");

	/**
	 * Ordinal constant for ATTRIBUTE_UPDATED.
	 */
	public static final int ATTRIBUTE_UPDATED_ORDINAL = 10;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType ATTRIBUTE_UPDATED = new DomainEventType(ATTRIBUTE_UPDATED_ORDINAL, "ATTRIBUTE_UPDATED");

	/**
	 * Ordinal constant for ATTRIBUTE_DELETED.
	 */
	public static final int ATTRIBUTE_DELETED_ORDINAL = 11;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType ATTRIBUTE_DELETED = new DomainEventType(ATTRIBUTE_DELETED_ORDINAL, "ATTRIBUTE_DELETED");

	/**
	 * Ordinal constant for PRODUCT_CREATED.
	 */
	public static final int PRODUCT_CREATED_ORDINAL = 12;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType PRODUCT_CREATED = new DomainEventType(PRODUCT_CREATED_ORDINAL, "PRODUCT_CREATED");

	/**
	 * Ordinal constant for PRODUCT_UPDATED.
	 */
	public static final int PRODUCT_UPDATED_ORDINAL = 13;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType PRODUCT_UPDATED = new DomainEventType(PRODUCT_UPDATED_ORDINAL, "PRODUCT_UPDATED");

	/**
	 * Ordinal constant for PRODUCT_DELETED.
	 */
	public static final int PRODUCT_DELETED_ORDINAL = 14;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType PRODUCT_DELETED = new DomainEventType(PRODUCT_DELETED_ORDINAL, "PRODUCT_DELETED");

	/**
	 * Ordinal constant for CATEGORY_CREATED.
	 */
	public static final int CATEGORY_CREATED_ORDINAL = 15;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_CREATED = new DomainEventType(CATEGORY_CREATED_ORDINAL, "CATEGORY_CREATED");

	/**
	 * Ordinal constant for CATEGORY_UPDATED.
	 */
	public static final int CATEGORY_UPDATED_ORDINAL = 16;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_UPDATED = new DomainEventType(CATEGORY_UPDATED_ORDINAL, "CATEGORY_UPDATED");

	/**
	 * Ordinal constant for CATEGORY_DELETED.
	 */
	public static final int CATEGORY_DELETED_ORDINAL = 17;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_DELETED = new DomainEventType(CATEGORY_DELETED_ORDINAL, "CATEGORY_DELETED");

	/**
	 * Ordinal constant for CATEGORY_LINK_CREATED.
	 */
	public static final int CATEGORY_LINK_CREATED_ORDINAL = 18;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_LINK_CREATED = new DomainEventType(CATEGORY_LINK_CREATED_ORDINAL, "CATEGORY_LINK_CREATED");

	/**
	 * Ordinal constant for CATEGORY_LINK_DELETED.
	 */
	public static final int CATEGORY_LINK_DELETED_ORDINAL = 19;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_LINK_DELETED = new DomainEventType(CATEGORY_LINK_DELETED_ORDINAL, "CATEGORY_LINK_DELETED");

	/**
	 * Ordinal constant for CATEGORY_LINK_UPDATED.
	 */
	public static final int CATEGORY_LINK_UPDATED_ORDINAL = 20;

	/**
	 * Signals that a domain has registered.
	 */
	public static final DomainEventType CATEGORY_LINK_UPDATED = new DomainEventType(CATEGORY_LINK_UPDATED_ORDINAL, "CATEGORY_LINK_UPDATED");


	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected DomainEventType(final int ordinal, final String name) {
		super(ordinal, name, DomainEventType.class);
	}

	/**
	 * Find the enum value with the specified name.
	 *
	 * @param name the name
	 * @return the enum value
	 */
	public static DomainEventType valueOf(final String name) {
		return valueOf(name, DomainEventType.class);
	}

	@Override
	protected Class<DomainEventType> getEnumType() {
		return DomainEventType.class;
	}

	@JsonIgnore
	@Override
	public int getOrdinal() {
		return super.getOrdinal();
	}

	/**
	 * DomainEventType implementation of lookup interface.
	 */
	public static class DomainEventTypeLookup implements EventTypeLookup<DomainEventType> {

		@Override
		public DomainEventType lookup(final String name) {
			try {
				return DomainEventType.valueOf(name);
			} catch (IllegalArgumentException e) {
				throw new NoSuchEventTypeException(e);
			}
		}

	}
}
