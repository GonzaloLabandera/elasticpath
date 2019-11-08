/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.selenium.domainobjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Attribute class state object.
 */
public class Attribute {
	private String attributeKey;
	private String attributeUsage;
	private String attributeType;
	private boolean attributeRequired;
	private boolean multiLanguage;
	private boolean multiValuesAllowed;
	private final Map<String, String> names = new HashMap<>();
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(final String key) {
		this.key = key;
	}

	public String getName(final String language) {
		return names.get(language);
	}

	public void setName(final String language, final String name) {
		names.put(language, name);
	}

	public String getAttributeKey() {
		return attributeKey;
	}

	public void setAttributeKey(final String attributeKey) {
		this.attributeKey = attributeKey;
	}

	public String getAttributeUsage() {
		return attributeUsage;
	}

	public void setAttributeUsage(final String attributeUsage) {
		this.attributeUsage = attributeUsage;
	}

	public String getAttributeType() {
		return attributeType;
	}

	public void setAttributeType(final String attributeType) {
		this.attributeType = attributeType;
	}

	public boolean isAttributeRequired() {
		return attributeRequired;
	}

	public void setAttributeRequired(final boolean attributeRequired) {
		this.attributeRequired = attributeRequired;
	}

	public boolean isMultiLanguage() {
		return multiLanguage;
	}

	public void setMultiLanguage(final boolean multiLanguage) {
		this.multiLanguage = multiLanguage;
	}

	public boolean isMultiValuesAllowed() {
		return multiValuesAllowed;
	}

	public void setMultiValuesAllowed(final boolean multiValuesAllowed) {
		this.multiValuesAllowed = multiValuesAllowed;
	}
}
