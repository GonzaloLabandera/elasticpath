/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.misc.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.EpDomainException;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;

/**
 * Test <code>LocalizedPropertiesImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyStaticImports" })
@RunWith(MockitoJUnitRunner.class)
public class LocalizedPropertiesImplTest {

	private static final String NULL_VALUES_PROHIBITED_MSG = "Null values are prohibited";

	private LocalizedPropertiesImpl localizedPropertiesImpl;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of any error happens
	 */
	@Before
	public void setUp() throws Exception {

		this.localizedPropertiesImpl = new LocalizedPropertiesImpl() {
			private static final long serialVersionUID = -7100252602367051323L;

			@Override
			protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
				return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
			}
		};
	}

	/**
	 * Test that getValue() and SetValue() methods cannot be called with null parameters;
	 * EpDomainException will be thrown.
	 */
	@Test
	public void testGetValueWithNullsForbidden() {

		assertThatThrownBy(() -> localizedPropertiesImpl.getValue(null, Locale.US))
			.as(NULL_VALUES_PROHIBITED_MSG)
			.isInstanceOf(EpDomainException.class);


		assertThatThrownBy(() -> localizedPropertiesImpl.getValue("", null))
			.as(NULL_VALUES_PROHIBITED_MSG)
			.isInstanceOf(EpDomainException.class);

		assertThatThrownBy(() -> localizedPropertiesImpl.setValue(null, Locale.US, ""))
			.as(NULL_VALUES_PROHIBITED_MSG)
			.isInstanceOf(EpDomainException.class);

		assertThatThrownBy(() -> localizedPropertiesImpl.setValue("", null, ""))
			.as(NULL_VALUES_PROHIBITED_MSG)
			.isInstanceOf(EpDomainException.class);
	}

	/**
	 * Tests that setting null value to a property gets null value afterwards.
	 */
	@Test
	public void testSetNullValueLocalizedProperty() {
		String propertyName = "myprop1";
		String value = "test1";
		localizedPropertiesImpl.setValue(propertyName, Locale.CANADA, value);
		assertThat(localizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isEqualTo(value);

		localizedPropertiesImpl.setValue(propertyName, Locale.CANADA, null);
		assertThat(localizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isNull();
	}

	/**
	 * Tests that setting empty string value to a property gets null value afterwards.
	 */
	@Test
	public void testSetEmptyStringValueLocalizedProperty() {
		String propertyName = "myprop2";
		String value = "test2";
		localizedPropertiesImpl.setValue(propertyName, Locale.CANADA, value);
		assertThat(localizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isEqualTo(value);

		localizedPropertiesImpl.setValue(propertyName, Locale.CANADA, "");
		assertThat(localizedPropertiesImpl.getValue(propertyName, Locale.CANADA)).isNull();
	}

	/**
	 * Test that GetValue() succeeds.
	 */
	@Test
	public void testGetAndSet() {
		final String propertyName1 = "testProperty";
		final Locale locale1 = Locale.US;
		final Locale locale2 = Locale.CANADA;
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale1)).isNull();

		// Set the value to a property name
		String value1 = "testValue";
		this.localizedPropertiesImpl.setValue(propertyName1, locale1, value1);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.localizedPropertiesImpl.getValueWithoutFallBack(propertyName1, locale2)).isNull();
		// When the value of the given locale doesn't exist, should not fallback to the value of the default locale
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale2)).isNull();

		// Set the value to the same property name with another locale
		String value2 = "testValue2";
		this.localizedPropertiesImpl.setValue(propertyName1, locale2, value2);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale2)).isEqualTo(value2);

		// Set the value to the another property name
		final String propertyName2 = "testProperty2";
		this.localizedPropertiesImpl.setValue(propertyName2, locale2, value2);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale1)).isEqualTo(value1);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName1, locale2)).isEqualTo(value2);
		assertThat(this.localizedPropertiesImpl.getValue(propertyName2, locale2)).isEqualTo(value2);
	}

	@Test
	public void verifyAttributeNamesMayHaveUnderscores() {
		final String attributeName = "my_attribute_name";
		final Locale locale = Locale.CANADA;

		final String localisedAttributeKey = attributeName + "_" + locale;

		assertThat(localizedPropertiesImpl.getPropertyNameFromKey(localisedAttributeKey))
				.isEqualTo(attributeName);
	}

	@Test
	public void verifyLocalesCanHaveCountryAndLanguage() {
		// in other words, locale strings can have an underscore in them
		final String attributeName = "attributename";
		final Locale locale = Locale.CANADA; // i.e. en_CA

		final String localisedAttributeKey = attributeName + "_" + locale;

		assertThat(localizedPropertiesImpl.getLocaleFromKey(localisedAttributeKey))
				.isEqualTo(locale);
	}

	/**
	 * Test that LocalizedPropertiesMap is never null.
	 */
	@Test
	public void testGetLocalizedPropertiesMap() {
		assertThat(this.localizedPropertiesImpl.getLocalizedPropertiesMap()).isNotNull();
	}

	/**
	 * Test that you can set the LocalizedPropertiesMap.
	 */
	@Test
	public void testSetLocalizedPropertiesMap() {
		final Map<String, LocalizedPropertyValue> map = new HashMap<>();
		final String beanId = ContextIdNames.TAX_CATEGORY_LOCALIZED_PROPERTY_VALUE;
		this.localizedPropertiesImpl.setLocalizedPropertiesMap(map, beanId);
		assertThat(this.localizedPropertiesImpl.getLocalizedPropertiesMap()).isEqualTo(map);
		assertThat(this.localizedPropertiesImpl.getLocalizedPropertyValueBean()).isEqualTo(beanId);
	}

	/**
	 * Tests that equals works as expected.
	 */
	@Test
	public void testEquals() {

		assertThat(localizedPropertiesImpl)
			.as("Every object should be equal to itself")
			.isEqualTo(localizedPropertiesImpl);

		LocalizedProperties otherLocalizedPropertiesImpl = new LocalizedPropertiesImpl() {
			private static final long serialVersionUID = 7825564499802504460L;

			@Override
			protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
				return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
			}
		};
		assertThat(localizedPropertiesImpl).isNotEqualTo(null);
		assertThat(otherLocalizedPropertiesImpl).isEqualTo(localizedPropertiesImpl);

		otherLocalizedPropertiesImpl.setValue("test", Locale.CANADA, "testValue");

		assertThat(localizedPropertiesImpl).isNotEqualTo(otherLocalizedPropertiesImpl);
	}

	/**
	 * Tests two equal/non-equal objects having equal/non-equal hash code values.
	 */
	@Test
	public void testHashCode() {
		LocalizedProperties otherLocalizedPropertiesImpl = new LocalizedPropertiesImpl() {
			private static final long serialVersionUID = 5677652994143700940L;

			@Override
			protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
				return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
			}
		};

		assertThat(otherLocalizedPropertiesImpl).hasSameHashCodeAs(localizedPropertiesImpl);

		otherLocalizedPropertiesImpl.setValue("test", Locale.CANADA, "testValue");

		assertThat(localizedPropertiesImpl.hashCode()).isNotEqualTo(otherLocalizedPropertiesImpl.hashCode());
	}

	/**
	 * Tests that toString() returns non-null value.
	 */
	@Test
	public void testToString() {
		assertThat(localizedPropertiesImpl.toString()).isNotNull();
	}

}
