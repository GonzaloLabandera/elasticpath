/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.customer.validator;

import io.reactivex.Completable;

import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;

/**
 * Validator for a set of dynamic Account Attributes.
 */
public interface AccountAttributeValidator {

	/**
	 * Validate the account entity for update.  Fields with null values will be ignored.
	 *
	 * @param accountEntity     the account entity
	 * @param accountIdentifier the account identifier
	 * @return an error if validation fails, success otherwise
	 */
	Completable validate(AccountEntity accountEntity, AccountIdentifier accountIdentifier);
}
