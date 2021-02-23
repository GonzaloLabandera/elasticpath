/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.impl;

import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Completable;

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.addresses.AddressEntity;
import com.elasticpath.rest.definitions.validator.constants.ValidationMessages;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.validator.AddressValidator;

/**
 * Implementation for AddressValidator.
 */
@Singleton
@Named("addressValidator")
public class AddressValidatorImpl implements AddressValidator {

	private static final String NO_VALID_ADDRESS_FIELDS = "No valid address fields specified.";

	@Override
	public Completable validate(final AddressEntity addressEntity) {
		if (addressEntity == null) {
			return Completable.error(ResourceOperationFailure.badRequestBody(ValidationMessages.MISSING_REQUIRED_REQUEST_BODY));
		}
		if (!representationHasFields(addressEntity)) {
			return Completable.error(ResourceOperationFailure.badRequestBody(NO_VALID_ADDRESS_FIELDS));
		}
		return Completable.complete();
	}

	private boolean representationHasFields(final AddressEntity address) {
		final com.elasticpath.rest.definition.base.AddressEntity addressEntity = address.getAddress();

		return addressEntityHasData(addressEntity)
				|| (address.getOrganization() != null && address.getPhoneNumber() != null);
	}

	@SuppressWarnings("squid:S1067")
	private boolean addressEntityHasData(final com.elasticpath.rest.definition.base.AddressEntity addressEntity) {
		return addressEntity != null
				&& (
				addressEntity.getStreetAddress() != null
						|| addressEntity.getExtendedAddress() != null
						|| addressEntity.getLocality() != null
						|| addressEntity.getRegion() != null
						|| addressEntity.getCountryName() != null
						|| addressEntity.getPostalCode() != null
		);
	}
}
