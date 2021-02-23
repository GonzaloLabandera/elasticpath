/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.cmclient.fulfillment.util;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.domain.customer.Address;

/**
 * Utility class for handling addresses.
 */
public final class AddressUtil {
	private static final String COMMA = ","; //$NON-NLS-1$

	private static final String SPACE = " "; //$NON-NLS-1$

	private static final String SEPARATOR = COMMA + SPACE;

	/**
	 * Constructor.
	 */
	private AddressUtil() {
		super();
	}
	
	/**
	 * Formats an address in a single line.
	 * 
	 * @param address the address to format
	 * @param displayNames should the customer names be displayed
	 * @return a string representing the address
	 */
	public static String formatAddress(final Address address, final boolean displayNames) {
		final StringBuilder addressStr = new StringBuilder();
		if (displayNames) {
			addressStr.append(getFullCustomerName(address));
			if (addressStr.length() != 0) {
				addressStr.append(SEPARATOR);
			}
		}

		addressStr.append(address.getStreet1()).append(SEPARATOR);
		if (address.getStreet2() != null && address.getStreet2().length() > 0) {
			addressStr.append(address.getStreet2()).append(SEPARATOR);
		}
		addressStr.append(address.getCity()).append(SEPARATOR);
		if (address.getSubCountry() != null && address.getSubCountry().length() > 0) {
			addressStr.append(address.getSubCountry()).append(SEPARATOR);
		}
		addressStr.append(address.getZipOrPostalCode()).append(SEPARATOR);
		addressStr.append(address.getCountry());

		return addressStr.toString();
	}

	/**
	 * Gets a formatted string of only the customer names in the address.
	 * 
	 * @param address the address
	 * @return first and last name saved in the address 
	 */
	public static String getFullCustomerName(final Address address) {
		final StringBuilder builder = new StringBuilder();
		final String firstName = address.getFirstName();
		final String lastName = address.getLastName();
		if (StringUtils.isNotBlank(firstName)) {
			builder.append(firstName);
		}
		final boolean lastNameNotEmpty = StringUtils.isNotBlank(lastName);
		if (builder.length() != 0 && lastNameNotEmpty) {
			builder.append(SPACE);
		}
		if (lastNameNotEmpty) {
			builder.append(lastName);
		}
		return builder.toString();
	}

}
