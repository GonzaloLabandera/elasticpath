/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.plugin.payment.provider.dto;

/**
 * The enum Transaction type.
 */
public enum TransactionType {

	/**
	 * Reserve transaction type.
	 */
	RESERVE,

	/**
	 * Modify reserve transaction type.
	 */
	MODIFY_RESERVE,

	/**
	 * Cancel reserve transaction type.
	 */
	CANCEL_RESERVE,

	/**
	 * Credit transaction type.
	 */
	CREDIT,

	/**
	 * Manual credit transaction type.
	 */
	MANUAL_CREDIT,

	/**
	 * Commit transaction type.
	 */
	CHARGE,

	/**
	 * Reverse charge transaction type.
	 */
	REVERSE_CHARGE
}
