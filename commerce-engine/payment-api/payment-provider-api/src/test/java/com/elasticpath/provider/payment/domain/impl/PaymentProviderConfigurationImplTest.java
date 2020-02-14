package com.elasticpath.provider.payment.domain.impl;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.PaymentLocalizedPropertiesImpl;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValue;
import com.elasticpath.provider.payment.PaymentLocalizedPropertyValueImpl;

public class PaymentProviderConfigurationImplTest {

	private PaymentProviderConfigurationImpl paymentProviderConfiguration;
	private static final String NAME_FR = "GST (Le Canada)";
	private static final String DEFAULT_DISPLAY_NAME = "defaultDisplayName";
	private static final String CONFIGURATION_NAME = "configurationName";

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@Before
	public void setUp() throws Exception {
		this.paymentProviderConfiguration = getNewProvider();
	}

	private PaymentProviderConfigurationImpl getNewProvider() {
		return new PaymentProviderConfigurationImpl();
	}

	/**
	 * Test method for 'com.elasticpath.provider.payment.domain.impl.PaymentProviderConfigurationImpl.getConfigurationName()'.
	 */
	@Test
	public void testGetSetLocalizedProperties() {
		final PaymentLocalizedProperties localizedProperties = createPaymentLocalizedProperties();

		this.paymentProviderConfiguration.setPaymentLocalizedProperties(localizedProperties);
		assertEquals(NAME_FR,
				this.paymentProviderConfiguration.getPaymentLocalizedProperties().getValue(
						PaymentProviderConfigurationImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, Locale.FRANCE));

		assertEquals(NAME_FR, this.paymentProviderConfiguration.getDisplayName(Locale.FRANCE));
	}

	/**
	 * Verifies that default displayed name is return if name do not found by PaymentProviderConfiguration.
	 */
	@Test
	public void verifyThatGetDisplayNameReturnDefaultDisplayNameIfNameDoNotFoundByLocale() {
		final PaymentLocalizedProperties localizedProperties = createPaymentLocalizedProperties();

		this.paymentProviderConfiguration.setPaymentLocalizedProperties(localizedProperties);
		this.paymentProviderConfiguration.setDefaultDisplayName(DEFAULT_DISPLAY_NAME);

		assertEquals(DEFAULT_DISPLAY_NAME, paymentProviderConfiguration.getDisplayName(Locale.CANADA));
	}

	/**
	 * Verifies that configuration name is return if name by locale and default display name do not found by PaymentProviderConfiguration.
	 */
	@Test
	public void verifyThatGetDisplayNameReturnConfigurationNameIfNameDoNotFoundByLocaleAndDefaultNameDoNotExist() {
		final PaymentLocalizedProperties localizedProperties = createPaymentLocalizedProperties();

		this.paymentProviderConfiguration.setPaymentLocalizedProperties(localizedProperties);
		this.paymentProviderConfiguration.setConfigurationName(CONFIGURATION_NAME);

		assertEquals(CONFIGURATION_NAME, paymentProviderConfiguration.getDisplayName(Locale.CANADA));
	}

	private PaymentLocalizedProperties createPaymentLocalizedProperties() {
		final PaymentLocalizedProperties localizedProperties = new PaymentLocalizedPropertiesImpl() {
			private static final long serialVersionUID = 5000000001L;

			@Override
			protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
				return new PaymentLocalizedPropertyValueImpl();
			}
		};
		localizedProperties.setValue(PaymentProviderConfigurationImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, Locale.FRANCE, NAME_FR);
		return localizedProperties;
	}
}