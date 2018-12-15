/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.service.attribute.impl;

/**
 * Value object for holding attribute value information
 */
public class AttributeValueInfo {

	private String localizedAttributeKey;
	private String value;

	/**
	 * The constructor.
	 *
	 * @param localizedAttributeKey field containing localized attribute key.
	 * @param value                 the value.
	 */
	public AttributeValueInfo(final String localizedAttributeKey, final String value) {
		this.localizedAttributeKey = localizedAttributeKey;
		this.value = value;
	}

	public String getLocalizedAttributeKey() {
		return localizedAttributeKey;
	}

	public void setLocalizedAttributeKey(final String localizedAttributeKey) {
		this.localizedAttributeKey = localizedAttributeKey;
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}
}
