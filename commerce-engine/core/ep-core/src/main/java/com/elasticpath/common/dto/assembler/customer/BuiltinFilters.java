/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.assembler.customer;

import java.util.Locale;

import com.elasticpath.common.dto.customer.CreditCardDTO;

/**
 * Simple builtin credit card filters.
 * Note that we do not ship with one that copies credit card numbers out of the box, although the two here can be used to write your own.
 */
public final class BuiltinFilters {

	/**
	 * A simple filter which always returns null.
	 */
	public static final CreditCardFilter EMPTYING = new CreditCardFilter() {
		
		@Override
		public CreditCardDTO filter(final CreditCardDTO creditCardDTO) {
			return null;
		}
	};

	/**
	 * A simple credit card filter which returns a static fake credit card number value, depending on the card's type.
	 * If the card's type cannot be matched null is returned.
	 */
	public static final CreditCardFilter STATIC = new CreditCardFilter() {

		@Override
		public CreditCardDTO filter(final CreditCardDTO creditCardDto) {
			String staticNumber = getStaticNumber(creditCardDto.getCardType());
			if (staticNumber == null) {
				return null;
			}

			creditCardDto.setCardNumber(staticNumber);

			return creditCardDto;
		}

		private String getStaticNumber(final String cardType) {
			final String type = cardType.toLowerCase(Locale.ENGLISH).trim();

			if (type.startsWith("visa")) {
				return "4111111111111111";
			} else if (type.startsWith("master")) {
				return "5500000000000004";
			} else if (type.startsWith("american")) {
				return "340000000000009";
			} else if (type.startsWith("diner")) {
				return "30000000000004";
			} else if (type.startsWith("discover")) {
				return "6011000000000004";
			}

			return null;
		}
	};

	private BuiltinFilters() {
		// Do not instantiate this class
	}
}
