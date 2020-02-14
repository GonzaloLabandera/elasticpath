/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.elasticpath.domain.misc.LocalizedAttributeKeyUtils;

/**
 * Represents a default implementation of <code>PaymentLocalizedProperties</code>.
 */
public class PaymentLocalizedPropertiesImpl implements PaymentLocalizedProperties {

	/**
	 * The name of localized property -- name.
	 */
	public static final String PAYMENT_LOCALIZED_PROPERTY_NAME = "paymentLocalizedPropertyDisplayName";

	private static final char SEPARATOR = '_';

	private Map<String, PaymentLocalizedPropertyValue> paymentLocalizedPropertiesMap = new HashMap<>();

	private String paymentLocalizedPropertyValueBean;

	/**
	 * Returns the localized property bean ID.
	 *
	 * @return the localized property bean ID.
	 */
	protected String getLocalizedPropertyValueBean() {
		return paymentLocalizedPropertyValueBean;
	}

	@Override
	public String getValue(final String propertyName, final Locale locale) {
		if (propertyName == null) {
			throw new IllegalArgumentException("Property name cannot be null.");
		}
		if (locale == null) {
			throw new IllegalArgumentException("Locale cannot be null.");
		}

		return getValueWithoutFallBack(propertyName, locale);
	}

	@Override
	public void setValue(final String propertyName, final Locale locale, final String value) {
		if (propertyName == null) {
			throw new IllegalArgumentException("Property name cannot be null.");
		}
		if (locale == null) {
			throw new IllegalArgumentException("Locale cannot be null.");
		}
		final String lpk = propertyName + SEPARATOR + locale;

		final PaymentLocalizedPropertyValue wrappedValue = getNewPaymentLocalizedPropertyValue();
		wrappedValue.setPaymentLocalizedPropertyKey(lpk);
		wrappedValue.setValue(value);
		if (StringUtils.isEmpty(value)) {
			paymentLocalizedPropertiesMap.remove(lpk);
		} else {
			paymentLocalizedPropertiesMap.put(lpk, wrappedValue);
		}
	}


	/**
	 * Creates a new instance of {@link PaymentLocalizedPropertyValue}.
	 *
	 * @return an instance of the bean specified by {@link #setPaymentLocalizedPropertiesMap(Map, String)}
	 */
	protected PaymentLocalizedPropertyValue getNewPaymentLocalizedPropertyValue() {
		return new PaymentLocalizedPropertyValueImpl();
	}

	@Override
	public Map<String, PaymentLocalizedPropertyValue> getPaymentLocalizedPropertiesMap() {
		return paymentLocalizedPropertiesMap;
	}


	@Override
	public void setPaymentLocalizedPropertiesMap(final Map<String, PaymentLocalizedPropertyValue> map,
												 final String paymentLocalizedPropertyValueBean) {
		if (map != null) {
			paymentLocalizedPropertiesMap = map;
			this.paymentLocalizedPropertyValueBean = paymentLocalizedPropertyValueBean;
		}
	}

	@Override
	public String getValueWithoutFallBack(final String propertyName, final Locale locale) {
		PaymentLocalizedPropertyValue value = null;

		// Try with the entire locale string as a key first (language_country)
		value = paymentLocalizedPropertiesMap.get(propertyName + SEPARATOR + locale);

		// If null, try with just the language as a key
		if (value == null) {
			value = paymentLocalizedPropertiesMap.get(propertyName + SEPARATOR + locale.getLanguage());
		}
		if (value != null) {
			return value.getValue();
		}
		return null;
	}

	@Override
	public Locale getLocaleFromKey(final String keyInMap) {
		return LocalizedAttributeKeyUtils.getLocaleFromLocalizedKeyName(keyInMap);
	}

	@Override
	public String getPropertyNameFromKey(final String keyInMap) {
		return LocalizedAttributeKeyUtils.getAttributePropertyFromLocalizedKeyName(keyInMap);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PaymentLocalizedPropertiesImpl)) {
			return false;
		}

		final PaymentLocalizedPropertiesImpl other = (PaymentLocalizedPropertiesImpl) obj;
		return Objects.equals(paymentLocalizedPropertiesMap, other.paymentLocalizedPropertiesMap);
	}

	@Override
	public int hashCode() {
		return Objects.hash(paymentLocalizedPropertiesMap);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("paymentLocalizedPropertiesMap", getPaymentLocalizedPropertiesMap())
				.toString();
	}
}
