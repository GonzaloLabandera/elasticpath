/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.base.AddressEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.BaseAddressValidator;

/**
 * Implementation for AddressValidator.
 */
@Singleton
@Named("baseAddressValidator")
public class BaseAddressValidatorImpl implements BaseAddressValidator {

	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";

	@Override
	public Completable validate(final AddressEntity addressEntity) {
		if (addressEntity == null) {
			return Completable.error(ResourceOperationFailure.badRequestBody(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY));
		}
		if (!addressEntityHasData(addressEntity)) {
			return Completable.error(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
		}
		return Completable.complete();
	}

	@SuppressWarnings("squid:S1067")
	private boolean addressEntityHasData(final AddressEntity address) {
		return address != null
				&& (
				address.getStreetAddress() != null
						|| address.getExtendedAddress() != null
						|| address.getLocality() != null
						|| address.getRegion() != null
						|| address.getCountryName() != null
						|| address.getPostalCode() != null
		);
	}
}
