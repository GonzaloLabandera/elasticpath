/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.cmclient.fulfillment.views;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.elasticpath.cmclient.core.validation.CompoundValidator;
import com.elasticpath.cmclient.core.validation.EpValidatorFactory;

/**
 * Validators used by the search tabs.
 */
class SearchFieldsValidators {

	/**
	 * Customer names validator.
	 */
	protected static final IValidator NAME_VALIDATOR = new CompoundValidator(new IValidator[] { EpValidatorFactory.MAX_LENGTH_255, 
			EpValidatorFactory.NO_SPECIAL_CHARACTERS });
	/**
	 * Zip code validator.
	 */
	protected static final IValidator ZIP_CODE_VALIDATOR = new CompoundValidator(new IValidator[] { EpValidatorFactory.MAX_LENGTH_50, 
			EpValidatorFactory.NO_SPECIAL_CHARACTERS });

	private static final IValidator[] EMAIL_VALIDATORS = new IValidator[] { EpValidatorFactory.NO_SPECIAL_CHARACTERS,
			EpValidatorFactory.EMAIL };

	/**
	 * Email/userID validator. Uses OR operator in order to validate the input of an email or user ID.
	 */
	protected static final IValidator EMAIL_USERID_VALIDATOR = new IValidator() {
		@Override
		public IStatus validate(final Object value) {
			IStatus status = Status.CANCEL_STATUS;
			for (final IValidator currValidator : EMAIL_VALIDATORS) {
				status = currValidator.validate(value);
				if (status.isOK()) {
					return status;
				}
			}
			return status;
		}
	};
}
