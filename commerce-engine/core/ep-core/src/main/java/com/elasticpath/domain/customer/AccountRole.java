/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.domain.customer;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Account role enumeration.
 */
public class AccountRole extends AbstractExtensibleEnum<AccountRole> {

	private static final long serialVersionUID = 1L;

	/**
	 * The <code>AccountRole</code> ordinal for Basic role.
	 */
	public static final int BASIC_ORDINAL = 1;

	/**
	 * The <code>AccountRole</code> instance for Basic role.
	 */
	public static final AccountRole BASIC = new AccountRole(BASIC_ORDINAL, "BASIC");

	/**
	 * The <code>AccountRole</code> ordinal for Buyers.
	 */
	public static final int BUYER_ORDINAL = 2;

	/**
	 * The <code>AccountRole</code> instance for Buyers.
	 */
	public static final AccountRole BUYER = new AccountRole(BUYER_ORDINAL, "BUYER");

	/**
	 * The <code>AccountRole</code> ordinal for Limited Buyers.
	 */
	public static final int LIMITED_BUYER_ORDINAL = 3;

	/**
	 * The <code>AccountRole</code> instance for Limited Buyers.
	 */
	public static final AccountRole LIMITED_BUYER = new AccountRole(LIMITED_BUYER_ORDINAL, "LIMITED_BUYER");

	/**
	 * The <code>AccountRole</code> ordinal for Buyer Admins.
	 */
	public static final int BUYER_ADMIN_ORDINAL = 4;

	/**
	 * The <code>AccountRole</code> instance for Buyer Admins.
	 */
	public static final AccountRole BUYER_ADMIN = new AccountRole(BUYER_ADMIN_ORDINAL, "BUYER_ADMIN");

	/**
	 * Constructor for account role.
	 *
	 * @param ordinal ordinal
	 * @param name name
	 */
	protected AccountRole(final int ordinal, final String name) {
		super(ordinal, name, AccountRole.class);
	}

	/**
	 * Find all enum values for a particular enum type.
	 * @return the enum values
	 */
	public static Collection<AccountRole> values() {
		return values(AccountRole.class);
	}

	@Override
	protected Class<AccountRole> getEnumType() {
		return AccountRole.class;
	}

	/**
	 * Get the enum value corresponding to the given name.
	 *
	 * @param name the name
	 * @return the account role
	 */
	public static AccountRole valueOf(final String name) {
		return valueOf(name, AccountRole.class);
	}
}