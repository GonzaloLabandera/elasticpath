/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.core.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import com.elasticpath.cmclient.core.CoreMessages;
import com.elasticpath.cmclient.core.CorePlugin;

/**
 * A common validator for maximum string length.
 */
public class MaxStringLengthValidator implements IValidator {

	private final int stringLength;

	/**
	 * Constructor.
	 *
	 * @param stringLength defines the maximum string length to be validate
	 */
	public MaxStringLengthValidator(final int stringLength) {
		this.stringLength = stringLength;
	}

	/**
	 * Performs validation using string length.
	 *
	 * @param value the value to be validated
	 * @return OK_STATUS if length value pass
	 */
	public IStatus validate(final Object value) {
		final String stringValue = (String) value;

		if (stringValue.length() > stringLength) {
			return new Status(IStatus.ERROR,
					CorePlugin.PLUGIN_ID,
					IStatus.ERROR,

					NLS.bind(CoreMessages.get().EpValidatorFactory_MaxCharLength,
							stringLength),
					null);
		}
		return Status.OK_STATUS;
	}

}
