/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;

/**
 * Test <code>PaymentLocalizedPropertiesImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyStaticImports" })
public class PaymentLocalizedPropertiesImplTest {

	private static final String NULL_VALUES_PROHIBITED_MSG = "Null values are prohibited";

	private PaymentLocalizedPropertiesImpl paymentLocalizedPropertiesImpl;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of any error happens
	 */
	@Before
	public void setUp() throws Exception {

		this.paymentLocalizedPropertiesImpl = new PaymentLocalizedPropertiesImpl() {
			private static final long serialVersionUID = -7100252602367051323L;

			@Override
			protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
				return new PaymentLocalizedPropertyValueImpl();
			}
		};
	}

	/**
	 * Test that getValue() and SetValue() methods cannot be called with null parameters;
	 * IllegalArgumentException will be thrown.
	 */
	@Test
	public void testGetValueWithNullsForbidden() {

		assertThatThrownBy(() -> paymentLocalizedPropertiesImpl.getValue(null, Locale.US))
				.as(NULL_VALUES_PROHIBITED_MSG)
				.isInstanceOf(IllegalArgumentException.class);


		assertThatThrownBy(() -> paymentLocalizedPropertiesImpl.getValue("", null))
				.as(NULL_VALUES_PROHIBITED_MSG)
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> paymentLocalizedPropertiesImpl.setValue(null, Locale.US, ""))
				.as(NULL_VALUES_PROHIBITED_MSG)
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> paymentLocalizedPropertiesImpl.setValue("", null, ""))
				.as(NULL_VALUES_PROHIBITED_MSG)
				.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Tests that setting null value to a property gets null value afterwards.
	 */
	@Test
	public void testSetNullValuePaymentLocalizedProperty() {
		String propertyName = "myprop1";
		String value = "test1";
		paymentLocalizedPropertiesImpl.setValue(propertyName, Locale.CANADA, value);
		assertThat(paymentLocalizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isEqualTo(value);

		paymentLocalizedPropertiesImpl.setValue(propertyName, Locale.CANADA, null);
		assertThat(paymentLocalizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isNull();
	}

	/**
	 * Tests that setting empty string value to a property gets null value afterwards.
	 */
	@Test
	public void testSetEmptyStringValuePaymentLocalizedProperty() {
		String propertyName = "myprop2";
		String value = "test2";
		paymentLocalizedPropertiesImpl.setValue(propertyName, Locale.CANADA, value);
		assertThat(paymentLocalizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isEqualTo(value);

		paymentLocalizedPropertiesImpl.setValue(propertyName, Locale.CANADA, "");
		assertThat(paymentLocalizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isNull();
	}

	/**
	 * Test that GetValue() succeeds.
	 */
	@Test
	public void testGetAndSet() {
		final String propertyName1 = "testProperty";
		final Locale locale1 = Locale.US;
		final Locale locale2 = Locale.CANADA;
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale1)).isNull();

		// Set the value to a property name
		String value1 = "testValue";
		this.paymentLocalizedPropertiesImpl.setValue(propertyName1, locale1, value1);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.paymentLocalizedPropertiesImpl.getValueWithoutFallBack(propertyName1, locale2)).isNull();
		// When the value of the given locale doesn't exist, should not fallback to the value of the default locale
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale2)).isNull();

		// Set the value to the same property name with another locale
		String value2 = "testValue2";
		this.paymentLocalizedPropertiesImpl.setValue(propertyName1, locale2, value2);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale2)).isEqualTo(value2);

		// Set the value to the another property name
		final String propertyName2 = "testProperty2";
		this.paymentLocalizedPropertiesImpl.setValue(propertyName2, locale2, value2);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName1, locale2)).isEqualTo(value2);
		assertThat(this.paymentLocalizedPropertiesImpl.getValue(propertyName2, locale2)).isEqualTo(value2);
	}

	/**
	 * Test that PaymentLocalizedPropertiesMap is never null.
	 */
	@Test
	public void testGetPaymentLocalizedPropertiesMap() {
		assertThat(this.paymentLocalizedPropertiesImpl.getPaymentLocalizedPropertiesMap()).isNotNull();
	}

	/**
	 * Test that you can set the LocalizedPropertiesMap.
	 */
	@Test
	public void testSetPaymentLocalizedPropertiesMap() {
		final Map<String, PaymentLocalizedPropertyValue> map = new HashMap<>();
		final String beanId = PaymentProviderApiContextIdNames.PAYMENT_LOCALIZED_PROPERTY_VALUE;
		this.paymentLocalizedPropertiesImpl.setPaymentLocalizedPropertiesMap(map, beanId);
		assertThat(this.paymentLocalizedPropertiesImpl.getPaymentLocalizedPropertiesMap()).isEqualTo(map);
		assertThat(this.paymentLocalizedPropertiesImpl.getLocalizedPropertyValueBean()).isEqualTo(beanId);
	}

	@Test
	public void verifyLocalesCanHaveCountryAndLanguage() {
		// in other words, locale strings can have an underscore in them
		final String attributeName = "attributename";
		final Locale locale = Locale.CANADA; // i.e. en_CA

		final String localisedAttributeKey = attributeName + "_" + locale;

		assertThat(paymentLocalizedPropertiesImpl.getLocaleFromKey(localisedAttributeKey))
				.isEqualTo(locale);
	}

	@Test
	public void verifyAttributeNamesMayHaveUnderscores() {
		final String attributeName = "my_attribute_name";
		final Locale locale = Locale.CANADA;

		final String localisedAttributeKey = attributeName + "_" + locale;

		assertThat(paymentLocalizedPropertiesImpl.getPropertyNameFromKey(localisedAttributeKey))
				.isEqualTo(attributeName);
	}

	/**
	 * Tests that equals works as expected.
	 */
	@Test
	public void testEquals() {

		assertThat(paymentLocalizedPropertiesImpl)
				.as("Every object should be equal to itself")
				.isEqualTo(paymentLocalizedPropertiesImpl);

		PaymentLocalizedProperties otherPaymentLocalizedPropertiesImpl = new PaymentLocalizedPropertiesImpl() {
			private static final long serialVersionUID = 7825564499802504460L;

			@Override
			protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
				return new PaymentLocalizedPropertyValueImpl();
			}
		};
		assertThat(paymentLocalizedPropertiesImpl).isNotEqualTo(null);
		assertThat(otherPaymentLocalizedPropertiesImpl).isEqualTo(paymentLocalizedPropertiesImpl);

		otherPaymentLocalizedPropertiesImpl.setValue("test", Locale.CANADA, "testValue");

		assertThat(paymentLocalizedPropertiesImpl).isNotEqualTo(otherPaymentLocalizedPropertiesImpl);
	}

	/**
	 * Tests two equal/non-equal objects having equal/non-equal hash code values.
	 */
	@Test
	public void testHashCode() {
		PaymentLocalizedProperties otherPaymentLocalizedPropertiesImpl = new PaymentLocalizedPropertiesImpl() {
			private static final long serialVersionUID = 5677652994143700940L;

			@Override
			protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
				return new PaymentLocalizedPropertyValueImpl();
			}
		};

		assertThat(otherPaymentLocalizedPropertiesImpl).hasSameHashCodeAs(paymentLocalizedPropertiesImpl);

		otherPaymentLocalizedPropertiesImpl.setValue("test", Locale.CANADA, "testValue");

		assertThat(paymentLocalizedPropertiesImpl.hashCode()).isNotEqualTo(otherPaymentLocalizedPropertiesImpl.hashCode());
	}

	/**
	 * Tests that toString() returns non-null value.
	 */
	@Test
	public void testToString() {
		assertThat(paymentLocalizedPropertiesImpl.toString()).isNotNull();
	}

}
