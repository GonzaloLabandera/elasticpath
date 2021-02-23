/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.customer;

import java.util.Collection;

import com.elasticpath.commons.util.extenum.AbstractExtensibleEnum;

/**
 * Customer Types.
 */
public class CustomerType extends AbstractExtensibleEnum<CustomerType> {

	private static final long serialVersionUID = 1L;

	/**
	 * The CustomerType ordinal for single session user.
	 */
	public static final int SINGLE_SESSION_USER_ORDINAL = 1;
	/**
	 * The CustomerType instance for single session user.
	 */
	public static final CustomerType SINGLE_SESSION_USER = new CustomerType(SINGLE_SESSION_USER_ORDINAL, "SINGLE_SESSION_USER", CustomerType.class);

	/**
	 * The CustomerType ordinal for registered user.
	 */
	public static final int REGISTERED_USER_ORDINAL = 2;
	/**
	 * The CustomerType instance for registered user.
	 */
	public static final CustomerType REGISTERED_USER = new CustomerType(REGISTERED_USER_ORDINAL, "REGISTERED_USER", CustomerType.class);

	/**
	 * The CustomerType ordinal for account user.
	 */
	public static final int ACCOUNT_ORDINAL = 3;
	/**
	 * The CustomerType instance for account user.
	 */
	public static final CustomerType ACCOUNT = new CustomerType(ACCOUNT_ORDINAL, "ACCOUNT", CustomerType.class);


	/**
	 * Create an enum value for a enum type. Name is case-insensitive and will be stored in upper-case.
	 *
	 * @param ordinal the ordinal value
	 * @param name    the name value (this will be converted to upper-case).
	 * @param klass   the enum type interface
	 */
	protected CustomerType(final int ordinal, final String name, final Class<CustomerType> klass) {
		super(ordinal, name, klass);
	}

	@Override
	protected Class<CustomerType> getEnumType() {
		return CustomerType.class;
	}

	/**
	 * Find the CustomerType with the specified ordinal value.
	 * @param ordinal the ordinal value
	 * @return the CustomerType
	 */
	public static CustomerType valueOf(final int ordinal) {
		return valueOf(ordinal, CustomerType.class);
	}

	/**
	 * Find the CustomerType with the specified name.
	 *
	 * @param name the name
	 * @return the CustomerType
	 */
	public static CustomerType valueOf(final String name) {
		return valueOf(name, CustomerType.class);
	}

	/**
	 * Return the values.
	 *
	 * @return the collection of values
	 */
	public static Collection<CustomerType> values() {
		return values(CustomerType.class);
	}
}
