/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.orderpaymentapi.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * The junit test class of PaymentDTOsUtil.
 */
public class PaymentDTOsUtilTest {

	private static final String CONFIG_NAME = "CONFIG_NAME";
	private static final String DEFAULT_DISPLAY_NAME = "DEFAULT_DISPLAY_NAME";
	private static final Locale CANADA_FRENCH_LOCALE = Locale.CANADA_FRENCH;
	private static final String CANADA_FRENCH_NAME = "en fr name";
	private static final Locale FRENCH_LOCALE = Locale.FRENCH;
	private static final String FRENCH_NAME = "fr name";

	private static final Locale OTHER_LOCALE = Locale.GERMAN;

	/**
	 * Test the getPage method.
	 */
	@Test
	public void testGetDisplayName() {
		PaymentProviderConfigDTO paymentProviderConfigDTO = new PaymentProviderConfigDTO();
		paymentProviderConfigDTO.setConfigurationName(CONFIG_NAME);
		Map<String, String> localizedMap = new HashMap<>();
		localizedMap.put(CANADA_FRENCH_LOCALE.toString(), CANADA_FRENCH_NAME);
		localizedMap.put(FRENCH_LOCALE.toString(), FRENCH_NAME);
		paymentProviderConfigDTO.setLocalizedNames(localizedMap);

		//test get config name
		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, null))
				.isEqualTo(CONFIG_NAME);
		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, OTHER_LOCALE))
				.isEqualTo(CONFIG_NAME);

		//test default name
		paymentProviderConfigDTO.setDefaultDisplayName(DEFAULT_DISPLAY_NAME);

		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, null))
				.isEqualTo(DEFAULT_DISPLAY_NAME);
		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, OTHER_LOCALE))
				.isEqualTo(DEFAULT_DISPLAY_NAME);

		//test localized names
		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, CANADA_FRENCH_LOCALE))
				.isEqualTo(CANADA_FRENCH_NAME);
		assertThat(PaymentDTOsUtil.getDisplayName(paymentProviderConfigDTO, FRENCH_LOCALE))
				.isEqualTo(FRENCH_NAME);
	}

}
