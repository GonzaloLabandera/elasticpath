/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.factory;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.customer.impl.CustomerImpl;

/**
 * Creates a customer profile useful for unit testing.
 */
public class TestCustomerProfileFactory {
	private static final boolean REQUIRED = true;
	private static final boolean NOT_REQUIRED = false;

	/**
	 * Setup the customer profile attribute.
	 * @return the constructed customer profile
	 */
	public Map<String, Attribute> getProfile() {
		HashMap<String, Attribute> customerProfileMap = new HashMap<>();

		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_FIRST_NAME, REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_LAST_NAME, REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_EMAIL, REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_PREF_LOCALE, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_PREF_CURR, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_PHONE, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_FAX, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_COMPANY, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_HTML_EMAIL, REQUIRED,
				AttributeType.BOOLEAN);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_DOB, NOT_REQUIRED,
				AttributeType.DATE);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_BE_NOTIFIED, NOT_REQUIRED,
				AttributeType.BOOLEAN);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_BUSINESS_NUMBER, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.USER_PROFILE_USAGE, CustomerImpl.ATT_KEY_CP_TAX_EXEMPTION_ID, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.ACCOUNT_PROFILE_USAGE, CustomerImpl.ATT_KEY_AP_NAME, REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.ACCOUNT_PROFILE_USAGE, CustomerImpl.ATT_KEY_AP_BUSINESS_NUMBER, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.ACCOUNT_PROFILE_USAGE, CustomerImpl.ATT_KEY_AP_PHONE, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.ACCOUNT_PROFILE_USAGE, CustomerImpl.ATT_KEY_AP_FAX, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);
		addAttribute(customerProfileMap, AttributeUsageImpl.ACCOUNT_PROFILE_USAGE, CustomerImpl.ATT_KEY_AP_TAX_EXEMPTION_ID, NOT_REQUIRED,
				AttributeType.SHORT_TEXT);

		return customerProfileMap;
	}


	private void addAttribute(final Map<String, Attribute> customerProfileMap, final AttributeUsage attributeUsage, final String key,
							  final boolean required, final AttributeType type) {

		Attribute attribute = new AttributeImpl() {
			private static final long serialVersionUID = -5855153586334823605L;

			@Override
			public AttributeUsage getAttributeUsage() {
				return attributeUsage;
			}

			@Override
			public int getAttributeUsageId() {
				return attributeUsage.getValue();
			}

			@Override
			public void setAttributeUsage(final AttributeUsage attributeUsage) {
				throw new UnsupportedOperationException("You tried to set attributeUsage on a test AttributeImpl that was"
						+ " created by TestCustomerProfileFactory.  We are trying to avoid needing ATTRIBUTE_USAGE to be defined"
						+ " in the beanFactory.");
			}

			@Override
			public void setAttributeUsageId(final int attributeUsageId) {
				throw new UnsupportedOperationException("You tried to set attributeUsageId on a test AttributeImpl that was"
						+ " created by TestCustomerProfileFactory.  We are trying to avoid needing ATTRIBUTE_USAGE to be defined"
						+ " in the beanFactory.");
			}
		};
		attribute.setKey(key);
		attribute.setRequired(required);
		attribute.setAttributeType(type);

		customerProfileMap.put(key, attribute);

	}
}
