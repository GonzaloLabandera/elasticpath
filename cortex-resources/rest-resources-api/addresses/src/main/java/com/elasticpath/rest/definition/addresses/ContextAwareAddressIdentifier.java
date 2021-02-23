/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.definition.addresses;

import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.ResourceIdentifier;

/**
 * The list of addresses for the customer.
 */
public class ContextAwareAddressIdentifier implements ResourceIdentifier {

	/**
	 * The template used to create URIs from this Identifier.
	 */
	public static final String URI_TEMPLATE = AddressIdentifier.URI_TEMPLATE;

	/**
	 * The uri part name used in other Identifier's uri templates.
	 */
	public static final String RESOURCE_NAME = AddressIdentifier.RESOURCE_NAME;

	/**
	 * The list of addresses for the customer.
	 */
	public static final String ADDRESSES = AddressIdentifier.ADDRESSES;

	/**
	 * The address ID.
	 */
	public static final String ADDRESS_ID = AddressIdentifier.ADDRESS_ID;

	private final AddressIdentifier addressIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addressIdentifier the address identifier for the customer.
	 */
	public ContextAwareAddressIdentifier(final AddressIdentifier addressIdentifier) {
		this.addressIdentifier = addressIdentifier;
	}

	/**
	 * Gets address identifier.
	 *
	 * @return address identifier.
	 */
	public AddressIdentifier getAddressIdentifier() {
		return addressIdentifier;
	}

	@Override
	public String resourceName() {
		return addressIdentifier.resourceName();
	}

	/**
	 * Gets address ID.
	 *
	 * @return address ID
	 */
	public IdentifierPart<String> getAddressId() {
		return addressIdentifier.getAddressId();
	}

	/**
	 * Gets list of addresses for the customer.
	 *
	 * @return AddressesIdentifier for the customer.
	 */
	public AddressesIdentifier getAddresses() {
		return addressIdentifier.getAddresses();
	}
}
