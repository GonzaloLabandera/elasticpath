/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.orderpaymentapi.util;

import java.util.Locale;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * Utility class for handling payment DTOs.
 */
public final class PaymentDTOsUtil {

	/**
	 * Constructor.
	 */
	private PaymentDTOsUtil() {
		super();
	}

	/**
	 * Gets the localized display name for a given locale.
	 *
	 * @param paymentProviderConfigDTO the paymentProviderConfigDTO
	 * @param locale the locale
	 * @return the display name of the PaymentProviderConfiguration displayName
	 */
	public static String getDisplayName(final PaymentProviderConfigDTO paymentProviderConfigDTO, final Locale locale) {
		String displayName = null;
		if (paymentProviderConfigDTO.getLocalizedNames() != null && locale != null) {
			displayName = paymentProviderConfigDTO.getLocalizedNames().get(locale.toString());
		}
		if (displayName == null) {
			displayName = paymentProviderConfigDTO.getDefaultDisplayName();
		}
		if (displayName == null) {
			displayName = paymentProviderConfigDTO.getConfigurationName();
		}
		return displayName;
	}

}
