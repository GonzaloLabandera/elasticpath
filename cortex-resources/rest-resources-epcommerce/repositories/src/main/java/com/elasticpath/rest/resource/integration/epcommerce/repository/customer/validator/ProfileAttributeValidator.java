/**
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator;

import io.reactivex.Completable;

import com.elasticpath.rest.definition.profiles.ProfileEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;

/**
 * Validator for a set of dynamic Profile Attributes.
 */
public interface ProfileAttributeValidator {

	/**
	 * Validate the profile entity for update.  Fields with null values will be ignored.
	 *
	 * @param profileEntity     the profile entity
	 * @param profileIdentifier the profile identifier
	 * @return an error if validation fails, success otherwise
	 */
	Completable validate(ProfileEntity profileEntity, ProfileIdentifier profileIdentifier);
}
