/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.catalogview.impl;

import com.elasticpath.commons.util.extenum.ExtensibleEnum;

/**
 * An implementation of {@link ExtensibleEnum} for store available rules.
 */
public class StoreAvailabilityRule implements ExtensibleEnum {

	private static final long serialVersionUID = -2185999554178045228L;

	private final int ordinal;

	private final String name;

	/**
	 * Ordinal constant for ALWAYS available rule.
	 */
	public static final int ALWAYS_ORDINAL = 0;

	/**
	 * ALWAYS store available rule.
	 */
	public static final StoreAvailabilityRule ALWAYS = new StoreAvailabilityRule(ALWAYS_ORDINAL, "ALWAYS");

	/**
	 * Ordinal constant for PRE_ORDER available rule.
	 */
	public static final int PRE_ORDER_ORDINAL = 1;

	/**
	 * PRE_ORDER store available rule.
	 */
	public static final StoreAvailabilityRule PRE_ORDER = new StoreAvailabilityRule(PRE_ORDER_ORDINAL, "PRE_ORDER");

	/**
	 * Ordinal constant for HAS_STOCK available rule.
	 */
	public static final int HAS_STOCK_ORDINAL = 2;

	/**
	 * HAS_STOCK store available rule.
	 */
	public static final StoreAvailabilityRule HAS_STOCK = new StoreAvailabilityRule(HAS_STOCK_ORDINAL, "HAS_STOCK");

	/**
	 * Ordinal constant for BACK_ORDER available rule.
	 */
	public static final int BACK_ORDER_ORDINAL = 3;

	/**
	 * BACK_ORDER store available rule.
	 */
	public static final StoreAvailabilityRule BACK_ORDER = new StoreAvailabilityRule(BACK_ORDER_ORDINAL, "BACK_ORDER");

	/**
	 * Constructor.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 */
	public StoreAvailabilityRule(final int ordinal, final String name) {
		this.ordinal = ordinal;
		this.name = name;
	}

	@Override
	public int getOrdinal() {
		return ordinal;
	}

	@Override
	public String getName() {
		return name;
	}

}
