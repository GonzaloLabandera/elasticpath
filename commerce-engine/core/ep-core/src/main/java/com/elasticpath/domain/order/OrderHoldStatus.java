/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.domain.order;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * An enum representing statuses of {@link OrderHold} that are available in the platform.
 */
public class OrderHoldStatus extends AbstractExtensibleEnum<OrderHoldStatus> {

	private static final long serialVersionUID = 5000000001L;

	/**
	 * Ordinal constant for active.
	 */
	public static final int ACTIVE_ORDINAL = 0;

	/**
	 * Represents the active status of order hold.
	 */
	public static final OrderHoldStatus ACTIVE = new OrderHoldStatus(ACTIVE_ORDINAL, "ACTIVE");

	/**
	 * Ordinal constant for resolved.
	 */
	public static final int RESOLVED_ORDINAL = 1;

	/**
	 * Represents the resolved status of order hold.
	 */
	public static final OrderHoldStatus RESOLVED = new OrderHoldStatus(RESOLVED_ORDINAL, "RESOLVED");

	/**
	 * Ordinal constant for unresolvable.
	 */
	public static final int UNRESOLVABLE_ORDINAL = 2;

	/**
	 * Represents the unresolvable status of order hold.
	 */
	public static final OrderHoldStatus UNRESOLVABLE = new OrderHoldStatus(UNRESOLVABLE_ORDINAL, "UNRESOLVABLE");

	/**
	 * Ordinal constant for resolve pending.
	 */
	public static final int RESOLVE_PENDING_ORDINAL = 3;

	/**
	 * Represents the resolve pending status of order hold.
	 */
	public static final OrderHoldStatus RESOLVE_PENDING = new OrderHoldStatus(RESOLVE_PENDING_ORDINAL, "RESOLVE_PENDING");

	/**
	 * Ordinal constant for unresolvable pending.
	 */
	public static final int UNRESOLVABLE_PENDING_ORDINAL = 4;

	/**
	 * Represents the unresolvable pending status of order hold.
	 */
	public static final OrderHoldStatus UNRESOLVABLE_PENDING = new OrderHoldStatus(UNRESOLVABLE_PENDING_ORDINAL, "UNRESOLVABLE_PENDING");

	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	protected OrderHoldStatus(final int ordinal, final String name) {
		super(ordinal, name, OrderHoldStatus.class);
	}

	@Override
	protected Class<OrderHoldStatus> getEnumType() {
		return OrderHoldStatus.class;
	}

	/**
	 * Find all enum values for a particular enum type.
	 *
	 * @return the enum values
	 */
	public static Collection<OrderHoldStatus> values() {
		return values(OrderHoldStatus.class);
	}

	/**
	 * Find the enum value with the specified name.
	 *
	 * @param name the name of the enum
	 * @return the corresponding value.
	 */
	public static OrderHoldStatus valueOf(final String name) {
		return valueOf(name, OrderHoldStatus.class);
	}
}