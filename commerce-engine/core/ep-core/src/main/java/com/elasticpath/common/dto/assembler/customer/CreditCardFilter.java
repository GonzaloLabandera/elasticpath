/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.assembler.customer;

import com.elasticpath.common.dto.customer.CreditCardDTO;

/**
 * Used to filter a credit card during DtoAssembler processing.
 * See {@code BuiltinFilters} for two simple implementations.
 */
public interface CreditCardFilter {

	/**
	 * Given a credit card, filter it in some interesting way.
	 * 
	 * @param creditCardDto The card to transform.
	 * @return The filtered credit card or null.
	 */
	CreditCardDTO filter(CreditCardDTO creditCardDto);
}
