/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.core.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.CorePlugin;

/**
 * Validator class that checks that a String has no leading or trailing spaces (and tabs).
 */
public class NoSpacesValidator implements IValidator {

	/**
	 * Performs validation checking for spaces in the value.
	 *
	 * @param value the value to be validated
	 * @return OK_STATUS if there is no spaces
	 */
	public IStatus validate(final Object value) {
		String stringValue = (String) value;
		if (stringValue.indexOf(' ') != -1
				|| stringValue.indexOf('\t') != -1) {
			return new Status(
					IStatus.ERROR,
					CorePlugin.PLUGIN_ID,
					IStatus.ERROR,
					CoreMessages.get().EpValidatorFactory_NoSpace,
					null);
		}
		return Status.OK_STATUS;
	}

}
